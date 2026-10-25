package com.example.hava.ui.components

import com.example.hava.data.local.TurkishProvinces
import com.example.hava.data.model.CurrentConditions
import com.example.hava.data.model.CurrentWeather
import com.example.hava.data.model.DailyForecast
import com.example.hava.data.model.DayPhase
import com.example.hava.data.model.HourlyForecast
import com.example.hava.data.model.MoonInfo
import com.example.hava.data.model.MoonPhase
import com.example.hava.data.model.WeatherCondition
import com.example.hava.data.model.WeatherReport

/** Yalnızca @Preview fonksiyonları için örnek veri. */
internal object PreviewData {

    val report = WeatherReport(
        current = CurrentWeather(
            cityName = "Ankara",
            temperature = 18.4,
            apparentTemperature = 16.9,
            humidity = 52,
            windSpeed = 14.0,
            weatherCode = 2,
            condition = WeatherCondition.PARTLY_CLOUDY,
            precipitationIntensity = 0f,
            dayPhase = DayPhase.DAY,
            windDirection = 315,
            windGusts = 28.0,
            pressure = 1014.2,
            cloudCover = 40,
            visibility = 24_000.0,
            uvIndex = 3.4,
            precipitation = 0.0,
        ),
        hourly = (0 until 24).map { i ->
            val hour = (14 + i) % 24
            val code = listOf(2, 2, 3, 61, 63, 3, 0, 0)[i % 8]
            HourlyForecast(
                time = "2026-10-25T%02d:00".format(hour),
                temperature = 18.0 - i % 12 * 0.6,
                weatherCode = code,
                condition = when (code) {
                    0 -> WeatherCondition.CLEAR
                    2 -> WeatherCondition.PARTLY_CLOUDY
                    3 -> WeatherCondition.CLOUDY
                    else -> WeatherCondition.RAIN
                },
                isDay = hour in 7..17,
                precipitationProbability = listOf(0, 5, 20, 70, 85, 30, 0, 0)[i % 8],
                windSpeed = 10.0 + i % 5,
            )
        },
        daily = listOf(
            DailyForecast(
                "2026-10-25", 19.0, 8.0, 2, WeatherCondition.PARTLY_CLOUDY,
                precipitationProbability = 15, maxWindSpeed = 22.0, maxWindGusts = 41.0,
                sunrise = "2026-10-25T07:20", sunset = "2026-10-25T18:10",
                moonset = "2026-10-25T06:09", moonPhase = 0.47,
            ),
            DailyForecast("2026-10-26", 16.0, 7.0, 63, WeatherCondition.RAIN, precipitationProbability = 80),
            DailyForecast("2026-10-27", 14.0, 5.0, 3, WeatherCondition.CLOUDY, precipitationProbability = 25),
            DailyForecast("2026-10-28", 17.0, 6.0, 0, WeatherCondition.CLEAR, precipitationProbability = 0),
            DailyForecast("2026-10-29", 21.0, 9.0, 0, WeatherCondition.CLEAR, precipitationProbability = 0),
            DailyForecast("2026-10-30", 12.0, 3.0, 95, WeatherCondition.STORM, precipitationProbability = 90),
            DailyForecast("2026-10-31", 6.0, -1.0, 73, WeatherCondition.SNOW, precipitationProbability = 60),
        ),
        latitude = 39.92,
        longitude = 32.85,
        moon = MoonInfo(
            phase = 0.47,
            illumination = 0.99,
            phaseName = MoonPhase.WAXING_GIBBOUS,
            nextRise = "2026-10-25T17:11",
            nextSet = "2026-10-26T07:24",
            daysUntilFullMoon = 1,
            daysUntilNewMoon = 16,
        ),
    )

    val provinces = TurkishProvinces.list.sortedBy { it.name }.take(8)

    val conditions: Map<Long, CurrentConditions> = provinces.mapIndexed { i, city ->
        val code = listOf(0, 2, 3, 61, 0, 45, 2, 71)[i]
        city.id to CurrentConditions(
            temperature = 22.0 - i * 2.3,
            weatherCode = code,
            condition = when (code) {
                0 -> WeatherCondition.CLEAR
                2 -> WeatherCondition.PARTLY_CLOUDY
                3 -> WeatherCondition.CLOUDY
                45 -> WeatherCondition.FOG
                61 -> WeatherCondition.RAIN
                else -> WeatherCondition.SNOW
            },
            isDay = true,
        )
    }.toMap()
}
