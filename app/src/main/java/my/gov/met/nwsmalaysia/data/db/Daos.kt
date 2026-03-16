package my.gov.met.nwsmalaysia.data.db

import androidx.room.*

@Dao
interface ForecastDao {
    @Query("SELECT * FROM cached_forecasts WHERE locationId = :locationId")
    suspend fun getForLocation(locationId: String): CachedForecastEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CachedForecastEntity)
}

@Dao
interface WarningsDao {
    @Query("SELECT * FROM cached_warnings WHERE id = 1")
    suspend fun get(): CachedWarningsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CachedWarningsEntity)
}

@Dao
interface NotifiedWarningDao {
    @Query("SELECT * FROM notified_warnings WHERE fingerprint = :fingerprint")
    suspend fun getByFingerprint(fingerprint: String): NotifiedWarningEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotifiedWarningEntity)

    @Query("SELECT fingerprint FROM notified_warnings WHERE validTo < :now")
    suspend fun getExpiredFingerprints(now: Long): List<String>

    @Query("DELETE FROM notified_warnings WHERE validTo < :now")
    suspend fun deleteExpired(now: Long)
}

@Dao
interface SignifikanDao {
    @Query("SELECT * FROM cached_sigweather WHERE id = 1")
    suspend fun get(): CachedSignifikanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CachedSignifikanEntity)
}

@Dao
interface LocationDao {
    @Query("SELECT COUNT(*) FROM locations")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<LocationEntity>)

    // All locations, regardless of coordinates
    @Query("SELECT * FROM locations ORDER BY state, name")
    suspend fun getAll(): List<LocationEntity>

    @Query("SELECT * FROM locations WHERE locationId = :id")
    suspend fun getById(id: String): LocationEntity?

    @Query("SELECT * FROM locations WHERE name LIKE '%' || :query || '%' ORDER BY state, name LIMIT 200")
    suspend fun search(query: String): List<LocationEntity>

    @Query("SELECT * FROM locations WHERE latitude IS NOT NULL AND longitude IS NOT NULL")
    suspend fun getAllWithCoords(): List<LocationEntity>
}
