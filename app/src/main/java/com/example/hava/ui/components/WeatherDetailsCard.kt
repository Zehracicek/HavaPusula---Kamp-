package com.example.hava.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hava.R
import com.example.hava.data.model.CurrentWeather
import com.example.hava.data.model.DailyForecast
import kotlin.math.roundToInt

private const val ITEMS_PER_ROW = 3

private class DetailItem(val emoji: String, val label: String, val value: String)

@Composable
fun WeatherDetailsCard(
    weather: CurrentWeather,
    today: DailyForecast?,
    modifier: Modifier = Modifier,
) {
    val noValue = stringResource(R.string.no_value)
    val uvLevels = stringArrayResource(R.array.uv_levels)
    val items = listOf(
        DetailItem(
            emoji = "🌡️",
            label = stringResource(R.string.feels_like),
            value = stringResource(R.string.temperature_value, weather.apparentTemperature.roundToInt()),
        ),
        DetailItem(
            emoji = "💧",
            label = stringResource(R.string.humidity),
            value = stringResource(R.string.humidity_value, weather.humidity),
        ),
        DetailItem(
            emoji = "☔",
            label = stringResource(R.string.precipitation),
            value = weather.precipitation
                ?.let { stringResource(R.string.precipitation_value, oneDecimal(it)) } ?: noValue,
        ),
        DetailItem(
            emoji = "🧭",
            label = stringResource(R.string.pressure),
            value = weather.pressure?.let { stringResource(R.string.pressure_value, it.roundToInt()) } ?: noValue,
        ),
        DetailItem(
            emoji = "👁️",
            label = stringResource(R.string.visibility),
            value = weather.visibility?.let { stringResource(R.string.visibility_value, visibilityKm(it)) } ?: noValue,
        ),
        DetailItem(
            emoji = "☁️",
            label = stringResource(R.string.cloud_cover),
            value = weather.cloudCover?.let { stringResource(R.string.percent_value, it) } ?: noValue,
        ),
        // Dar ekranlarda sığması için seviye adı değerin değil etiketin yanında
        DetailItem(
            emoji = "🕶️",
            label = weather.uvIndex
                ?.let { stringResource(R.string.uv_label, uvLevels[uvLevelIndex(it)]) }
                ?: stringResource(R.string.uv_index),
            value = weather.uvIndex?.roundToInt()?.toString() ?: noValue,
        ),
        DetailItem(
            emoji = "🌅",
            label = stringResource(R.string.sunrise),
            value = today?.sunrise?.let(::hourLabel) ?: noValue,
        ),
        DetailItem(
            emoji = "🌇",
            label = stringResource(R.string.sunset),
            value = today?.sunset?.let(::hourLabel) ?: noValue,
        ),
    )

    WeatherCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items.chunked(ITEMS_PER_ROW).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    row.forEach { item ->
                        WeatherDetailItem(
                            emoji = item.emoji,
                            label = item.label,
                            value = item.value,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherDetailItem(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = emoji, fontSize = 20.sp, modifier = Modifier.clearAndSetSemantics {})
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = LocalContentColor.current.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(widthDp = 320, showBackground = true, backgroundColor = 0xFF1A73E8)
@Composable
private fun WeatherDetailsCardPreview() {
    WeatherDetailsCard(PreviewData.report.current, PreviewData.report.daily.first())
}
