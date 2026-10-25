package com.example.hava.ui.screens.guard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cyclone
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.hava.R
import com.example.hava.guard.GuardRisk
import com.example.hava.guard.RiskFinding
import com.example.hava.guard.RiskLevel
import com.example.hava.guard.RiskMeasurement
import com.example.hava.guard.RiskReason
import com.example.hava.guard.RiskThreshold
import com.example.hava.guard.RiskWindow
import com.example.hava.guard.UserRisk
import com.example.hava.planb.FactorKind
import com.example.hava.planb.MeasurementUnit
import com.example.hava.ui.components.hourLabel
import com.example.hava.ui.components.lowercaseTr
import com.example.hava.ui.components.oneDecimal
import com.example.hava.ui.components.percentTr
import com.example.hava.ui.theme.Brand
import com.example.hava.ui.theme.BrandSoft
import com.example.hava.ui.theme.Danger
import com.example.hava.ui.theme.DangerSoft
import com.example.hava.ui.theme.Info
import com.example.hava.ui.theme.InfoSoft
import com.example.hava.ui.theme.Success
import com.example.hava.ui.theme.SuccessSoft
import com.example.hava.ui.theme.Warning
import com.example.hava.ui.theme.WarningSoft
import kotlin.math.roundToInt

internal data class RiskStyle(val icon: ImageVector, val color: Color, val soft: Color)

internal fun GuardRisk.style(): RiskStyle = when (this) {
    GuardRisk.PRECIPITATION -> RiskStyle(Icons.Filled.Umbrella, Danger, DangerSoft)
    GuardRisk.WIND -> RiskStyle(Icons.Filled.Air, Info, InfoSoft)
    GuardRisk.GUST -> RiskStyle(Icons.Filled.Cyclone, Color(0xFF0EA5A4), Color(0xFFE0F5F5))
    GuardRisk.HEAT -> RiskStyle(Icons.Filled.Thermostat, Warning, WarningSoft)
    GuardRisk.FROST -> RiskStyle(Icons.Filled.AcUnit, Color(0xFF3B82F6), Color(0xFFE8F0FE))
    GuardRisk.UV -> RiskStyle(Icons.Filled.WbSunny, Success, SuccessSoft)
    GuardRisk.THUNDERSTORM -> RiskStyle(Icons.Filled.Thunderstorm, Brand, BrandSoft)
}

internal fun UserRisk.guardRisk(): GuardRisk = when (this) {
    UserRisk.PRECIPITATION -> GuardRisk.PRECIPITATION
    UserRisk.WIND -> GuardRisk.WIND
    UserRisk.GUST -> GuardRisk.GUST
    UserRisk.HEAT -> GuardRisk.HEAT
    UserRisk.FROST -> GuardRisk.FROST
    UserRisk.UV -> GuardRisk.UV
}

internal fun UserRisk.settingsLabelRes(): Int = when (this) {
    UserRisk.PRECIPITATION -> R.string.settings_risk_precip
    UserRisk.WIND -> R.string.settings_risk_wind
    UserRisk.GUST -> R.string.settings_risk_gust
    UserRisk.HEAT -> R.string.settings_risk_heat
    UserRisk.FROST -> R.string.settings_risk_frost
    UserRisk.UV -> R.string.settings_risk_uv
}

internal fun GuardRisk.labelRes(): Int = when (this) {
    GuardRisk.PRECIPITATION -> R.string.guard_risk_precip
    GuardRisk.WIND -> R.string.guard_risk_wind
    GuardRisk.GUST -> R.string.guard_risk_gust
    GuardRisk.HEAT -> R.string.guard_risk_heat
    GuardRisk.FROST -> R.string.guard_risk_frost
    GuardRisk.UV -> R.string.guard_risk_uv
    GuardRisk.THUNDERSTORM -> R.string.guard_risk_storm
}

internal fun GuardRisk.detailRes(): Int = when (this) {
    GuardRisk.PRECIPITATION -> R.string.guard_detail_precip
    GuardRisk.WIND -> R.string.guard_detail_wind
    GuardRisk.GUST -> R.string.guard_detail_gust
    GuardRisk.HEAT -> R.string.guard_detail_heat
    GuardRisk.FROST -> R.string.guard_detail_frost
    GuardRisk.UV -> R.string.guard_detail_uv
    GuardRisk.THUNDERSTORM -> R.string.guard_detail_storm
}

