package my.gov.met.nwsmalaysia

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import my.gov.met.nwsmalaysia.notification.NotificationHelper
import my.gov.met.nwsmalaysia.worker.WeatherSyncWorker
import javax.inject.Inject

@HiltAndroidApp
class NwsApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createChannels()
        WeatherSyncWorker.enqueue(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
