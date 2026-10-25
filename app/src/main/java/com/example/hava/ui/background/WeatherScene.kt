package com.example.hava.ui.background

import androidx.compose.runtime.Immutable
import com.example.hava.data.model.CurrentWeather
import com.example.hava.data.model.DayPhase
import com.example.hava.data.model.WeatherCondition
import java.util.Calendar

@Immutable
data class WeatherScene(
    val condition: WeatherCondition,
    val dayPhase: DayPhase,
    val intensity: Float = 0f,
) {
    companion object {
        /** İlk veri gelene kadar cihaz saatine göre tahmini bir sahne. */
        fun fromDeviceClock(): WeatherScene {
            val phase = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                6 -> DayPhase.SUNRISE
                in 7..17 -> DayPhase.DAY
                18 -> DayPhase.SUNSET
                else -> DayPhase.NIGHT
            }
            return WeatherScene(WeatherCondition.PARTLY_CLOUDY, phase)
        }
    }
}

fun CurrentWeather.toScene(): WeatherScene =
    WeatherScene(condition = condition, dayPhase = dayPhase, intensity = precipitationIntensity)
