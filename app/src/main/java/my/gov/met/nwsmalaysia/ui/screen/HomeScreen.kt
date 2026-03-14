package my.gov.met.nwsmalaysia.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import my.gov.met.nwsmalaysia.domain.model.Location
import my.gov.met.nwsmalaysia.domain.model.Warning
import my.gov.met.nwsmalaysia.ui.component.CurrentWeatherCard
import my.gov.met.nwsmalaysia.ui.component.DailyForecastCard
import my.gov.met.nwsmalaysia.ui.component.HourlyForecastRow
import my.gov.met.nwsmalaysia.ui.component.WarningCard
import my.gov.met.nwsmalaysia.ui.viewmodel.HomeViewModel
import my.gov.met.nwsmalaysia.ui.viewmodel.LocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    onWarningDetailClick: (Warning) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    locationViewModel: LocationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val locationState by locationViewModel.uiState.collectAsState()

    var searchActive by remember { mutableStateOf(false) }

    // When GPS detection finishes successfully, close the search bar and reload weather
    var wasDetecting by remember { mutableStateOf(false) }
    LaunchedEffect(locationState.isDetecting) {
        if (wasDetecting && !locationState.isDetecting && locationState.error == null) {
            searchActive = false
            viewModel.refresh()
        }
        wasDetecting = locationState.isDetecting
    }

    Scaffold { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Google Weather-style search bar ──────────────────────────────
            DockedSearchBar(
                query = locationState.query,
                onQueryChange = { locationViewModel.search(it) },
                onSearch = { },
                active = searchActive,
                onActiveChange = { active ->
                    searchActive = active
                    if (active) locationViewModel.search("")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = {
                    Text(uiState.location?.name ?: "Search location…")
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchActive) {
                        IconButton(onClick = {
                            if (locationState.query.isNotEmpty()) locationViewModel.search("")
                            else searchActive = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    } else {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            ) {
                // ── Search results (shown when bar is active) ────────────────
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    // GPS button
                    item {
                        ListItem(
                            leadingContent = {
                                if (locationState.isDetecting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.MyLocation, contentDescription = null)
                                }
                            },
                            headlineContent = {
                                Text(
                                    if (locationState.isDetecting) "Detecting…" else "Use my location"
                                )
                            },
                            modifier = Modifier.clickable(enabled = !locationState.isDetecting) {
                                locationViewModel.detectGpsLocation()
                            }
                        )
                        HorizontalDivider()
                    }

                    // Error feedback
                    if (locationState.error != null) {
                        item {
                            Text(
                                text = locationState.error!!,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Results grouped by state
                    val grouped = locationState.results.groupBy { it.state }
                    grouped.forEach { (state, locations) ->
                        item {
                            Text(
                                text = state,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp, vertical = 6.dp
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(locations) { location ->
                            SearchResultRow(
                                location = location,
                                onClick = {
                                    locationViewModel.selectLocation(location)
                                    searchActive = false
                                    viewModel.refresh()
                                }
                            )
                        }
                    }
                }
            }

            // ── Weather content ──────────────────────────────────────────────
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.isLoading && uiState.location == null && uiState.warnings.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val conditions = uiState.currentConditions
                        if (conditions != null) {
                            CurrentWeatherCard(
                                conditions = conditions,
                                locationName = uiState.location?.name ?: ""
                            )
                        } else if (uiState.location != null && uiState.location?.latitude == null) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "No GPS coordinates for ${uiState.location?.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "Current conditions available for locations with coordinates.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }

                        WarningsSection(
                            warnings = uiState.warnings,
                            onDetailClick = onWarningDetailClick
                        )

                        if (uiState.hourlyForecasts.isNotEmpty()) {
                            HourlyForecastRow(forecasts = uiState.hourlyForecasts)
                        }

                        if (uiState.dailyForecasts.isNotEmpty()) {
                            DailyForecastCard(forecasts = uiState.dailyForecasts)
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(location: Location, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(location.name) },
        supportingContent = {
            Text(
                text = location.type,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        },
        trailingContent = {
            if (location.latitude != null) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Has coordinates",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}

@Composable
private fun WarningsSection(
    warnings: List<Warning>,
    onDetailClick: (Warning) -> Unit
) {
    when {
        warnings.isEmpty() -> { /* render nothing */ }
        warnings.size == 1 -> {
            WarningCard(
                warning = warnings[0],
                onDetailClick = { onDetailClick(warnings[0]) }
            )
        }
        else -> {
            var expanded by remember { mutableStateOf(true) }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${warnings.size} Active Warnings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                AnimatedVisibility(visible = expanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        warnings.forEach { warning ->
                            WarningCard(
                                warning = warning,
                                onDetailClick = { onDetailClick(warning) }
                            )
                        }
                    }
                }
            }
        }
    }
}
