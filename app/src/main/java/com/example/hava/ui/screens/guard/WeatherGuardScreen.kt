package com.example.hava.ui.screens.guard

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import com.example.hava.R
import com.example.hava.guard.WeatherGuardScheduler
import com.example.hava.guard.WeatherNotifications
import com.example.hava.data.model.WeatherReport
import com.example.hava.data.remote.dto.City
import com.example.hava.guard.GuardReport
import com.example.hava.guard.GuardRisk
import com.example.hava.guard.RiskFinding
import com.example.hava.guard.RiskLevel
import com.example.hava.guard.RiskMeasurement
import com.example.hava.guard.RiskReason
import com.example.hava.guard.RiskSettings
import com.example.hava.guard.RiskThreshold
import com.example.hava.guard.UserRisk
import com.example.hava.planb.FactorKind
import com.example.hava.planb.MeasurementUnit
import com.example.hava.ui.components.ErrorView
import com.example.hava.ui.components.LoadingView
import com.example.hava.ui.components.WeatherCard
import com.example.hava.ui.components.hourLabel
import com.example.hava.ui.components.oneDecimal
import com.example.hava.ui.screens.city.ErrorType
import com.example.hava.ui.screens.city.messageRes
import kotlin.math.roundToInt

@Composable
fun WeatherGuardRoute(
    city: City,
    report: WeatherReport?,
    isLoading: Boolean,
    errorType: ErrorType?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onChangeCity: () -> Unit,
    onOpenFavorite: (City) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: WeatherGuardViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(city.id, city.latitude, city.longitude, report?.observedAt) {
        viewModel.bind(city, report)
    }
    WeatherGuardScreen(
        cityName = report?.current?.cityName ?: city.name,
        state = state,
        isLoading = isLoading && report == null,
        errorType = if (report == null) errorType else null,
        onRetry = onRetry,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        onChangeCity = onChangeCity,
        onOpenFavorite = onOpenFavorite,
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

@Composable
fun WeatherGuardScreen(
    cityName: String,
    state: GuardUiState,
    isLoading: Boolean,
    errorType: ErrorType?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onChangeCity: () -> Unit,
    onOpenFavorite: (City) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    CompositionLocalProvider(LocalContentColor provides com.example.hava.ui.theme.Ink) {
        Column(modifier = modifier.fillMaxSize().padding(contentPadding)) {
            GuardTopBar(
                title = stringResource(R.string.guard_title),
                action = stringResource(R.string.guard_settings),
                onBack = onBack,
                onAction = onOpenSettings,
            )
            when {
                errorType != null -> ErrorView(message = stringResource(errorType.messageRes()), onRetry = onRetry)
                isLoading || state.report == null -> LoadingView()
                else -> GuardBody(
                    cityName = cityName,
                    observedAt = state.observedAt,
                    settings = state.settings,
                    report = state.report,
                    favorites = state.favorites,
                    onChangeCity = onChangeCity,
                    onOpenFavorite = onOpenFavorite,
                )
            }
        }
    }
}

@Composable
private fun GuardBody(
    cityName: String,
    observedAt: String?,
    settings: RiskSettings,
    report: GuardReport,
    favorites: List<FavoriteGuard>,
    onChangeCity: () -> Unit,
    onOpenFavorite: (City) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "city") {
            Text(
                text = cityName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = observedAt?.let { stringResource(R.string.guard_observed_at, hourLabel(it)) }
                    ?: stringResource(R.string.guard_observed_unknown),
            )
            TextButton(onClick = onChangeCity) { Text(stringResource(R.string.planb_change_city)) }
            Text(
                text = stringResource(R.string.guard_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = LocalContentColor.current.copy(alpha = 0.8f),
            )
        }
        item(key = "summary") { Summary(settings, report) }
        if (report.thunderstorm != null) {
            item(key = "storm") {
                Text(stringResource(R.string.guard_general_safety), fontWeight = FontWeight.SemiBold)
                RiskCard(report.thunderstorm)
            }
        }
        for (finding in report.active) {
            item(key = "active-${finding.risk.name}") { RiskCard(finding) }
        }
        if (report.insufficient.isNotEmpty()) {
            item(key = "missing-title") {
                Text(stringResource(R.string.guard_insufficient_title), fontWeight = FontWeight.SemiBold)
            }
            for (finding in report.insufficient) {
                item(key = "missing-${finding.risk.name}") {
                    WeatherCard(title = stringResource(finding.risk.labelRes())) {
                        Text(stringResource(R.string.guard_missing, joinedFactors(finding.missing)))
                    }
                }
            }
        }
        item(key = "favorites") {
            Text(stringResource(R.string.guard_favorites), fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() })
            if (favorites.isEmpty()) {
                Text(stringResource(R.string.guard_favorites_empty))
            }
            for (favorite in favorites) {
                FavoriteRow(favorite, onOpenFavorite)
            }
        }
    }
}

