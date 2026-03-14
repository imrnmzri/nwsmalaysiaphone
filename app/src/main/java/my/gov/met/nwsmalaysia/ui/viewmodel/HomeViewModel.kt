package my.gov.met.nwsmalaysia.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.gov.met.nwsmalaysia.data.repository.LocationRepository
import my.gov.met.nwsmalaysia.data.repository.SignifikanRepository
import my.gov.met.nwsmalaysia.data.repository.WarningRepository
import my.gov.met.nwsmalaysia.data.repository.WeatherRepository
import my.gov.met.nwsmalaysia.domain.model.*
import my.gov.met.nwsmalaysia.util.UserPreferences
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val warningRepository: WarningRepository,
    private val signifikanRepository: SignifikanRepository,
    private val locationRepository: LocationRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        WeatherState(
            location = null,
            currentConditions = null,
            hourlyForecasts = emptyList(),
            dailyForecasts = emptyList(),
            warnings = emptyList(),
            signifikanText = null,
            isLoading = true,
            error = null
        )
    )
    val uiState: StateFlow<WeatherState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Must complete BEFORE collect fires its first emission,
            // otherwise getById() returns null for a freshly-selected location.
            locationRepository.ensureLocationsLoaded()

            // Now safe to react to location changes (first emission comes immediately)
            userPreferences.selectedLocationId.collect { locationId ->
                loadAll(locationId)
            }
        }
    }

    /** Called by pull-to-refresh */
    fun refresh() {
        viewModelScope.launch {
            loadAll(userPreferences.selectedLocationId.first())
        }
    }

    private suspend fun loadAll(locationId: String?) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            // 1. Resolve Location object from DB
            val location = if (locationId != null) locationRepository.getById(locationId) else null
            Log.d(TAG, "loadAll: locationId='$locationId' location=${location?.name} state='${location?.state}'")

            // 2. Filter warnings by location using API state/district fields
            val warnings = warningRepository.getWarnings(
                locationState = location?.state,
                locationName = location?.name
            )
            Log.d(TAG, "loadAll: got ${warnings.size} warnings for state='${location?.state}' name='${location?.name}'")

            // 3. Fetch Signifikan advisory text via ML Kit OCR
            _uiState.update { it.copy(signifikanLoading = true) }
            val signifikanText = runCatching {
                signifikanRepository.getSignifikanText()
            }.getOrNull()

            // 4. Fetch current conditions + hourly/daily from Open-Meteo (only if we have coords)
            val lat = location?.latitude
            val lon = location?.longitude
            val condData = if (lat != null && lon != null) {
                runCatching { weatherRepository.getCurrentConditions(lat, lon) }.getOrNull()
            } else null

            _uiState.update { current ->
                current.copy(
                    location = location,
                    currentConditions = condData?.current,
                    hourlyForecasts = condData?.hourly ?: emptyList(),
                    dailyForecasts = condData?.daily ?: emptyList(),
                    warnings = warnings,
                    signifikanText = signifikanText,
                    signifikanLoading = false,
                    isLoading = false,
                    error = null
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }
}
