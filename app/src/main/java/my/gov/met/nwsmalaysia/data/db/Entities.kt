package my.gov.met.nwsmalaysia.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_forecasts")
data class CachedForecastEntity(
    @PrimaryKey val locationId: String,
    val json: String,
    val fetchedAt: Long
)

@Entity(tableName = "cached_warnings")
data class CachedWarningsEntity(
    @PrimaryKey val id: Int = 1,
    val json: String,
    val fetchedAt: Long
)

@Entity(tableName = "notified_warnings")
data class NotifiedWarningEntity(
    @PrimaryKey val fingerprint: String,
    val level: String,
    val notifiedAt: Long,
    val validTo: Long
)

@Entity(tableName = "cached_sigweather")
data class CachedSignifikanEntity(
    @PrimaryKey val id: Int = 1,
    val extractedText: String,
    val fetchedAt: Long
)

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val locationId: String,
    val name: String,
    val type: String,
    val state: String,
    val latitude: Double?,
    val longitude: Double?
)
