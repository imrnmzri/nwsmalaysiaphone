package my.gov.met.nwsmalaysia.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val ALARM_BROADCAST_ACTION = "my.gov.met.nwsmalaysia.WEATHER_SYNC_ALARM"

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

            val locationId = userPreferences.selectedLocationId.first()
            val location = if (locationId != null) locationRepository.getById(locationId) else null

            val warnings = warningRepository.getWarnings(
                locationState = location?.state,
                locationName = location?.name
            )
            val dao = db.notifiedWarningDao()

            val now = System.currentTimeMillis()
            dao.deleteExpired(now)

            var newCount = 0
            if (notifWarnings) {
                warnings.forEach { warning -> if (dispatchIfNeeded(warning, dao)) newCount++ }
                val activeWarnings = warnings.filter { parseValidTo(it.validTo) >= now }
                notificationHelper.showWarningsSummary(
                    warnings = activeWarnings,
                    locationName = location?.name,
                    hasNewWarning = newCount > 0
                )
            } else {
                notificationHelper.cancelAll()
            }

            scheduleAlarmBackup()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun dispatchIfNeeded(
        warning: Warning,
        dao: my.gov.met.nwsmalaysia.data.db.NotifiedWarningDao
    ): Boolean {
        val existing = dao.getByFingerprint(warning.fingerprint)
        if (existing == null) {
            dao.insert(
                NotifiedWarningEntity(
                    fingerprint = warning.fingerprint,
                    level = "",
                    notifiedAt = System.currentTimeMillis(),
                    validTo = parseValidTo(warning.validTo)
                )
            )
            return true
        }
        return false
    }

    private fun scheduleAlarmBackup() {
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(applicationContext, AlarmReceiver::class.java).apply {
            action = ALARM_BROADCAST_ACTION
        }
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(35)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
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
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val builder = PeriodicWorkRequestBuilder<WeatherSyncWorker>(30, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                builder.setExpedited(OutOfPolicy)
            }

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                builder.build()
            )
        }
    }
}