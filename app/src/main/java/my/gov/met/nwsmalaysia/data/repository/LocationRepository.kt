package my.gov.met.nwsmalaysia.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import my.gov.met.nwsmalaysia.data.db.LocationDao
import my.gov.met.nwsmalaysia.data.db.LocationEntity
import my.gov.met.nwsmalaysia.domain.model.Location
import my.gov.met.nwsmalaysia.util.CsvParser
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.math.*

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationDao: LocationDao
) {

    suspend fun ensureLocationsLoaded() {
        if (locationDao.count() == 0) {
            val entities = CsvParser.parseLocations(context)
            locationDao.insertAll(entities)
        }
    }

    suspend fun search(query: String): List<Location> {
        return if (query.isBlank()) {
            locationDao.getAll().map { it.toDomain() }
        } else {
            locationDao.search(query).map { it.toDomain() }
        }
    }

    suspend fun getById(id: String): Location? {
        return locationDao.getById(id)?.toDomain()
    }

    /**
     * Gets device GPS fix then finds the nearest location in the DB with known coordinates.
     * Uses two sequential suspends so there is no runBlocking inside a coroutine.
     */
    @SuppressLint("MissingPermission")
    suspend fun detectGpsAndFindNearest(): Location? {
        val androidLocation = getGpsFix() ?: return null
        val candidates = locationDao.getAllWithCoords()
        return candidates.minByOrNull {
            haversine(androidLocation.latitude, androidLocation.longitude, it.latitude!!, it.longitude!!)
        }?.toDomain()
    }

    @SuppressLint("MissingPermission")
    private suspend fun getGpsFix(): android.location.Location? {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        // Try last known location first — instant, no GPS warm-up needed
        val lastLocation: android.location.Location? = suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { loc -> cont.resume(loc) }
                .addOnFailureListener { cont.resume(null) }
        }
        if (lastLocation != null) return lastLocation

        // Fall back to a fresh fix with high accuracy and a generous age window
        return suspendCancellableCoroutine { cont ->
            val request = com.google.android.gms.location.CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(300_000) // accept fixes up to 5 min old
                .setDurationMillis(15_000)      // wait up to 15 s for a fresh fix
                .build()
            fusedClient.getCurrentLocation(request, null)
                .addOnSuccessListener { loc -> cont.resume(loc) }
                .addOnFailureListener { cont.resume(null) }
        }
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun LocationEntity.toDomain() = Location(
        locationId = locationId,
        name = name,
        type = type,
        state = state,
        latitude = latitude,
        longitude = longitude
    )
}
