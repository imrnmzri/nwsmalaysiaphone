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
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

const val CHANNEL_WARNINGS = "channel_warnings"

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

    fun notify(warning: Warning) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val endDate = formatValidTo(warning.validTo)
        val notification = NotificationCompat.Builder(context, CHANNEL_WARNINGS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(warning.titleEn)
            .setContentText("Alert in effect until $endDate")
            .setStyle(NotificationCompat.BigTextStyle().bigText(warning.headingEn))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notifId = Math.abs(warning.fingerprint.hashCode())
        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    fun cancel(fingerprint: String) {
        val notifId = Math.abs(fingerprint.hashCode())
        NotificationManagerCompat.from(context).cancel(notifId)
    }

    private fun formatValidTo(validTo: String): String {
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val output = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.US)
            val date = input.parse(validTo)
            if (date != null) output.format(date) else validTo
        } catch (e: Exception) {
            validTo
        }
    }
}
