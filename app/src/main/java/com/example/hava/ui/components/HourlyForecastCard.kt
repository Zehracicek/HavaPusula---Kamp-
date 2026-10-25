package com.example.hava.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hava.R
import com.example.hava.data.model.HourlyForecast
import kotlin.math.roundToInt

@Composable
fun HourlyForecastCard(
    hourly: List<HourlyForecast>,
    modifier: Modifier = Modifier,
) {
    WeatherCard(modifier = modifier, title = stringResource(R.string.hourly_forecast)) {
        if (hourly.isEmpty()) {
            ForecastUnavailable()
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(items = hourly, key = { _, item -> item.time }) { index, item ->
                    HourlyForecastItem(
                        forecast = item,
                        label = if (index == 0) stringResource(R.string.now) else hourLabel(item.time),
                    )
                }
            }
        }
    }
}

@Composable
private fun HourlyForecastItem(
    forecast: HourlyForecast,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .widthIn(min = 52.dp)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = LocalContentColor.current.copy(alpha = 0.8f),
            maxLines = 1,
        )
        WeatherIcon(
            condition = forecast.condition,
            contentDescription = stringResource(weatherDescriptionRes(forecast.weatherCode)),
            isDay = forecast.isDay,
        )
        Text(
            text = stringResource(R.string.temperature_value, forecast.temperature.roundToInt()),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        PrecipitationChance(forecast.precipitationProbability)
    }
}

/** Satırların hizası bozulmasın diye olasılık düşükken de yer kaplar. */
@Composable
internal fun PrecipitationChance(probability: Int?, modifier: Modifier = Modifier) {
    val shown = probability?.takeIf { it >= MIN_VISIBLE_PRECIPITATION_CHANCE }
    Text(
        text = shown?.let { stringResource(R.string.percent_value, it) } ?: " ",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = PrecipitationColor,
        maxLines = 1,
        modifier = modifier,
    )
}

private const val MIN_VISIBLE_PRECIPITATION_CHANCE = 10
private val PrecipitationColor = Color(0xFF9AD8FF)

@Preview(widthDp = 320, showBackground = true, backgroundColor = 0xFF1A73E8)
@Composable
private fun HourlyForecastCardPreview() {
    HourlyForecastCard(PreviewData.report.hourly)
}

@Preview(widthDp = 320, showBackground = true, backgroundColor = 0xFF1A73E8)
@Composable
private fun HourlyForecastCardEmptyPreview() {
    HourlyForecastCard(emptyList())
}
