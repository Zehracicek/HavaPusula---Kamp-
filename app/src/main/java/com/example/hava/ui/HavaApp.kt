package com.example.hava.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hava.R
import com.example.hava.data.repository.hasLocationPermission
import com.example.hava.guard.GuardLaunchRequest
import com.example.hava.ui.screens.city.CityRoute
import com.example.hava.ui.screens.guard.GuardSettingsRoute
import com.example.hava.ui.screens.guard.WeatherGuardRoute
import com.example.hava.ui.screens.guard.WeatherGuardViewModel
import com.example.hava.ui.screens.planb.PlanBRoute
import com.example.hava.ui.screens.weather.WeatherScreen
import com.example.hava.ui.screens.weather.WeatherUiState
import com.example.hava.ui.screens.weather.WeatherViewModel
import com.example.hava.ui.theme.Brand
import com.example.hava.ui.theme.BrandSoft
import com.example.hava.ui.theme.Muted
import com.example.hava.ui.theme.PageBackground

private val LocationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

private enum class Destination { WEATHER, PLAN_B, WEATHER_GUARD, GUARD_SETTINGS, CITIES }

@Composable
fun HavaApp(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    weatherViewModel: WeatherViewModel = viewModel(factory = WeatherViewModel.Factory),
    guardViewModel: WeatherGuardViewModel = viewModel(factory = WeatherGuardViewModel.Factory),
    guardLaunch: GuardLaunchRequest? = null,
) {
    val weatherState by weatherViewModel.uiState.collectAsStateWithLifecycle()
    val scene by weatherViewModel.scene.collectAsStateWithLifecycle()
    val selectedCityId by weatherViewModel.selectedCityId.collectAsStateWithLifecycle()
    val currentCity by weatherViewModel.currentCity.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var destination by rememberSaveable { mutableStateOf(Destination.WEATHER) }
    var resumeAfterCities by rememberSaveable { mutableStateOf(Destination.WEATHER) }
    var resumeAfterSettings by rememberSaveable { mutableStateOf(Destination.WEATHER) }
    var locationPrompted by rememberSaveable { mutableStateOf(false) }
    var locationReturnsToWeather by rememberSaveable { mutableStateOf(true) }
    // Ekranlar arasında gidip gelirken kaydırma konumları korunur
    val stateHolder = rememberSaveableStateHolder()

    val requestLocation = {
        destination = if (locationReturnsToWeather) Destination.WEATHER else resumeAfterCities
        weatherViewModel.onMyLocationRequested()
    }
    val manualPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) {
            requestLocation()
        } else {
            Toast.makeText(context, R.string.location_permission_denied, Toast.LENGTH_LONG).show()
        }
    }
    // İlk açılıştaki otomatik istekte reddedilirse varsayılan şehir gösterilmeye devam eder
    val startupPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it } && guardLaunch == null) {
            locationReturnsToWeather = true
            requestLocation()
        }
    }
    val onMyLocationClick = {
        locationReturnsToWeather = true
        if (context.hasLocationPermission()) requestLocation() else manualPermissionLauncher.launch(LocationPermissions)
    }
    val onMyLocationFromCities = {
        locationReturnsToWeather = false
        if (context.hasLocationPermission()) requestLocation() else manualPermissionLauncher.launch(LocationPermissions)
    }

    val launch = guardLaunch
    var appliedLaunchToken by remember { mutableStateOf<Long?>(null) }
    if (launch != null && appliedLaunchToken != launch.token) {
        appliedLaunchToken = launch.token
        weatherViewModel.onCitySelected(launch.city)
        destination = Destination.WEATHER_GUARD
    }

    LaunchedEffect(Unit) {
        if (guardLaunch != null) return@LaunchedEffect
        if (!locationPrompted && weatherViewModel.isFreshStart && !context.hasLocationPermission()) {
            locationPrompted = true
            startupPermissionLauncher.launch(LocationPermissions)
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { weatherViewModel.refreshIfStale() }

    BackHandler(enabled = destination != Destination.WEATHER) {
        destination = when (destination) {
            Destination.CITIES -> resumeAfterCities
            Destination.GUARD_SETTINGS -> resumeAfterSettings
            else -> Destination.WEATHER
        }
    }

    val openCities = { from: Destination ->
        resumeAfterCities = from
        destination = Destination.CITIES
    }
    val openSettings = { from: Destination ->
        if (from != Destination.GUARD_SETTINGS) resumeAfterSettings = from
        destination = Destination.GUARD_SETTINGS
    }
    val showBar = destination != Destination.CITIES
    val screenPadding = PaddingValues(
        top = contentPadding.calculateTopPadding(),
        bottom = if (showBar) 0.dp else contentPadding.calculateBottomPadding(),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PageBackground),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = destination,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    if (forward) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut())
                    } else {
                        (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "destination",
            ) { target ->
                stateHolder.SaveableStateProvider(target.name) {
                    when (target) {
                        Destination.WEATHER -> WeatherScreen(
                            state = weatherState,
                            scene = scene,
                            onRetry = weatherViewModel::retry,
                            onMyLocationClick = onMyLocationClick,
                            onCitiesClick = { openCities(Destination.WEATHER) },
                            onPlanBClick = { destination = Destination.PLAN_B },
                            onGuardClick = { destination = Destination.WEATHER_GUARD },
                            onSettingsClick = { openSettings(Destination.WEATHER) },
                            contentPadding = screenPadding,
                        )

                        Destination.PLAN_B -> {
                            val success = weatherState as? WeatherUiState.Success
                            PlanBRoute(
                                cityId = currentCity.id,
                                cityName = success?.report?.current?.cityName ?: currentCity.name,
                                report = success?.report,
                                isLoading = weatherState is WeatherUiState.Loading,
                                errorType = (weatherState as? WeatherUiState.Error)?.type,
                                onRetry = weatherViewModel::retry,
                                onBack = { destination = Destination.WEATHER },
                                onChangeCity = { openCities(Destination.PLAN_B) },
                                contentPadding = screenPadding,
                            )
                        }

                        Destination.WEATHER_GUARD -> {
                            val success = weatherState as? WeatherUiState.Success
                            WeatherGuardRoute(
                                city = currentCity,
                                report = success?.report,
                                isLoading = weatherState is WeatherUiState.Loading,
                                errorType = (weatherState as? WeatherUiState.Error)?.type,
                                onRetry = weatherViewModel::retry,
                                onBack = { destination = Destination.WEATHER },
                                onOpenSettings = { openSettings(Destination.WEATHER_GUARD) },
                                onChangeCity = { openCities(Destination.WEATHER_GUARD) },
                                onOpenFavorite = { city ->
                                    weatherViewModel.onCitySelected(city)
                                },
                                contentPadding = screenPadding,
                                viewModel = guardViewModel,
                            )
                        }

                        Destination.GUARD_SETTINGS -> GuardSettingsRoute(
                            onBack = { destination = resumeAfterSettings },
                            onManageCities = { openCities(Destination.GUARD_SETTINGS) },
                            onMyLocationClick = onMyLocationClick,
                            contentPadding = screenPadding,
                            viewModel = guardViewModel,
                        )

                        Destination.CITIES -> CityRoute(
                            selectedCityId = selectedCityId,
                            onCitySelected = { city ->
                                weatherViewModel.onCitySelected(city)
                                destination = resumeAfterCities
                            },
                            onMyLocationClick = onMyLocationFromCities,
                            onBack = { destination = resumeAfterCities },
                            contentPadding = screenPadding,
                        )
                    }
                }
            }
        }
        if (showBar) {
            AppBottomBar(
                destination = destination,
                bottomInset = contentPadding,
                onHome = { destination = Destination.WEATHER },
                onPlanB = { destination = Destination.PLAN_B },
                onGuard = { destination = Destination.WEATHER_GUARD },
                onMore = { openSettings(destination) },
            )
        }
    }
}

@Composable
private fun AppBottomBar(
    destination: Destination,
    bottomInset: PaddingValues,
    onHome: () -> Unit,
    onPlanB: () -> Unit,
    onGuard: () -> Unit,
    onMore: () -> Unit,
) {
    Surface(color = Color.White, shadowElevation = 12.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = bottomInset.calculateBottomPadding())
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val home = destination == Destination.WEATHER
            val guard = destination == Destination.WEATHER_GUARD
            BarItem(stringResource(R.string.nav_home), if (home) Icons.Filled.Home else Icons.Outlined.Home, home, onHome)
            BarItem(stringResource(R.string.planb_open), Icons.AutoMirrored.Filled.DirectionsWalk, destination == Destination.PLAN_B, onPlanB)
            BarItem(stringResource(R.string.guard_open), if (guard) Icons.Filled.Shield else Icons.Outlined.Shield, guard, onGuard)
            BarItem(stringResource(R.string.nav_more), Icons.Filled.MoreHoriz, destination == Destination.GUARD_SETTINGS, onMore)
        }
    }
}

@Composable
private fun RowScope.BarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) Brand else Muted
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) BrandSoft else Color.Transparent)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = color,
            maxLines = 1,
        )
    }
}