@Composable
private fun Summary(settings: RiskSettings, report: GuardReport) {
    val text = when {
        settings.enabled.isEmpty() && report.thunderstorm == null -> stringResource(R.string.guard_none_selected)
        report.active.isEmpty() && report.thunderstorm == null && report.insufficient.isEmpty() ->
            stringResource(R.string.guard_no_exceedance)
        report.active.isEmpty() && report.thunderstorm == null && report.clearRisks.isNotEmpty() ->
            stringResource(R.string.guard_partial_clear)
        else -> null
    }
    if (text != null) WeatherCard { Text(text) }
}

@Composable
private fun RiskCard(finding: RiskFinding) {
    val level = finding.level ?: return
    val levelName = stringResource(level.labelRes())
    val riskName = stringResource(finding.risk.labelRes())
    val container = when (level) {
        RiskLevel.CAUTION -> Color(0xFFFFF6E8)
        RiskLevel.HIGH -> Color(0xFFFFEEF0)
    }
    WeatherCard(title = riskName, containerColor = container) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.semantics { contentDescription = "$riskName, $levelName" },
        ) {
            RiskMark(level)
            Text(levelName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.guard_not_official), style = MaterialTheme.typography.bodySmall)
        }
        finding.validAt?.let { Text(stringResource(R.string.guard_valid_at, hourLabel(it))) }
        if (finding.measurements.isNotEmpty()) {
            Text(stringResource(R.string.guard_measurement, joinedMeasurements(finding.measurements)))
        }
        if (finding.thresholds.isNotEmpty()) {
            Text(stringResource(R.string.guard_threshold, joinedThresholds(finding.thresholds)))
        }
        Text(stringResource(finding.reason.actionRes()))
    }
}

@Composable
private fun FavoriteRow(favorite: FavoriteGuard, onOpen: (City) -> Unit) {
    WeatherCard {
        Text(favorite.city.name, fontWeight = FontWeight.SemiBold)
        when {
            favorite.loading -> Text(stringResource(R.string.loading))
            favorite.failed -> Text(stringResource(R.string.guard_favorite_failed))
            favorite.report == null -> Text(stringResource(R.string.loading))
            else -> {
                val active = favorite.report.active + listOfNotNull(favorite.report.thunderstorm)
                if (active.isEmpty()) {
                    Text(stringResource(R.string.guard_no_exceedance))
                } else {
                    val labels = ArrayList<String>(active.size)
                    for (finding in active) labels += stringResource(finding.risk.labelRes())
                    Text(labels.joinToString())
                }
            }
        }
        TextButton(onClick = { onOpen(favorite.city) }) { Text(stringResource(R.string.choose_city)) }
    }
}

