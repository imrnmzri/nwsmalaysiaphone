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
            locationRepository.ensureLocationsLoaded()
            userPreferences.selectedLocationId.collect { locationId ->
                loadAll(locationId)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loadAll(userPreferences.selectedLocationId.first())
        }
    }

    private suspend fun loadAll(locationId: String?) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val location = if (locationId != null) locationRepository.getById(locationId) else null
            Log.d(TAG, "loadAll: locationId='$locationId' location=${location?.name}")

            val warnings = warningRepository.getWarnings(
                locationState = location?.state,
                locationName = location?.name
            )

            _uiState.update { it.copy(signifikanLoading = true) }
            val signifikanText = runCatching {
                signifikanRepository.getSignifikanText()
            }.getOrNull()

            // Fetch gov API forecast for 7-day display (works for all locations with locationId)
            val apiForecasts = if (locationId != null) {
                runCatching { weatherRepository.getForecast(locationId) }.getOrNull() ?: emptyList()
            } else emptyList()

            // Fetch Open-Meteo current conditions + hourly (only when coords available)
            val lat = location?.latitude
            val lon = location?.longitude
            val condData = if (lat != null && lon != null) {
                runCatching { weatherRepository.getCurrentConditions(lat, lon) }.getOrNull()
            } else null

            // Build daily forecast: prefer gov API data, fall back to Open-Meteo
            val dailyForecasts = if (apiForecasts.isNotEmpty()) {
                weatherRepository.buildDailyForecastsFromApi(apiForecasts)
            } else {
                condData?.daily ?: emptyList()
            }

            _uiState.update { current ->
                current.copy(
                    location = location,
                    currentConditions = condData?.current,
                    hourlyForecasts = condData?.hourly ?: emptyList(),
                    dailyForecasts = dailyForecasts,
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
