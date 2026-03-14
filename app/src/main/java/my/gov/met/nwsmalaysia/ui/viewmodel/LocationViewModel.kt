package my.gov.met.nwsmalaysia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.gov.met.nwsmalaysia.data.repository.LocationRepository
import my.gov.met.nwsmalaysia.domain.model.Location
import my.gov.met.nwsmalaysia.util.UserPreferences
import javax.inject.Inject

data class LocationUiState(
    val results: List<Location> = emptyList(),
    val query: String = "",
    val isDetecting: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            locationRepository.ensureLocationsLoaded()
            search("")
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(query = query) }
        viewModelScope.launch {
            val results = locationRepository.search(query)
            _uiState.update { it.copy(results = results) }
        }
    }

    fun selectLocation(location: Location) {
        viewModelScope.launch {
            userPreferences.saveLocation(
                location.locationId,
                location.name,
                location.state
            )
        }
    }

    fun detectGpsLocation() {
        _uiState.update { it.copy(isDetecting = true, error = null) }
        viewModelScope.launch {
            val location = locationRepository.detectGpsAndFindNearest()
            if (location != null) {
                userPreferences.saveLocation(location.locationId, location.name, location.state)
                _uiState.update { it.copy(isDetecting = false) }
            } else {
                _uiState.update { it.copy(isDetecting = false, error = "Could not detect location. Please select manually.") }
            }
        }
    }
}
