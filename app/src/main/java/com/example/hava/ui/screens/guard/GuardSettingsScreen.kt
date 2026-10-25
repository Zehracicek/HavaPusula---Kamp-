package com.example.hava.ui.screens.guard

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hava.R
import com.example.hava.guard.GuardRisk
import com.example.hava.guard.RiskSettings
import com.example.hava.guard.UserRisk
import com.example.hava.guard.WeatherGuardScheduler
import com.example.hava.guard.WeatherNotifications
import com.example.hava.ui.components.ChevronIcon
import com.example.hava.ui.components.HavaCard
import com.example.hava.ui.components.ScreenTopBar
import com.example.hava.ui.components.SectionTitle
import com.example.hava.ui.components.oneDecimal
import com.example.hava.ui.components.percentTr
import com.example.hava.ui.theme.Brand
import com.example.hava.ui.theme.CardBorder
import com.example.hava.ui.theme.Ink
import com.example.hava.ui.theme.Muted
import kotlin.math.roundToInt

private enum class ThresholdGroup { PRECIPITATION, WIND, HEAT, FROST, UV }

@Composable
fun GuardSettingsRoute(
    onBack: () -> Unit,
    onManageCities: () -> Unit,
    onMyLocationClick: () -> Unit,
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
        onManageCities = onManageCities,
        onMyLocationClick = onMyLocationClick,
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
    onManageCities: () -> Unit,
    onMyLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    var editingName by rememberSaveable { mutableStateOf<String?>(null) }
    Column(modifier = modifier.fillMaxSize().padding(contentPadding)) {
        ScreenTopBar(title = stringResource(R.string.settings_title), onBack = onBack)
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "risk-title") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionTitle(stringResource(R.string.settings_risk_prefs))
                    Text(stringResource(R.string.settings_risk_question), style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
            item(key = "risks") {
                HavaCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp), spacing = 0.dp) {
                    for (risk in UserRisk.entries) {
                        ToggleRow(
                            icon = risk.guardRisk().style().icon,
                            title = stringResource(risk.settingsLabelRes()),
                            checked = risk in settings.enabled,
                            onCheckedChange = { onEnabled(risk, it) },
                        )
                        HorizontalDivider(color = CardBorder)
                    }
                    ToggleRow(
                        icon = GuardRisk.THUNDERSTORM.style().icon,
                        title = stringResource(R.string.settings_risk_storm),
                        caption = stringResource(R.string.settings_storm_locked),
                        checked = true,
                        enabled = false,
                        onCheckedChange = {},
                    )
                }
            }
            item(key = "thresholds-title") {
                SectionTitle(stringResource(R.string.settings_thresholds), modifier = Modifier.padding(top = 6.dp))
            }
            item(key = "thresholds") {
                HavaCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp), spacing = 0.dp) {
                    val rows = listOf(
                        Triple(ThresholdGroup.PRECIPITATION, R.string.settings_th_precip, percentTr(settings.precipProbabilityCautionPercent)),
                        Triple(ThresholdGroup.WIND, R.string.settings_th_wind, "${settings.windCautionKmh.roundToInt()} km/sa"),
                        Triple(ThresholdGroup.HEAT, R.string.settings_th_heat, "${wholeOrDecimal(settings.heatCautionC)}°C"),
                        Triple(ThresholdGroup.FROST, R.string.settings_th_frost, "${wholeOrDecimal(settings.frostCautionC)}°C"),
                        Triple(ThresholdGroup.UV, R.string.settings_th_uv, wholeOrDecimal(settings.uvCaution)),
                    )
                    rows.forEachIndexed { index, (group, label, value) ->
                        ThresholdRow(stringResource(label), value, onClick = { editingName = group.name })
                        if (index < rows.lastIndex) HorizontalDivider(color = CardBorder)
                    }
                }
            }
            item(key = "notifications-title") {
                SectionTitle(stringResource(R.string.settings_notifications), modifier = Modifier.padding(top = 6.dp))
            }
            item(key = "notifications") {
                HavaCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.NotificationsNone, contentDescription = null, tint = Brand, modifier = Modifier.size(22.dp))
                        Text(
                            text = stringResource(R.string.guard_notifications),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        Switch(checked = notificationsEnabled, onCheckedChange = onNotifications, colors = brandSwitchColors())
                    }
                    Text(stringResource(R.string.guard_notifications_summary), style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
            item(key = "other-title") {
                SectionTitle(stringResource(R.string.settings_other), modifier = Modifier.padding(top = 6.dp))
            }
            item(key = "other") {
                HavaCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp), spacing = 0.dp) {
                    NavigationRow(Icons.Outlined.LocationCity, stringResource(R.string.settings_manage_cities), onManageCities)
                    HorizontalDivider(color = CardBorder)
                    NavigationRow(Icons.Outlined.MyLocation, stringResource(R.string.use_my_location), onMyLocationClick)
                }
            }
            item(key = "note") {
                Text(stringResource(R.string.guard_thresholds_note), style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
    }
    val editing = editingName?.let { name -> ThresholdGroup.entries.firstOrNull { it.name == name } }
    if (editing != null) {
        ThresholdDialog(group = editing, settings = settings, onChange = onChange, onDismiss = { editingName = null })
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    caption: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, contentDescription = null, tint = Brand, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Ink)
            if (caption != null) Text(caption, style = MaterialTheme.typography.labelSmall, color = Muted)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled, colors = brandSwitchColors())
    }
}

