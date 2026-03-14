package my.gov.met.nwsmalaysia.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import my.gov.met.nwsmalaysia.data.api.WeatherApiService
import my.gov.met.nwsmalaysia.data.db.CachedWarningsEntity
import my.gov.met.nwsmalaysia.data.db.WarningsDao
import my.gov.met.nwsmalaysia.data.model.WarningResponse
import my.gov.met.nwsmalaysia.domain.model.Warning
import my.gov.met.nwsmalaysia.util.WarningFingerprint
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WarningRepo"
private const val CACHE_TTL_MS = 15 * 60 * 1000L

@Singleton
class WarningRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val warningsDao: WarningsDao,
    private val gson: Gson
) {

    /**
     * Returns warnings relevant to the given location.
     * The API does not expose structured state/district fields, so we search the warning
     * title/heading/text for the location's state name and location name — mirroring the
     * PHP dashboard's approach. If no location is given, all warnings are returned.
     */
    suspend fun getWarnings(locationState: String? = null, locationName: String? = null): List<Warning> {
        val cached = warningsDao.get()
        val now = System.currentTimeMillis()
        val cacheValid = cached != null &&
                now - cached.fetchedAt < CACHE_TTL_MS &&
                cached.json != "[]" &&
                cached.json.isNotBlank()
        val raw = if (cacheValid) {
            Log.d(TAG, "Using cached warnings (age=${(now - cached!!.fetchedAt)/1000}s)")
            parseWarningsRaw(cached.json)
        } else {
            fetchAndCacheRaw()
        }
        Log.d(TAG, "getWarnings() locationState=$locationState locationName=$locationName, total raw=${raw.size}")
        val filtered = if (locationState.isNullOrBlank() && locationName.isNullOrBlank()) {
            raw
        } else {
            val keywords = listOfNotNull(locationState, locationName)
                .filter { it.isNotBlank() }
                .map { it.lowercase() }
            raw.filter { w ->
                val searchable = listOfNotNull(
                    w.warningIssue?.titleEn,
                    w.warningIssue?.titleBm,
                    w.headingEn,
                    w.headingBm,
                    w.textEn,
                    w.textBm
                ).joinToString(" ").lowercase()
                keywords.any { kw -> searchable.contains(kw) }
            }
        }
        Log.d(TAG, "After location filter: ${filtered.size} warnings")
        return filtered.mapNotNull { it.toDomain() }
    }

    /** Fetches from API, caches, returns raw WarningResponse list */
    suspend fun fetchAndCacheRaw(): List<WarningResponse> {
        return try {
            Log.d(TAG, "Fetching warnings from API…")
            val response = weatherApiService.getWarnings()
            Log.d(TAG, "API returned ${response.size} raw items")
            if (response.isNotEmpty()) {
                val f = response[0]
                Log.d(TAG, "First: titleEn=${f.warningIssue?.titleEn} validFrom=${f.validFrom} state=${f.state}")
            }
            warningsDao.insert(CachedWarningsEntity(json = gson.toJson(response), fetchedAt = System.currentTimeMillis()))
            response
        } catch (e: Exception) {
            Log.e(TAG, "Fetch failed: ${e.message}", e)
            parseWarningsRaw(warningsDao.get()?.json ?: "[]")
        }
    }

    /** For WorkManager: fetch fresh, return domain warnings unfiltered */
    suspend fun fetchAndCache(): List<Warning> = fetchAndCacheRaw().mapNotNull { it.toDomain() }

    private fun parseWarningsRaw(json: String): List<WarningResponse> {
        return try {
            val type = object : TypeToken<List<WarningResponse>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            Log.e(TAG, "Parse failed: ${e.message}")
            emptyList()
        }
    }

    private fun WarningResponse.toDomain(): Warning? {
        if (validFrom.isNullOrBlank() || validTo.isNullOrBlank()) return null
        // warningIssue.titleEn is the campaign/issue type and may be wrong for co-issued
        // warnings (e.g. a rough-seas record issued under a thunderstorm campaign).
        // The individual warning's actual type is always the first line of headingEn.
        val headingFirst = (headingEn ?: headingBm)
            ?.lines()?.firstOrNull { it.isNotBlank() }?.trim()
        val issueTitleFallback = warningIssue?.titleEn?.trim()
            ?: warningIssue?.titleBm?.trim()
            ?: "Weather Warning"
        val displayTitle = headingFirst?.takeIf { it.isNotBlank() } ?: issueTitleFallback
        return Warning(
            fingerprint = WarningFingerprint.compute(this),
            issued      = warningIssue?.issued.orEmpty(),
            validFrom   = validFrom,
            validTo     = validTo,
            titleEn     = displayTitle,
            headingEn   = headingEn?.ifBlank { headingBm.orEmpty() }.orEmpty(),
            textEn      = textEn?.ifBlank { textBm.orEmpty() }.orEmpty(),
            state       = state,
            district    = district
        )
    }
}
