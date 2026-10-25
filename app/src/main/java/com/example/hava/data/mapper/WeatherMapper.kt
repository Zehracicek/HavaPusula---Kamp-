package com.example.hava.data.mapper

import com.example.hava.data.local.moonInfoOf
import com.example.hava.data.model.CurrentConditions
import com.example.hava.data.model.CurrentWeather
import com.example.hava.data.model.DailyForecast
import com.example.hava.data.model.DayPhase
import com.example.hava.data.model.HourlyForecast
import com.example.hava.data.model.WeatherCondition
import com.example.hava.data.model.WeatherReport
import com.example.hava.data.remote.dto.DailyWeatherDto
import com.example.hava.data.remote.dto.HourlyWeatherDto
import com.example.hava.data.remote.dto.WeatherResponse
import kotlin.math.abs

private const val TWILIGHT_WINDOW_MINUTES = 45
private const val DEFAULT_WEATHER_CODE = 3
private const val HOURLY_FORECAST_COUNT = 24

// "yyyy-MM-ddTHH" kısmı; aynı biçimdeki zamanlar metin olarak karşılaştırılabilir
private const val HOUR_PREFIX_LENGTH = 13

fun WeatherResponse.toWeatherReport(
    cityName: String,
    latitude: Double = 0.0,
    longitude: Double = 0.0,
): WeatherReport {
    val upcomingHours = hourly?.toHourlyForecasts(from = current?.time).orEmpty()
    val days = daily?.toDailyForecasts().orEmpty()
    return WeatherReport(
        current = toCurrentWeather(cityName),
        hourly = upcomingHours.take(HOURLY_FORECAST_COUNT),
        daily = days,
        hourlyByDate = upcomingHours.groupBy { it.time.substringBefore('T') },
        latitude = latitude,
        longitude = longitude,
        moon = moonInfoOf(now = current?.time, daily = days),
        observedAt = current?.time,
    )
}

private fun WeatherResponse.toCurrentWeather(cityName: String): CurrentWeather {
    val current = requireNotNull(current) { "Yanıtta güncel hava durumu yok" }
    val code = current.weatherCode ?: DEFAULT_WEATHER_CODE
    val (condition, intensity) = conditionOf(code)

    return CurrentWeather(
        cityName = cityName,
        temperature = current.temperature ?: 0.0,
        apparentTemperature = current.apparentTemperature ?: current.temperature ?: 0.0,
        humidity = current.humidity ?: 0,
        windSpeed = current.windSpeed ?: 0.0,
        weatherCode = code,
        condition = condition,
        precipitationIntensity = intensity,
        dayPhase = dayPhaseOf(
            now = current.time?.minutesOfDay(),
            sunrise = daily?.sunrise?.firstOrNull()?.minutesOfDay(),
            sunset = daily?.sunset?.firstOrNull()?.minutesOfDay(),
            isDay = current.isDay != 0,
        ),
        windDirection = current.windDirection,
        windGusts = current.windGusts,
        pressure = current.pressure,
        cloudCover = current.cloudCover,
        visibility = current.visibility,
        uvIndex = current.uvIndex,
        precipitation = current.precipitation,
    )
}

/** Toplu istekteki tek konumun özeti; sıcaklık yoksa null. */
fun WeatherResponse.toCurrentConditions(): CurrentConditions? {
    val temperature = current?.temperature ?: return null
    val code = current.weatherCode ?: DEFAULT_WEATHER_CODE
    return CurrentConditions(
        temperature = temperature,
        weatherCode = code,
        condition = conditionOf(code).first,
        isDay = current.isDay != 0,
    )
}

