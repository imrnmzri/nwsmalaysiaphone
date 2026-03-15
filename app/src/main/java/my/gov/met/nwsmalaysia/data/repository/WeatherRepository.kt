package my.gov.met.nwsmalaysia.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import my.gov.met.nwsmalaysia.data.api.OpenMeteoApiService
import my.gov.met.nwsmalaysia.data.api.WeatherApiService
import my.gov.met.nwsmalaysia.data.db.CachedForecastEntity
import my.gov.met.nwsmalaysia.data.db.ForecastDao
import my.gov.met.nwsmalaysia.data.model.ForecastResponse
import my.gov.met.nwsmalaysia.data.model.OpenMeteoResponse
import my.gov.met.nwsmalaysia.domain.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private const val CACHE_TTL_MS = 30 * 60 * 1000L  // 30 minutes

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val openMeteoApiService: OpenMeteoApiService,
    private val forecastDao: ForecastDao,
    private val gson: Gson
) {

    /**
     * Fetches 7-day forecast from the gov API for a specific location.
     * Applies client-side filtering by locationId in case the API returns all locations.
     */
    suspend fun getForecast(locationId: String): List<Forecast> {
        val cached = forecastDao.getForLocation(locationId)
        val now = System.currentTimeMillis()
        if (cached != null && now - cached.fetchedAt < CACHE_TTL_MS) {
            return parseForecastJson(cached.json)
        }
        return try {
            val response = weatherApiService.getForecast(locationId)
            // Filter to selected location, then deduplicate by date (API returns multiple records per date)
            val filtered = response
                .filter { it.location.locationId == locationId }
                .distinctBy { it.date }
            val json = gson.toJson(filtered)
            forecastDao.insert(CachedForecastEntity(locationId, json, now))
            filtered.map { it.toDomain() }
        } catch (e: Exception) {
            cached?.let { parseForecastJson(it.json) } ?: emptyList()
        }
    }

    /**
     * Fetches current conditions + hourly from Open-Meteo using coordinates.
     */
    suspend fun getCurrentConditions(lat: Double, lon: Double): CurrentConditionsData? {
        return try {
            val response = openMeteoApiService.getCurrent(
                lat = lat,
                lon = lon,
                current = "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m,precipitation,apparent_temperature",
                hourly = "temperature_2m,weather_code,precipitation_probability",
                daily = "weather_code,temperature_2m_max,temperature_2m_min"
            )
            response.toCurrentConditionsData()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Converts gov API Forecast list into DailyForecast list using the same model
     * as Open-Meteo daily, so the UI stays identical. The WMO code is derived from
     * the summary_forecast text; temperatures come from the gov API.
     */
    fun buildDailyForecastsFromApi(apiForecasts: List<Forecast>): List<DailyForecast> {
        return apiForecasts
            .sortedBy { it.date }
            .map { f ->
                DailyForecast(
                    date = f.date,
                    weatherCode = summaryTextToWmoCode(f.summaryForecast),
                    maxTemp = f.maxTemp.toDouble(),
                    minTemp = f.minTemp.toDouble()
                )
            }
    }

    private fun summaryTextToWmoCode(text: String): Int {
        val t = text.lowercase()
        return when {
            "ribut petir" in t -> 95
            "hujan lebat" in t -> 65
            "hujan" in t       -> 63
            "berjerebu" in t   -> 45
            "berawan" in t     -> 3
            "berpanas" in t    -> 1
            "tiada hujan" in t -> 0
            else               -> 2
        }
    }

    private fun parseForecastJson(json: String): List<Forecast> {
        return try {
            val type = object : TypeToken<List<ForecastResponse>>() {}.type
            val list: List<ForecastResponse> = gson.fromJson(json, type)
            list.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun ForecastResponse.toDomain() = Forecast(
        date = date,
        locationId = location.locationId,
        morningForecast = morningForecast,
        afternoonForecast = afternoonForecast,
        nightForecast = nightForecast,
        summaryForecast = summaryForecast,
        summaryWhen = summaryWhen,
        minTemp = minTemp,
        maxTemp = maxTemp
    )

    private fun OpenMeteoResponse.toCurrentConditionsData(): CurrentConditionsData? {
        val c = current ?: return null
        return CurrentConditionsData(
            current = CurrentConditions(
                temperature = c.temperature ?: 0.0,
                apparentTemperature = c.apparentTemperature ?: 0.0,
                humidity = c.relativeHumidity ?: 0,
                weatherCode = c.weatherCode ?: 0,
                windSpeed = c.windSpeed ?: 0.0,
                windDirection = c.windDirection ?: 0,
                precipitation = c.precipitation ?: 0.0
            ),
            hourly = buildHourlyList(),
            daily = buildMeteoDaily()
        )
    }

    private fun OpenMeteoResponse.buildHourlyList(): List<HourlyForecast> {
        val h = hourly ?: return emptyList()
        val times = h.time ?: return emptyList()
        val now = LocalDateTime.now()
        val startIndex = times.indexOfFirst { timeStr ->
            try {
                val t = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                !t.isBefore(now.withMinute(0).withSecond(0).withNano(0))
            } catch (e: Exception) { false }
        }.takeIf { it >= 0 } ?: 0
        return (startIndex until times.size).take(12).mapNotNull { i ->
            val time = times.getOrNull(i) ?: return@mapNotNull null
            HourlyForecast(
                time = time,
                temperature = h.temperature?.getOrNull(i) ?: return@mapNotNull null,
                weatherCode = h.weatherCode?.getOrNull(i) ?: 0,
                precipProbability = h.precipitationProbability?.getOrNull(i) ?: 0
            )
        }
    }

    private fun OpenMeteoResponse.buildMeteoDaily(): List<DailyForecast> {
        val d = daily ?: return emptyList()
        val dates = d.time ?: return emptyList()
        return dates.indices.mapNotNull { i ->
            val date = dates.getOrNull(i) ?: return@mapNotNull null
            DailyForecast(
                date = date,
                weatherCode = d.weatherCode?.getOrNull(i) ?: 0,
                maxTemp = d.temperatureMax?.getOrNull(i) ?: 0.0,
                minTemp = d.temperatureMin?.getOrNull(i) ?: 0.0
            )
        }
    }
}

data class CurrentConditionsData(
    val current: CurrentConditions,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>
)