internal fun GuardRisk.bodyRes(): Int = when (this) {
    GuardRisk.PRECIPITATION -> R.string.guard_body_precip
    GuardRisk.WIND -> R.string.guard_body_wind
    GuardRisk.GUST -> R.string.guard_body_gust
    GuardRisk.HEAT -> R.string.guard_body_heat
    GuardRisk.FROST -> R.string.guard_body_frost
    GuardRisk.UV -> R.string.guard_body_uv
    GuardRisk.THUNDERSTORM -> R.string.guard_body_storm
}

internal fun GuardRisk.tipsRes(): Int = when (this) {
    GuardRisk.PRECIPITATION -> R.array.guard_tips_precip
    GuardRisk.WIND, GuardRisk.GUST -> R.array.guard_tips_wind
    GuardRisk.HEAT -> R.array.guard_tips_heat
    GuardRisk.FROST -> R.array.guard_tips_frost
    GuardRisk.UV -> R.array.guard_tips_uv
    GuardRisk.THUNDERSTORM -> R.array.guard_tips_storm
}

internal fun RiskFinding.cardTitleRes(): Int = when (risk) {
    GuardRisk.PRECIPITATION ->
        if (reason == RiskReason.AMOUNT_ONLY) R.string.guard_card_precip_amount else R.string.guard_card_precip
    GuardRisk.WIND -> R.string.guard_card_wind
    GuardRisk.GUST -> R.string.guard_card_gust
    GuardRisk.HEAT -> R.string.guard_card_heat
    GuardRisk.FROST -> R.string.guard_card_frost
    GuardRisk.UV -> R.string.guard_card_uv
    GuardRisk.THUNDERSTORM -> R.string.guard_card_storm
}

internal fun RiskLevel.labelRes(): Int = when (this) {
    RiskLevel.CAUTION -> R.string.guard_level_caution
    RiskLevel.HIGH -> R.string.guard_level_high
}

internal fun RiskLevel.chipColors(): Pair<Color, Color> = when (this) {
    RiskLevel.CAUTION -> Color(0xFFB7791F) to Color(0xFFFFE9B8)
    RiskLevel.HIGH -> Danger to Color(0xFFFFD9DB)
}

internal fun RiskReason.actionRes(): Int = when (this) {
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

internal fun FactorKind.guardLabelRes(): Int = when (this) {
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

/** Kartta büyük gösterilen ölçüm: yağışta önce olasılık, diğerlerinde ilk ölçüm. */
internal fun RiskFinding.primaryMeasurement(): RiskMeasurement? =
    measurements.firstOrNull { it.kind == FactorKind.PRECIPITATION_PROBABILITY } ?: measurements.firstOrNull()

internal fun RiskFinding.thresholdFor(kind: FactorKind): RiskThreshold? = thresholds.firstOrNull { it.kind == kind }

@Composable
internal fun RiskFinding.valueLabel(measurement: RiskMeasurement): String = when (risk) {
    GuardRisk.HEAT -> stringResource(R.string.guard_value_heat)
    GuardRisk.FROST -> stringResource(R.string.guard_value_frost)
    GuardRisk.THUNDERSTORM -> stringResource(R.string.guard_value_storm)
    else -> stringResource(measurement.kind.guardLabelRes())
}

internal fun formatValue(value: Double, unit: MeasurementUnit): String = when (unit) {
    MeasurementUnit.PERCENT -> percentTr(value.roundToInt())
    MeasurementUnit.CELSIUS -> "${wholeOrDecimal(value)}°C"
    MeasurementUnit.MILLIMETER -> "${oneDecimal(value)} mm"
    MeasurementUnit.KILOMETERS_PER_HOUR -> "${value.roundToInt()} km/sa"
    MeasurementUnit.UV_INDEX -> wholeOrDecimal(value)
    MeasurementUnit.NONE -> value.roundToInt().toString()
}

internal fun wholeOrDecimal(value: Double): String =
    if (value % 1.0 == 0.0) value.roundToInt().toString() else oneDecimal(value)

internal fun RiskWindow.label(): String = "${hourLabel(startTime)} – ${hourLabel(endTime)}"

@Composable
internal fun joinedFactors(kinds: List<FactorKind>): String {
    val labels = ArrayList<String>(kinds.size)
    for (kind in kinds) labels += stringResource(kind.guardLabelRes()).lowercaseTr()
    return labels.joinToString()
}