private fun HourlyWeatherDto.toHourlyForecasts(from: String?): List<HourlyForecast> {
    val times = time ?: return emptyList()
    val startHour = from?.take(HOUR_PREFIX_LENGTH)
    return times.indices.asSequence()
        .filter { startHour == null || times[it].take(HOUR_PREFIX_LENGTH) >= startHour }
        .mapNotNull { i ->
            val temp = temperature?.getOrNull(i) ?: return@mapNotNull null
            val code = weatherCode?.getOrNull(i) ?: DEFAULT_WEATHER_CODE
            HourlyForecast(
                time = times[i],
                temperature = temp,
                weatherCode = code,
                condition = conditionOf(code).first,
                isDay = isDay?.getOrNull(i) != 0,
                precipitationProbability = precipitationProbability?.getOrNull(i),
                precipitation = precipitation?.getOrNull(i),
                windSpeed = windSpeed?.getOrNull(i),
                windDirection = windDirection?.getOrNull(i),
                windGusts = windGusts?.getOrNull(i),
                uvIndex = uvIndex?.getOrNull(i),
                apparentTemperature = apparentTemperature?.getOrNull(i),
            )
        }
        .toList()
}

private fun DailyWeatherDto.toDailyForecasts(): List<DailyForecast> {
    val dates = time ?: return emptyList()
    return dates.indices.mapNotNull { i ->
        val max = maxTemperature?.getOrNull(i) ?: return@mapNotNull null
        val min = minTemperature?.getOrNull(i) ?: return@mapNotNull null
        val code = weatherCode?.getOrNull(i) ?: DEFAULT_WEATHER_CODE
        DailyForecast(
            date = dates[i],
            maxTemperature = max,
            minTemperature = min,
            weatherCode = code,
            condition = conditionOf(code).first,
            precipitationProbability = precipitationProbability?.getOrNull(i),
            precipitationSum = precipitationSum?.getOrNull(i),
            maxWindSpeed = maxWindSpeed?.getOrNull(i),
            maxWindGusts = maxWindGusts?.getOrNull(i),
            dominantWindDirection = dominantWindDirection?.getOrNull(i),
            maxUvIndex = maxUvIndex?.getOrNull(i),
            sunrise = sunrise?.getOrNull(i),
            sunset = sunset?.getOrNull(i),
            moonrise = moonrise?.getOrNull(i),
            moonset = moonset?.getOrNull(i),
            moonPhase = moonPhase?.getOrNull(i),
        )
    }
}

fun weatherConditionOf(code: Int): WeatherCondition = conditionOf(code).first

/** WMO hava durumu kodlarını (Open-Meteo) uygulama durumuna çevirir. */
private fun conditionOf(code: Int): Pair<WeatherCondition, Float> = when (code) {
    0, 1 -> WeatherCondition.CLEAR to 0f
    2 -> WeatherCondition.PARTLY_CLOUDY to 0f
    3 -> WeatherCondition.CLOUDY to 0f
    45, 48 -> WeatherCondition.FOG to 0f
    51, 56 -> WeatherCondition.RAIN to 0.3f
    53 -> WeatherCondition.RAIN to 0.4f
    55, 57 -> WeatherCondition.RAIN to 0.5f
    61, 66, 80 -> WeatherCondition.RAIN to 0.6f
    63, 81 -> WeatherCondition.RAIN to 0.8f
    65, 67, 82 -> WeatherCondition.RAIN to 1f
    71, 77, 85 -> WeatherCondition.SNOW to 0.5f
    73 -> WeatherCondition.SNOW to 0.75f
    75, 86 -> WeatherCondition.SNOW to 1f
    95 -> WeatherCondition.STORM to 0.85f
    96, 99 -> WeatherCondition.STORM to 1f
    else -> WeatherCondition.CLOUDY to 0f
}

private fun dayPhaseOf(now: Int?, sunrise: Int?, sunset: Int?, isDay: Boolean): DayPhase = when {
    now != null && sunrise != null && abs(now - sunrise) <= TWILIGHT_WINDOW_MINUTES -> DayPhase.SUNRISE
    now != null && sunset != null && abs(now - sunset) <= TWILIGHT_WINDOW_MINUTES -> DayPhase.SUNSET
    isDay -> DayPhase.DAY
    else -> DayPhase.NIGHT
}

// minSdk 24'te java.time yok; "yyyy-MM-ddTHH:mm" içinden saat ve dakika okunuyor
private fun String.minutesOfDay(): Int? {
    val parts = substringAfter('T', missingDelimiterValue = "").split(':')
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: return null
    return hour * 60 + minute
}