@Composable
private fun ThresholdRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Ink, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Ink)
        ChevronIcon(modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun NavigationRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, contentDescription = null, tint = Brand, modifier = Modifier.size(22.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Ink, modifier = Modifier.weight(1f))
        ChevronIcon()
    }
}

@Composable
private fun ThresholdDialog(
    group: ThresholdGroup,
    settings: RiskSettings,
    onChange: (RiskSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val title = when (group) {
        ThresholdGroup.PRECIPITATION -> R.string.settings_th_precip
        ThresholdGroup.WIND -> R.string.settings_th_wind
        ThresholdGroup.HEAT -> R.string.settings_th_heat
        ThresholdGroup.FROST -> R.string.settings_th_frost
        ThresholdGroup.UV -> R.string.settings_th_uv
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(stringResource(title), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                when (group) {
                    ThresholdGroup.PRECIPITATION -> {
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
                    ThresholdGroup.WIND -> {
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
                    ThresholdGroup.HEAT -> {
                        DoubleSlider(stringResource(R.string.guard_slider_heat_caution), settings.heatCautionC, 28f, 40f) {
                            onChange(settings.copy(heatCautionC = it))
                        }
                        DoubleSlider(stringResource(R.string.guard_slider_heat_high), settings.heatHighC, 32f, 45f) {
                            onChange(settings.copy(heatHighC = it))
                        }
                    }
                    ThresholdGroup.FROST -> {
                        DoubleSlider(stringResource(R.string.guard_slider_frost_caution), settings.frostCautionC, -2f, 6f) {
                            onChange(settings.copy(frostCautionC = it))
                        }
                        DoubleSlider(stringResource(R.string.guard_slider_frost_high), settings.frostHighC, -8f, 2f) {
                            onChange(settings.copy(frostHighC = it))
                        }
                    }
                    ThresholdGroup.UV -> {
                        DoubleSlider(stringResource(R.string.guard_slider_uv_caution), settings.uvCaution, 3f, 8f) {
                            onChange(settings.copy(uvCaution = it))
                        }
                        DoubleSlider(stringResource(R.string.guard_slider_uv_high), settings.uvHigh, 6f, 12f) {
                            onChange(settings.copy(uvHigh = it))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok), color = Brand) }
        },
    )
}

@Composable
private fun IntSlider(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Text("$label: $value", style = MaterialTheme.typography.bodyMedium, color = Ink)
    Slider(
        value = value.toFloat().coerceIn(min.toFloat(), max.toFloat()),
        onValueChange = { onChange(it.roundToInt().coerceIn(min, max)) },
        valueRange = min.toFloat()..max.toFloat(),
        colors = brandSliderColors(),
    )
}

@Composable
private fun DoubleSlider(label: String, value: Double, min: Float, max: Float, onChange: (Double) -> Unit) {
    Text("$label: ${oneDecimal(value)}", style = MaterialTheme.typography.bodyMedium, color = Ink)
    Slider(
        value = value.toFloat().coerceIn(min, max),
        onValueChange = { onChange(it.toDouble().coerceIn(min.toDouble(), max.toDouble())) },
        valueRange = min..max,
        colors = brandSliderColors(),
    )
}

@Composable
private fun brandSliderColors() = SliderDefaults.colors(thumbColor = Brand, activeTrackColor = Brand)

@Composable
private fun brandSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = Brand,
    uncheckedThumbColor = Color.White,
    uncheckedTrackColor = CardBorder,
    uncheckedBorderColor = CardBorder,
    disabledCheckedThumbColor = Color.White,
    disabledCheckedTrackColor = Brand.copy(alpha = 0.45f),
)
