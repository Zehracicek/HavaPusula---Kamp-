package com.example.hava.ui.background

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.hava.data.model.DayPhase
import com.example.hava.data.model.WeatherCondition
import com.example.hava.data.model.WeatherCondition.CLEAR
import com.example.hava.data.model.WeatherCondition.CLOUDY
import com.example.hava.data.model.WeatherCondition.FOG
import com.example.hava.data.model.WeatherCondition.PARTLY_CLOUDY
import com.example.hava.data.model.WeatherCondition.RAIN
import com.example.hava.data.model.WeatherCondition.SNOW
import com.example.hava.data.model.WeatherCondition.STORM

/** Bir sahnenin çizim parametreleri; geçişlerde her alan ayrı ayrı canlandırılır. */
@Immutable
internal data class SkySpec(
    val skyTop: Color,
    val skyMiddle: Color,
    val skyBottom: Color,
    val sunAlpha: Float,
    val sunColor: Color,
    /** Güneş merkezinin ekran yüksekliğine oranı */
    val sunHeight: Float,
    val moonAlpha: Float,
    val starsAlpha: Float,
    val cloudCover: Float,
    val cloudColor: Color,
    val rainIntensity: Float,
    val snowIntensity: Float,
    val hazeAlpha: Float,
    val lightning: Boolean,
)

@Immutable
private data class SkyPalette(val top: Color, val middle: Color, val bottom: Color) {
    fun blend(other: SkyPalette, fraction: Float) = SkyPalette(
        top = lerp(top, other.top, fraction),
        middle = lerp(middle, other.middle, fraction),
        bottom = lerp(bottom, other.bottom, fraction),
    )
}

private val SunrisePalette = SkyPalette(Color(0xFF2B3A8C), Color(0xFFD9719A), Color(0xFFFFB774))
private val SunsetPalette = SkyPalette(Color(0xFF34205E), Color(0xFFC9507A), Color(0xFFFF8A47))

private val DaySunColor = Color(0xFFFFF1C1)
private val SunriseSunColor = Color(0xFFFFC27A)
private val SunsetSunColor = Color(0xFFFF9A5A)
private val TwilightCloudTint = Color(0xFFF6B3A6)

internal fun WeatherScene.toSkySpec(): SkySpec {
    val isNight = dayPhase == DayPhase.NIGHT
    val isTwilight = dayPhase == DayPhase.SUNRISE || dayPhase == DayPhase.SUNSET
    val baseCloudColor = cloudColor(condition, isNight)
    val palette = palette()

    return SkySpec(
        skyTop = palette.top,
        skyMiddle = palette.middle,
        skyBottom = palette.bottom,
        sunAlpha = if (isNight) 0f else sunAlpha(condition, isTwilight),
        sunColor = when (dayPhase) {
            DayPhase.SUNRISE -> SunriseSunColor
            DayPhase.SUNSET -> SunsetSunColor
            else -> DaySunColor
        },
        sunHeight = if (isTwilight) 0.36f else 0.1f,
        moonAlpha = if (!isNight) 0f else when (condition) {
            CLEAR -> 1f
            PARTLY_CLOUDY -> 0.85f
            else -> 0f
        },
        starsAlpha = if (!isNight) 0f else when (condition) {
            CLEAR -> 1f
            PARTLY_CLOUDY -> 0.55f
            else -> 0f
        },
        cloudCover = when (condition) {
            CLEAR -> 0f
            PARTLY_CLOUDY -> 0.5f
            CLOUDY, STORM -> 1f
            FOG -> 0.8f
            RAIN -> 0.9f
            SNOW -> 0.75f
        },
        cloudColor = if (isTwilight) lerp(baseCloudColor, TwilightCloudTint, 0.45f) else baseCloudColor,
        rainIntensity = if (condition == RAIN || condition == STORM) intensity.coerceIn(0.25f, 1f) else 0f,
        snowIntensity = if (condition == SNOW) intensity.coerceIn(0.25f, 1f) else 0f,
        hazeAlpha = if (condition == FOG) 1f else 0f,
        lightning = condition == STORM,
    )
}

