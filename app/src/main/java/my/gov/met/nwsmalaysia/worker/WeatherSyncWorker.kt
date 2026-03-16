package my.gov.met.nwsmalaysia.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import my.gov.met.nwsmalaysia.data.db.AppDatabase
import my.gov.met.nwsmalaysia.data.db.NotifiedWarningEntity
import my.gov.met.nwsmalaysia.data.repository.LocationRepository
import my.gov.met.nwsmalaysia.data.repository.WarningRepository
import my.gov.met.nwsmalaysia.domain.model.Warning
import my.gov.met.nwsmalaysia.notification.NotificationHelper
import my.gov.met.nwsmalaysia.util.UserPreferences
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class WeatherSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val warningRepository: WarningRepository,
    private val locationRepository: LocationRepository,
    private val notificationHelper: NotificationHelper,
    private val db: AppDatabase,
    private val userPreferences: UserPreferences
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val notifWarnings = userPreferences.notifWarnings.first()

            // Filter warnings for user's selected location via API state/district fields
            val locationId = userPreferences.selectedLocationId.first()
            val location = if (locationId != null) locationRepository.getById(locationId) else null

            val warnings = warningRepository.getWarnings(
                locationState = location?.state,
                locationName = location?.name
            )
            val dao = db.notifiedWarningDao()

            val now = System.currentTimeMillis()
            dao.getExpiredFingerprints(now).forEach { fingerprint ->
                notificationHelper.cancel(fingerprint)
            }
            dao.deleteExpired(now)

            if (notifWarnings) {
                warnings.forEach { warning -> dispatchIfNeeded(warning, dao) }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun dispatchIfNeeded(
        warning: Warning,
        dao: my.gov.met.nwsmalaysia.data.db.NotifiedWarningDao
    ) {
        val existing = dao.getByFingerprint(warning.fingerprint)
        if (existing == null) {
            notificationHelper.notify(warning)
            dao.insert(
                NotifiedWarningEntity(
                    fingerprint = warning.fingerprint,
                    level = "",
                    notifiedAt = System.currentTimeMillis(),
                    validTo = parseValidTo(warning.validTo)
                )
            )
        }
    }

    private fun parseValidTo(validTo: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            sdf.parse(validTo)?.time ?: (System.currentTimeMillis() + 24 * 60 * 60 * 1000L)
        } catch (e: Exception) {
            System.currentTimeMillis() + 24 * 60 * 60 * 1000L
        }
    }

    companion object {
        const val WORK_NAME = "WeatherSyncWork"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<WeatherSyncWorker>(30, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
