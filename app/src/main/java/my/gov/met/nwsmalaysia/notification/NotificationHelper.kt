package my.gov.met.nwsmalaysia.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import my.gov.met.nwsmalaysia.MainActivity
import my.gov.met.nwsmalaysia.R
import my.gov.met.nwsmalaysia.domain.model.Warning
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

const val CHANNEL_WARNINGS = "channel_warnings"
private const val SUMMARY_ID = 1

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun createChannels() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_WARNINGS,
                context.getString(R.string.channel_warnings_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_warnings_desc)
                lightColor = android.graphics.Color.parseColor("#E65100")
                enableLights(true)
                enableVibration(true)
            }
        )
    }

    fun showWarningsSummary(
        warnings: List<Warning>,
        locationName: String?,
        hasNewWarning: Boolean
    ) {
        if (warnings.isEmpty()) {
            NotificationManagerCompat.from(context).cancel(SUMMARY_ID)
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (warnings.size == 1) {
            warnings.first().titleEn
        } else {
            "${warnings.size} Active Warnings"
        }

        val contentText = if (locationName != null) {
            if (warnings.size == 1) {
                "$locationName  \u00B7  Until ${formatValidTo(warnings.first().validTo)}"
            } else {
                "${warnings.joinToString(", ") { it.titleEn }} \u00B7 $locationName"
            }
        } else {
            warnings.joinToString(", ") { it.titleEn }
        }

        val inboxStyle = NotificationCompat.InboxStyle().apply {
            warnings.forEach { w ->
                addLine("${w.titleEn}  \u00B7  Until ${formatValidTo(w.validTo)}")
            }
            setSummaryText(contentText)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_WARNINGS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(inboxStyle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(!hasNewWarning)

        if (hasNewWarning) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
            builder.setSilent(true)
        }

        NotificationManagerCompat.from(context).notify(SUMMARY_ID, builder.build())
    }

    fun cancelAll() {
        NotificationManagerCompat.from(context).cancel(SUMMARY_ID)
    }

    private fun formatValidTo(validTo: String): String {
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val output = SimpleDateFormat("h:mm a", Locale.US)
            val date = input.parse(validTo)
            if (date != null) output.format(date) else validTo
        } catch (e: Exception) {
            validTo
        }
    }
}