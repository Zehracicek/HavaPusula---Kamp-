package com.example.hava.guard

import com.example.hava.planb.FactorKind
import com.example.hava.planb.HourConditions
import com.example.hava.planb.isNextHour

data class RiskWindow(val startTime: String, val endTime: String)

/**
 * Bulgunun geçerli saatini çevreleyen ve aynı seviyenin eşiğini kesintisiz aşan saatler.
 * Eşik bilgisi bulgunun kendisinden okunur; saat verisi yoksa yalnızca geçerli saat döner.
 */
fun RiskFinding.window(hours: List<HourConditions>): RiskWindow? {
    val at = validAt ?: return null
    val ordered = hours.sortedBy { it.time }
    val index = ordered.indexOfFirst { it.time == at }
    if (index < 0) return RiskWindow(at, at)
    var first = index
    while (first > 0 && isNextHour(ordered[first - 1].time, ordered[first].time) && exceeds(ordered[first - 1])) first--
    var last = index
    while (last < ordered.lastIndex && isNextHour(ordered[last].time, ordered[last + 1].time) && exceeds(ordered[last + 1])) last++
    return RiskWindow(ordered[first].time, ordered[last].time)
}

private fun RiskFinding.exceeds(hour: HourConditions): Boolean {
    if (risk == GuardRisk.THUNDERSTORM) return hour.weatherCode in STORM_CODES
    return thresholds.any { threshold ->
        val value = hour.valueFor(threshold.kind, risk) ?: return@any false
        if (risk == GuardRisk.FROST) value <= threshold.value else value >= threshold.value
    }
}

private fun HourConditions.valueFor(kind: FactorKind, risk: GuardRisk): Double? = when (kind) {
    FactorKind.PRECIPITATION_PROBABILITY -> precipitationProbabilityPercent?.toDouble()
    FactorKind.PRECIPITATION_AMOUNT -> precipitationMm
    FactorKind.WIND_SPEED -> windSpeedKmh
    FactorKind.WIND_GUSTS -> windGustsKmh
    FactorKind.UV_INDEX -> uvIndex
    FactorKind.TEMPERATURE -> if (risk == GuardRisk.HEAT) apparentTemperatureC ?: temperatureC else temperatureC
    else -> null
}

private val STORM_CODES = setOf(95, 96, 99)
