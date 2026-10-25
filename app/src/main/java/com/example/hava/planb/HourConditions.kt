package com.example.hava.planb

import com.example.hava.data.model.HourlyForecast

/**
 * PlanB'nin okuduğu saatlik ölçümler.
 * Günlük tahminden üretilmez; yalnızca saatlik forecast alanları gelir.
 * Sıcaklık °C, rüzgâr km/h, yağış mm, olasılık %, UV indeksi.
 */
data class HourConditions(
    /** "yyyy-MM-ddTHH:mm" */
    val time: String,
    val temperatureC: Double? = null,
    val apparentTemperatureC: Double? = null,
    /** 0–100 */
    val precipitationProbabilityPercent: Int? = null,
    val precipitationMm: Double? = null,
    val windSpeedKmh: Double? = null,
    /** Rüzgârın estiği yön, derece */
    val windDirectionDegrees: Int? = null,
    val windGustsKmh: Double? = null,
    val uvIndex: Double? = null,
    val weatherCode: Int? = null,
)

fun HourlyForecast.toHourConditions(): HourConditions = HourConditions(
    time = time,
    temperatureC = temperature,
    apparentTemperatureC = apparentTemperature,
    precipitationProbabilityPercent = precipitationProbability,
    precipitationMm = precipitation,
    windSpeedKmh = windSpeed,
    windDirectionDegrees = windDirection,
    windGustsKmh = windGusts,
    uvIndex = uvIndex,
    weatherCode = weatherCode,
)

internal fun String.forecastHour(): Int? = substringAfter('T', "").take(2).toIntOrNull()

internal fun isNextHour(previous: String, next: String): Boolean {
    if (previous.substringBefore('T') != next.substringBefore('T')) return false
    val previousHour = previous.forecastHour() ?: return false
    val nextHour = next.forecastHour() ?: return false
    return nextHour == previousHour + 1
}