private fun WeatherScene.palette(): SkyPalette = when (dayPhase) {
    DayPhase.DAY -> dayPalette(condition)
    DayPhase.NIGHT -> nightPalette(condition)
    DayPhase.SUNRISE -> SunrisePalette.blend(dayPalette(condition), twilightWeatherWeight(condition))
    DayPhase.SUNSET -> SunsetPalette.blend(dayPalette(condition), twilightWeatherWeight(condition))
}

private fun dayPalette(condition: WeatherCondition) = when (condition) {
    CLEAR -> SkyPalette(Color(0xFF1A73E8), Color(0xFF4AA8F0), Color(0xFFA9DCFA))
    PARTLY_CLOUDY -> SkyPalette(Color(0xFF2F6FC4), Color(0xFF64A0DA), Color(0xFFBFD9F0))
    CLOUDY -> SkyPalette(Color(0xFF55687C), Color(0xFF7D8FA2), Color(0xFFADBAC7))
    FOG -> SkyPalette(Color(0xFF7D8B97), Color(0xFFA2AEB8), Color(0xFFCCD3D9))
    RAIN -> SkyPalette(Color(0xFF2F3E48), Color(0xFF4A5F6C), Color(0xFF6F8593))
    STORM -> SkyPalette(Color(0xFF161B23), Color(0xFF283039), Color(0xFF3E4853))
    SNOW -> SkyPalette(Color(0xFF7189A1), Color(0xFF9DB2C6), Color(0xFFD5E0EA))
}

private fun nightPalette(condition: WeatherCondition) = when (condition) {
    CLEAR -> SkyPalette(Color(0xFF030920), Color(0xFF0B1A3E), Color(0xFF1A2E5C))
    PARTLY_CLOUDY -> SkyPalette(Color(0xFF071128), Color(0xFF13224A), Color(0xFF24365F))
    CLOUDY -> SkyPalette(Color(0xFF0F141D), Color(0xFF1C2431), Color(0xFF2C3645))
    FOG -> SkyPalette(Color(0xFF151B24), Color(0xFF262F3B), Color(0xFF3A4452))
    RAIN -> SkyPalette(Color(0xFF090E14), Color(0xFF151E28), Color(0xFF232E3A))
    STORM -> SkyPalette(Color(0xFF040609), Color(0xFF0E131A), Color(0xFF1A212B))
    SNOW -> SkyPalette(Color(0xFF172131), Color(0xFF2A384C), Color(0xFF44546A))
}

/** Gün doğumu/batımı renklerinin hava durumuna göre ne kadar bastırılacağı. */
private fun twilightWeatherWeight(condition: WeatherCondition): Float = when (condition) {
    CLEAR -> 0f
    PARTLY_CLOUDY -> 0.15f
    CLOUDY, FOG, SNOW -> 0.5f
    RAIN -> 0.6f
    STORM -> 0.72f
}

private fun sunAlpha(condition: WeatherCondition, isTwilight: Boolean): Float = when (condition) {
    CLEAR -> if (isTwilight) 0.9f else 1f
    PARTLY_CLOUDY -> 0.75f
    CLOUDY, FOG -> if (isTwilight) 0.3f else 0.15f
    else -> 0f
}

private fun cloudColor(condition: WeatherCondition, isNight: Boolean): Color = if (isNight) {
    when (condition) {
        CLEAR, PARTLY_CLOUDY -> Color(0xFF8792AA)
        CLOUDY -> Color(0xFF4A5568)
        FOG -> Color(0xFF5F6B7A)
        RAIN -> Color(0xFF3D4756)
        STORM -> Color(0xFF2A313B)
        SNOW -> Color(0xFF7C8A9E)
    }
} else {
    when (condition) {
        CLEAR, PARTLY_CLOUDY -> Color.White
        CLOUDY -> Color(0xFFE3E8EE)
        FOG -> Color(0xFFEEF1F4)
        RAIN -> Color(0xFFA5B1BC)
        STORM -> Color(0xFF6B7480)
        SNOW -> Color(0xFFEEF2F6)
    }
}
