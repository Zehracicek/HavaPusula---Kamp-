package com.example.hava.guard

import com.example.hava.data.model.WeatherReport
import com.example.hava.planb.HourConditions
import com.example.hava.planb.toHourConditions

/** Güncel ölçüm, aynı saatteki saatlik tahminin eksik alanlarını doldurur. 15 dakikalık yağış, saatlik miktar yerine geçmez. */
fun WeatherReport.toGuardHours(): List<HourConditions> {
    val hourly = hourlyByDate.values.flatten().map { it.toHourConditions() }.sortedBy { it.time }
    val observed = observedAt ?: return hourly
    val current = HourConditions(
        time = observed,
        temperatureC = current.temperature,
        apparentTemperatureC = current.apparentTemperature,
        windSpeedKmh = current.windSpeed,
        windDirectionDegrees = current.windDirection,
        windGustsKmh = current.windGusts,
        uvIndex = current.uvIndex,
        weatherCode = current.weatherCode,
    )
    val index = hourly.indexOfFirst { it.time.take(HOUR_PREFIX) == observed.take(HOUR_PREFIX) }
    if (index < 0) return listOf(current) + hourly
    val hour = hourly[index]
    val merged = hour.copy(
        temperatureC = hour.temperatureC ?: current.temperatureC,
        apparentTemperatureC = hour.apparentTemperatureC ?: current.apparentTemperatureC,
        windSpeedKmh = hour.windSpeedKmh ?: current.windSpeedKmh,
        windDirectionDegrees = hour.windDirectionDegrees ?: current.windDirectionDegrees,
        windGustsKmh = hour.windGustsKmh ?: current.windGustsKmh,
        uvIndex = hour.uvIndex ?: current.uvIndex,
        weatherCode = hour.weatherCode ?: current.weatherCode,
    )
    return hourly.toMutableList().also { it[index] = merged }
}

private const val HOUR_PREFIX = 13
