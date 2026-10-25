package com.example.hava.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hava.R
import com.example.hava.data.model.CurrentWeather
import com.example.hava.ui.screens.city.ErrorType
import com.example.hava.ui.screens.city.messageRes
import com.example.hava.ui.screens.weather.WeatherUiState
import kotlin.math.roundToInt

private val TextShadow = Shadow(
    color = Color.Black.copy(alpha = 0.35f),
    offset = Offset(0f, 2f),
    blurRadius = 8f,
)

private val HeaderMinHeight = 196.dp

private fun TextStyle.withShadow() = copy(shadow = TextShadow)

@Composable
fun CurrentWeatherHeader(
    state: WeatherUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = state,
        contentKey = { it::class },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier.fillMaxWidth(),
        label = "weatherHeader",
    ) { target ->
        // Sabit minimum yükseklik: durum değişirken altındaki liste zıplamasın
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HeaderMinHeight)
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            when (target) {
                is WeatherUiState.Loading -> {
                    CityName(target.cityName ?: stringResource(R.string.locating))
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(16.dp)
                            .size(32.dp),
                        color = LocalContentColor.current,
                        strokeWidth = 2.dp,
                    )
                }

                is WeatherUiState.Success -> WeatherSummary(
                    weather = target.report.current,
                    low = target.report.daily.firstOrNull()?.minTemperature,
                    high = target.report.daily.firstOrNull()?.maxTemperature,
                )

                is WeatherUiState.Error -> {
                    CityName(target.cityName ?: stringResource(R.string.my_location))
                    Text(
                        text = stringResource(R.string.weather_load_failed),
                        style = MaterialTheme.typography.bodyLarge.withShadow(),
                    )
                    Text(
                        text = stringResource(target.type.messageRes()),
                        style = MaterialTheme.typography.bodyMedium.withShadow(),
                        color = LocalContentColor.current.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                    )
                    // TextButton içinde LocalContentColor butonun kendi rengine döner; dış renk burada aktarılır
                    TextButton(
                        onClick = onRetry,
                        colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current),
                    ) {
                        Text(text = stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

@Composable
private fun CityName(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.headlineSmall.withShadow(),
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun WeatherSummary(weather: CurrentWeather, low: Double?, high: Double?) {
    CityName(weather.cityName)
    Text(
        text = stringResource(R.string.temperature_value, weather.temperature.roundToInt()),
        style = MaterialTheme.typography.displayLarge.withShadow(),
        fontSize = 76.sp,
        fontWeight = FontWeight.Light,
    )
    Text(
        text = stringResource(weatherDescriptionRes(weather.weatherCode)),
        style = MaterialTheme.typography.titleMedium.withShadow(),
    )
    if (low != null && high != null) {
        Text(
            text = stringResource(R.string.day_temperature_range, low.roundToInt(), high.roundToInt()),
            style = MaterialTheme.typography.titleMedium.withShadow(),
            color = LocalContentColor.current.copy(alpha = 0.85f),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A73E8)
@Composable
private fun CurrentWeatherHeaderPreview() {
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        CurrentWeatherHeader(state = WeatherUiState.Success(PreviewData.report), onRetry = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A73E8)
@Composable
private fun CurrentWeatherHeaderLocationErrorPreview() {
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        CurrentWeatherHeader(state = WeatherUiState.Error(null, ErrorType.LOCATION), onRetry = {})
    }
}
