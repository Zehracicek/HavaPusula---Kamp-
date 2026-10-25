package com.example.hava.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.hava.data.model.HourlyForecast
import com.example.hava.ui.theme.Ink
import kotlin.math.roundToInt

private val SheetColor = Color.White

/** Seçilen günün saat saat tahmini. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayHourlySheet(
    day: DailyForecast,
    hourly: List<HourlyForecast>,
    isToday: Boolean,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SheetColor,
        contentColor = Ink,
    ) {
        DayHourlyContent(day = day, hourly = hourly, isToday = isToday)
    }
}

@Composable
private fun DayHourlyContent(
    day: DailyForecast,
    hourly: List<HourlyForecast>,
    isToday: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isToday) stringResource(R.string.today) else fullDayTitle(day.date),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(
                        R.string.high_low,
                        day.maxTemperature.roundToInt(),
                        day.minTemperature.roundToInt(),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalContentColor.current.copy(alpha = 0.75f),
                )
            }
            WeatherIcon(
                condition = day.condition,
                contentDescription = stringResource(weatherDescriptionRes(day.weatherCode)),
            )
        }

        if (hourly.isEmpty()) {
            Column(modifier = Modifier.padding(24.dp)) { ForecastUnavailable() }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
            ) {
                itemsIndexed(items = hourly, key = { _, item -> item.time }) { index, item ->
                    if (index > 0) {
                        HorizontalDivider(color = LocalContentColor.current.copy(alpha = 0.12f))
                    }
                    HourRow(
                        forecast = item,
                        label = if (isToday && index == 0) stringResource(R.string.now) else hourLabel(item.time),
                    )
                }
            }
        }
    }
}

@Composable
private fun HourRow(forecast: HourlyForecast, label: String) {
    val description = stringResource(weatherDescriptionRes(forecast.weatherCode))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.width(56.dp),
        )
        WeatherIcon(
            condition = forecast.condition,
            contentDescription = description,
            isDay = forecast.isDay,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalContentColor.current.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        PrecipitationChance(forecast.precipitationProbability)
        Text(
            text = stringResource(R.string.temperature_value, forecast.temperature.roundToInt()),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 40.dp),
        )
    }
}

@Preview(widthDp = 360, showBackground = true, backgroundColor = 0xFF1C2740)
@Composable
private fun DayHourlyContentPreview() {
    DayHourlyContent(
        day = PreviewData.report.daily[1],
        hourly = PreviewData.report.hourly,
        isToday = false,
    )
}