@Composable
fun GuardSettingsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: WeatherGuardViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.setNotificationsEnabled(true)
            WeatherGuardScheduler.ensureScheduled(context)
        } else {
            Toast.makeText(context, R.string.guard_notification_denied, Toast.LENGTH_LONG).show()
        }
    }
    val onNotifications: (Boolean) -> Unit = { enabled ->
        if (!enabled) {
            viewModel.setNotificationsEnabled(false)
            WeatherGuardScheduler.cancel(context)
        } else if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (!WeatherNotifications.canPost(context)) {
            Toast.makeText(context, R.string.guard_notification_blocked, Toast.LENGTH_LONG).show()
        } else {
            viewModel.setNotificationsEnabled(true)
            WeatherGuardScheduler.ensureScheduled(context)
        }
    }
    GuardSettingsScreen(
        settings = state.settings,
        notificationsEnabled = state.notificationsEnabled,
        onBack = onBack,
        onEnabled = viewModel::setEnabled,
        onChange = viewModel::save,
        onNotifications = onNotifications,
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

@Composable
fun GuardSettingsScreen(
    settings: RiskSettings,
    notificationsEnabled: Boolean,
    onBack: () -> Unit,
    onEnabled: (UserRisk, Boolean) -> Unit,
    onChange: (RiskSettings) -> Unit,
    onNotifications: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    CompositionLocalProvider(LocalContentColor provides com.example.hava.ui.theme.Ink) {
        Column(modifier = modifier.fillMaxSize().padding(contentPadding)) {
            GuardTopBar(
                title = stringResource(R.string.guard_settings),
                action = null,
                onBack = onBack,
                onAction = {},
            )
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "notifications") {
                    WeatherCard(title = stringResource(R.string.guard_notifications)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.guard_notifications_summary))
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = onNotifications,
                            )
                        }
                    }
                }
                item(key = "toggles") {
                    Text(stringResource(R.string.guard_thresholds_note), style = MaterialTheme.typography.bodySmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(UserRisk.entries, key = { it.name }) { risk ->
                            FilterChip(
                                selected = risk in settings.enabled,
                                onClick = { onEnabled(risk, risk !in settings.enabled) },
                                label = { Text(stringResource(risk.labelRes())) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = com.example.hava.ui.theme.Brand,
                                    selectedLabelColor = Color.White,
                                ),
                            )
                        }
                    }
                }
                if (UserRisk.PRECIPITATION in settings.enabled) {
                    item(key = "precip") {
                        ThresholdGroup(stringResource(R.string.guard_risk_precip)) {
                            IntSlider(stringResource(R.string.guard_slider_prob_caution), settings.precipProbabilityCautionPercent, 20, 90) {
                                onChange(settings.copy(precipProbabilityCautionPercent = it))
                            }
                            IntSlider(stringResource(R.string.guard_slider_prob_high), settings.precipProbabilityHighPercent, 40, 100) {
                                onChange(settings.copy(precipProbabilityHighPercent = it))
                            }
                            DoubleSlider(stringResource(R.string.guard_slider_amount_caution), settings.precipAmountCautionMm, 0.2f, 5f) {
                                onChange(settings.copy(precipAmountCautionMm = it))
                            }
                            DoubleSlider(stringResource(R.string.guard_slider_amount_high), settings.precipAmountHighMm, 1f, 20f) {
                                onChange(settings.copy(precipAmountHighMm = it))
                            }
                        }
                    }
                }
                if (UserRisk.WIND in settings.enabled) {
                    item(key = "wind") {
                        ThresholdGroup(stringResource(R.string.guard_risk_wind)) {
                            DoubleSlider(stringResource(R.string.guard_slider_wind_caution), settings.windCautionKmh, 20f, 70f) {
                                onChange(settings.copy(windCautionKmh = it))
                            }
                            DoubleSlider(stringResource(R.string.guard_slider_wind_high), settings.windHighKmh, 30f, 90f) {
                                onChange(settings.copy(windHighKmh = it))
                            }
                            DoubleSlider(stringResource(R.string.guard_slider_gust_caution), settings.gustCautionKmh, 30f, 90f) {
                                onChange(settings.copy(gustCautionKmh = it))
                            }
                            DoubleSlider(stringResource(R.string.guard_slider_gust_high), settings.gustHighKmh, 40f, 120f) {
                                onChange(settings.copy(gustHighKmh = it))
                            }
                        }
                    }
                }
                if (UserRisk.HEAT in settings.enabled) {
                    item(key = "heat") {
                        ThresholdGroup(stringResource(R.string.guard_risk_heat)) {
                            DoubleSlider(stringResource(R.string.guard_slider_heat_caution), settings.heatCautionC, 28f, 40f) {
                                onChange(settings.copy(heatCautionC = it))
                            }
                            DoubleSlider(stringResource(R.string.guard_slider_heat_high), settings.heatHighC, 32f, 45f) {
                                onChange(settings.copy(heatHighC = it))
                            }
                        }
                    }
                }
                if (UserRisk.FROST in settings.enabled) {
                    item(key = "frost") {
                        ThresholdGroup(stringResource(R.string.guard_risk_frost)) {
                            DoubleSlider(stringResource(R.string.guard_slider_frost_caution), settings.frostCautionC, -2f, 6f) {
                                onChange(settings.copy(frostCautionC = it))
                            }
                            DoubleSlider(stringResource(R.string.guard_slider_frost_high), settings.frostHighC, -8f, 2f) {
                                onChange(settings.copy(frostHighC = it))
                            }
                        }
                    }
                }
                if (UserRisk.UV in settings.enabled) {
                    item(key = "uv") {
                        ThresholdGroup(stringResource(R.string.guard_risk_uv)) {
                            DoubleSlider(stringResource(R.string.guard_slider_uv_caution), settings.uvCaution, 3f, 8f) {
                                onChange(settings.copy(uvCaution = it))
                            }
                            DoubleSlider(stringResource(R.string.guard_slider_uv_high), settings.uvHigh, 6f, 12f) {
                                onChange(settings.copy(uvHigh = it))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThresholdGroup(title: String, content: @Composable () -> Unit) {
    WeatherCard(title = title) { content() }
}

@Composable
private fun IntSlider(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Text("$label: $value")
    Slider(
        value = value.toFloat(),
        onValueChange = { onChange(it.roundToInt().coerceIn(min, max)) },
        valueRange = min.toFloat()..max.toFloat(),
        colors = sliderColors(),
    )
}

@Composable
private fun DoubleSlider(label: String, value: Double, min: Float, max: Float, onChange: (Double) -> Unit) {
    Text("$label: ${oneDecimal(value)}")
    Slider(
        value = value.toFloat().coerceIn(min, max),
        onValueChange = { onChange(it.toDouble().coerceIn(min.toDouble(), max.toDouble())) },
        valueRange = min..max,
        colors = sliderColors(),
    )
}

@Composable
private fun sliderColors() = SliderDefaults.colors()

@Composable
private fun GuardTopBar(title: String, action: String?, onBack: () -> Unit, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        if (action != null) TextButton(onClick = onAction) { Text(action) }
    }
}

@Composable
private fun RiskMark(level: RiskLevel) {
    val color = when (level) {
        RiskLevel.CAUTION -> Color(0xFFFFF176)
        RiskLevel.HIGH -> Color(0xFFFFB4AB)
    }
    Canvas(Modifier.size(14.dp)) {
        if (level == RiskLevel.CAUTION) {
            val path = Path().apply {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, color)
        } else {
            drawRect(color, topLeft = Offset(size.width * 0.15f, size.height * 0.15f), size = Size(size.width * 0.7f, size.height * 0.7f))
        }
    }
}

@Composable
private fun joinedFactors(kinds: List<FactorKind>): String {
    val labels = ArrayList<String>(kinds.size)
    for (kind in kinds) labels += stringResource(kind.guardLabelRes())
    return labels.joinToString()
}

@Composable
private fun joinedMeasurements(values: List<RiskMeasurement>): String {
    val labels = ArrayList<String>(values.size)
    for (value in values) labels += "${stringResource(value.kind.guardLabelRes())} ${formatValue(value.value, value.unit)}"
    return labels.joinToString()
}

@Composable
private fun joinedThresholds(values: List<RiskThreshold>): String {
    val labels = ArrayList<String>(values.size)
    for (value in values) labels += "${stringResource(value.kind.guardLabelRes())} ${formatValue(value.value, value.unit)}"
    return labels.joinToString()
}

private fun formatValue(value: Double, unit: MeasurementUnit): String {
    val number = when (unit) {
        MeasurementUnit.PERCENT, MeasurementUnit.KILOMETERS_PER_HOUR -> value.roundToInt().toString()
        else -> oneDecimal(value)
    }
    val suffix = when (unit) {
        MeasurementUnit.CELSIUS -> " °C"
        MeasurementUnit.PERCENT -> "%"
        MeasurementUnit.MILLIMETER -> " mm"
        MeasurementUnit.KILOMETERS_PER_HOUR -> " km/sa"
        MeasurementUnit.UV_INDEX, MeasurementUnit.NONE -> ""
    }
    return number + suffix
}

private fun GuardRisk.labelRes(): Int = when (this) {
    GuardRisk.PRECIPITATION -> R.string.guard_risk_precip
    GuardRisk.WIND -> R.string.guard_risk_wind
    GuardRisk.GUST -> R.string.guard_risk_gust
    GuardRisk.HEAT -> R.string.guard_risk_heat
    GuardRisk.FROST -> R.string.guard_risk_frost
    GuardRisk.UV -> R.string.guard_risk_uv
    GuardRisk.THUNDERSTORM -> R.string.guard_risk_storm
}

private fun UserRisk.labelRes(): Int = when (this) {
    UserRisk.PRECIPITATION -> R.string.guard_risk_precip
    UserRisk.WIND -> R.string.guard_risk_wind
    UserRisk.HEAT -> R.string.guard_risk_heat
    UserRisk.FROST -> R.string.guard_risk_frost
    UserRisk.UV -> R.string.guard_risk_uv
}

private fun RiskLevel.labelRes(): Int = when (this) {
    RiskLevel.CAUTION -> R.string.guard_level_caution
    RiskLevel.HIGH -> R.string.guard_level_high
}

private fun RiskReason.actionRes(): Int = when (this) {
    RiskReason.PROBABILITY_ONLY -> R.string.guard_action_probability
    RiskReason.AMOUNT_ONLY -> R.string.guard_action_amount
    RiskReason.PROBABILITY_AND_AMOUNT -> R.string.guard_action_both
    RiskReason.WIND_SPEED -> R.string.guard_action_wind
    RiskReason.GUST -> R.string.guard_action_gust
    RiskReason.HEAT -> R.string.guard_action_heat
    RiskReason.FROST -> R.string.guard_action_frost
    RiskReason.UV -> R.string.guard_action_uv
    RiskReason.THUNDERSTORM -> R.string.guard_action_storm
    RiskReason.MISSING_DATA, RiskReason.BELOW_THRESHOLD -> R.string.guard_no_exceedance
}

private fun FactorKind.guardLabelRes(): Int = when (this) {
    FactorKind.PRECIPITATION_PROBABILITY -> R.string.planb_factor_precip_prob
    FactorKind.PRECIPITATION_AMOUNT -> R.string.planb_factor_precip_amount
    FactorKind.TEMPERATURE -> R.string.planb_factor_temperature
    FactorKind.WIND_SPEED -> R.string.planb_factor_wind
    FactorKind.WIND_GUSTS -> R.string.planb_factor_gust
    FactorKind.UV_INDEX -> R.string.planb_factor_uv
    FactorKind.WEATHER_CODE -> R.string.planb_factor_code
    FactorKind.WIND_DIRECTION -> R.string.planb_factor_direction
    FactorKind.AIR_QUALITY -> R.string.planb_factor_aqi
    FactorKind.WAVE_HEIGHT -> R.string.planb_factor_wave
}
