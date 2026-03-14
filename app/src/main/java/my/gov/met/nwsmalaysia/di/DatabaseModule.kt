package my.gov.met.nwsmalaysia.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import my.gov.met.nwsmalaysia.data.db.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "nws_malaysia.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideForecastDao(db: AppDatabase): ForecastDao = db.forecastDao()

    @Provides
    fun provideWarningsDao(db: AppDatabase): WarningsDao = db.warningsDao()

    @Provides
    fun provideNotifiedWarningDao(db: AppDatabase): NotifiedWarningDao = db.notifiedWarningDao()

    @Provides
    fun provideSignifikanDao(db: AppDatabase): SignifikanDao = db.signifikanDao()

    @Provides
    fun provideLocationDao(db: AppDatabase): LocationDao = db.locationDao()
}
