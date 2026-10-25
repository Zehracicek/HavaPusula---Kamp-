package com.example.hava.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hava.R
import com.example.hava.data.model.DailyForecast
import kotlin.math.roundToInt

private val ColdColor = Color(0xFF7FD1FF)
private val WarmColor = Color(0xFFFFB74D)
private val TemperatureWidth = 36.dp
private val IconColumnWidth = 40.dp

@Composable
fun DailyForecastCard(
    daily: List<DailyForecast>,
    modifier: Modifier = Modifier,
    onDayClick: ((DailyForecast) -> Unit)? = null,
) {
    WeatherCard(modifier = modifier, title = stringResource(R.string.daily_forecast)) {
        if (daily.isEmpty()) {
            ForecastUnavailable()
            return@WeatherCard
        }
        // Çubuklar haftanın tamamına göre ölçeklenir; günler birbiriyle kıyaslanabilir
        val weekMin = daily.minOf { it.minTemperature }
        val weekMax = daily.maxOf { it.maxTemperature }
        daily.forEachIndexed { index, day ->
            if (index > 0) {
                HorizontalDivider(color = LocalContentColor.current.copy(alpha = 0.15f))
            }
            DailyForecastRow(
                forecast = day,
                dayLabel = if (index == 0) stringResource(R.string.today) else shortDayName(day.date),
                weekMin = weekMin,
                weekMax = weekMax,
                onClick = onDayClick?.let { { it(day) } },
            )
        }
    }
}

@Composable
private fun DailyForecastRow(
    forecast: DailyForecast,
    dayLabel: String,
    weekMin: Double,
    weekMax: Double,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(56.dp),
        )
        Column(
            modifier = Modifier.width(IconColumnWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WeatherIcon(
                condition = forecast.condition,
                contentDescription = stringResource(weatherDescriptionRes(forecast.weatherCode)),
            )
            PrecipitationChance(forecast.precipitationProbability)
        }
        Text(
            text = stringResource(R.string.temperature_value, forecast.minTemperature.roundToInt()),
            style = MaterialTheme.typography.bodyLarge,
            color = LocalContentColor.current.copy(alpha = 0.75f),
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.widthIn(min = TemperatureWidth),
        )
        TemperatureRangeBar(
            min = forecast.minTemperature,
            max = forecast.maxTemperature,
            rangeMin = weekMin,
            rangeMax = weekMax,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.temperature_value, forecast.maxTemperature.roundToInt()),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.widthIn(min = TemperatureWidth),
        )
        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = LocalContentColor.current.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun TemperatureRangeBar(
    min: Double,
    max: Double,
    rangeMin: Double,
    rangeMax: Double,
    modifier: Modifier = Modifier,
) {
    val trackColor = LocalContentColor.current.copy(alpha = 0.2f)
    Canvas(modifier = modifier.height(6.dp)) {
        val span = (rangeMax - rangeMin).takeIf { it > 0 } ?: 1.0
        val start = ((min - rangeMin) / span).toFloat() * size.width
        val end = ((max - rangeMin) / span).toFloat() * size.width
        val radius = CornerRadius(size.height / 2)

        drawRoundRect(color = trackColor, cornerRadius = radius)
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(ColdColor, WarmColor), startX = 0f, endX = size.width),
            topLeft = Offset(start, 0f),
            size = Size((end - start).coerceAtLeast(size.height), size.height),
            cornerRadius = radius,
        )
    }
}

@Preview(widthDp = 320, showBackground = true, backgroundColor = 0xFF1A73E8)
@Composable
private fun DailyForecastCardPreview() {
    DailyForecastCard(PreviewData.report.daily)
}
