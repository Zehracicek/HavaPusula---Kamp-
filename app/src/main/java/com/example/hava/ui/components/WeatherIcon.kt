package com.example.hava.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.hava.data.model.WeatherCondition

// material-icons-core hava durumu ikonları içermediği için emoji kullanılıyor
@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    contentDescription: String,
    modifier: Modifier = Modifier,
    isDay: Boolean = true,
    size: TextUnit = 24.sp,
) {
    Text(
        text = condition.emoji(isDay),
        fontSize = size,
        modifier = modifier.clearAndSetSemantics { this.contentDescription = contentDescription },
    )
}

private fun WeatherCondition.emoji(isDay: Boolean): String = when (this) {
    WeatherCondition.CLEAR -> if (isDay) "☀️" else "🌙"
    WeatherCondition.PARTLY_CLOUDY -> if (isDay) "⛅" else "☁️"
    WeatherCondition.CLOUDY -> "☁️"
    WeatherCondition.FOG -> "🌫️"
    WeatherCondition.RAIN -> "🌧️"
    WeatherCondition.STORM -> "⛈️"
    WeatherCondition.SNOW -> "❄️"
}
