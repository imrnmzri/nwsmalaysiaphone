package my.gov.met.nwsmalaysia.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedForecastEntity::class,
        CachedWarningsEntity::class,
        NotifiedWarningEntity::class,
        CachedSignifikanEntity::class,
        LocationEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun forecastDao(): ForecastDao
    abstract fun warningsDao(): WarningsDao
    abstract fun notifiedWarningDao(): NotifiedWarningDao
    abstract fun signifikanDao(): SignifikanDao
    abstract fun locationDao(): LocationDao
}
