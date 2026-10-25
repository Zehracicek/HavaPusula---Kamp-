package com.example.hava.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hava.R
import com.example.hava.data.model.DailyForecast
import com.example.hava.data.model.HourlyForecast
import com.example.hava.ui.theme.Brand
import com.example.hava.ui.theme.BrandSoft
import com.example.hava.ui.theme.CardBorder
import com.example.hava.ui.theme.Danger
import com.example.hava.ui.theme.Info
import com.example.hava.ui.theme.InfoSoft
import com.example.hava.ui.theme.Ink
import com.example.hava.ui.theme.Muted
import com.example.hava.ui.theme.PageBackground
import com.example.hava.ui.theme.Warning
import com.example.hava.ui.theme.WarningSoft
import kotlin.math.roundToInt

private val ColumnWidth = 58.dp
private val ChartHeight = 132.dp
private val WindColor = Color(0xFF0EA5A4)
private val WindSoft = Color(0xFFE0F5F5)
private val SunColor = Color(0xFFF08C00)

private enum class DayMetric { TEMPERATURE, PRECIPITATION, WIND }

/** Seçilen günün saat saat tahmini; sıcaklık, yağış ve rüzgâr grafikleriyle. */
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
        containerColor = PageBackground,
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
    var metricName by rememberSaveable { mutableStateOf(DayMetric.TEMPERATURE.name) }
    var selected by rememberSaveable(day.date) { mutableIntStateOf(0) }
    val metric = DayMetric.valueOf(metricName)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DayHeader(day, isToday)
        if (hourly.isEmpty()) {
            HavaCard { ForecastUnavailable() }
            return@Column
        }
        DaySummary(day, hourly)
        MetricTabs(metric, onSelect = { metricName = it.name })
        HourlyChart(
            hourly = hourly,
            metric = metric,
            selected = selected.coerceIn(hourly.indices),
            isToday = isToday,
            onSelect = { selected = it },
        )
        val index = selected.coerceIn(hourly.indices)
        HourDetailCard(
            forecast = hourly[index],
            label = hourTitle(hourly[index], isToday && index == 0),
        )
    }
}

@Composable
private fun DayHeader(day: DailyForecast, isToday: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = if (isToday) stringResource(R.string.today) else fullDayTitle(day.date),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(weatherDescriptionRes(day.weatherCode)),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                StatusChip("↑ ${day.maxTemperature.roundToInt()}°", color = Danger, background = Color(0xFFFFE4E5))
                StatusChip("↓ ${day.minTemperature.roundToInt()}°", color = Info, background = InfoSoft)
            }
        }
        WeatherGlyph(day.condition, Modifier.size(76.dp))
    }
}

@Composable
private fun DaySummary(day: DailyForecast, hourly: List<HourlyForecast>) {
    val rainChance = day.precipitationProbability ?: hourly.mapNotNull { it.precipitationProbability }.maxOrNull()
    val maxWind = day.maxWindSpeed ?: hourly.mapNotNull { it.windSpeed }.maxOrNull()
    val maxUv = day.maxUvIndex ?: hourly.mapNotNull { it.uvIndex }.maxOrNull()
    val sun = if (day.sunrise != null && day.sunset != null) {
        "${hourLabel(day.sunrise)}\n${hourLabel(day.sunset)}"
    } else {
        null
    }
    val none = stringResource(R.string.no_value)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
        SummaryTile(Icons.Filled.WaterDrop, Info, InfoSoft, rainChance?.let(::percentTr) ?: none, stringResource(R.string.precipitation), Modifier.weight(1f))
        SummaryTile(
            Icons.Filled.Air,
            WindColor,
            WindSoft,
            maxWind?.let { stringResource(R.string.wind_kmh, it.roundToInt()) } ?: none,
            stringResource(R.string.wind),
            Modifier.weight(1f),
        )
        SummaryTile(Icons.Filled.WbSunny, Warning, WarningSoft, maxUv?.roundToInt()?.toString() ?: none, "UV", Modifier.weight(1f))
        SummaryTile(Icons.Filled.WbTwilight, SunColor, Color(0xFFFFF0E0), sun ?: none, stringResource(R.string.day_sun), Modifier.weight(1f))
    }
}

@Composable
private fun SummaryTile(
    icon: ImageVector,
    tint: Color,
    background: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    HavaCard(modifier = modifier.fillMaxHeight(), contentPadding = PaddingValues(vertical = 10.dp, horizontal = 6.dp), spacing = 4.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            IconBadge(icon, tint = tint, background = background, size = 30.dp)
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = Muted, maxLines = 1)
        }
    }
}

@Composable
private fun MetricTabs(selected: DayMetric, onSelect: (DayMetric) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFE9ECF5))
            .padding(4.dp),
    ) {
        for (metric in DayMetric.entries) {
            val active = metric == selected
            Text(
                text = stringResource(metric.labelRes()),
                style = MaterialTheme.typography.labelLarge,
                color = if (active) Color.White else Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (active) Brand else Color.Transparent)
                    .clickable { onSelect(metric) }
                    .padding(vertical = 9.dp),
            )
        }
    }
}

@Composable
private fun HourlyChart(
    hourly: List<HourlyForecast>,
    metric: DayMetric,
    selected: Int,
    isToday: Boolean,
    onSelect: (Int) -> Unit,
) {
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = Muted, fontWeight = FontWeight.SemiBold)
    val selectedStyle = MaterialTheme.typography.labelMedium.copy(color = Ink, fontWeight = FontWeight.Bold)
    val totalWidth = ColumnWidth * hourly.size
    HavaCard(contentPadding = PaddingValues(vertical = 12.dp), spacing = 0.dp) {
        Box(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(totalWidth)
                    .pointerInput(hourly.size) {
                        detectTapGestures { offset ->
                            onSelect((offset.x / ColumnWidth.toPx()).toInt().coerceIn(0, hourly.lastIndex))
                        }
                    },
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val column = ColumnWidth.toPx()
                    drawRoundRect(
                        color = BrandSoft,
                        topLeft = Offset(column * selected + 3.dp.toPx(), 0f),
                        size = Size(column - 6.dp.toPx(), size.height),
                        cornerRadius = CornerRadius(16.dp.toPx()),
                    )
                }
                Column {
                    Row {
                        hourly.forEachIndexed { index, item ->
                            Column(
                                modifier = Modifier
                                    .width(ColumnWidth)
                                    .padding(top = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = if (isToday && index == 0) stringResource(R.string.now) else hourLabel(item.time),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (index == selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (index == selected) Brand else Muted,
                                )
                                WeatherGlyph(item.condition, Modifier.size(28.dp), isDay = item.isDay)
                            }
                        }
                    }
                    Canvas(modifier = Modifier.width(totalWidth).height(ChartHeight)) {
                        when (metric) {
                            DayMetric.TEMPERATURE -> drawLineChart(
                                values = hourly.map { it.temperature },
                                labels = hourly.map { "${it.temperature.roundToInt()}°" },
                                selected = selected,
                                lineBrush = Brush.verticalGradient(
                                    listOf(Danger, Warning, Info),
                                    startY = 24.dp.toPx(),
                                    endY = size.height - 12.dp.toPx(),
                                ),
                                accent = Warning,
                                measurer = measurer,
                                labelStyle = labelStyle,
                                selectedStyle = selectedStyle,
                            )
                            DayMetric.WIND -> drawLineChart(
                                values = hourly.map { it.windSpeed ?: 0.0 },
                                labels = hourly.map { it.windSpeed?.roundToInt()?.toString() ?: "–" },
                                selected = selected,
                                lineBrush = Brush.verticalGradient(listOf(WindColor, WindColor)),
                                accent = WindColor,
                                measurer = measurer,
                                labelStyle = labelStyle,
                                selectedStyle = selectedStyle,
                                minRange = 10.0,
                                floorAtZero = true,
                            )
                            DayMetric.PRECIPITATION -> drawPrecipitationBars(
                                probabilities = hourly.map { it.precipitationProbability },
                                selected = selected,
                                measurer = measurer,
                                labelStyle = labelStyle,
                                selectedStyle = selectedStyle,
                            )
                        }
                    }
                    Row(modifier = Modifier.padding(bottom = 8.dp)) {
                        hourly.forEachIndexed { index, item ->
                            Box(modifier = Modifier.width(ColumnWidth).height(22.dp), contentAlignment = Alignment.Center) {
                                BottomLabel(item, metric, index == selected)
                            }
                        }
                    }
                }
            }
        }
        Text(
            text = stringResource(metric.unitRes()),
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp),
        )
    }
}

@Composable
private fun BottomLabel(item: HourlyForecast, metric: DayMetric, selected: Boolean) {
    val color = if (selected) Ink else Muted
    when (metric) {
        DayMetric.TEMPERATURE -> {
            val chance = item.precipitationProbability ?: return
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = Info.copy(alpha = 0.7f), modifier = Modifier.size(10.dp))
                Text(percentTr(chance), style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
        DayMetric.PRECIPITATION -> Text(
            text = item.precipitation?.takeIf { it > 0.0 }?.let { "${oneDecimal(it)} mm" } ?: "–",
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
        DayMetric.WIND -> {
            val direction = item.windDirection ?: return
            Icon(
                Icons.Filled.Navigation,
                contentDescription = null,
                tint = if (selected) WindColor else WindColor.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(16.dp)
                    .rotate((direction + 180f) % 360f),
            )
        }
    }
}

private fun DrawScope.drawLineChart(
    values: List<Double>,
    labels: List<String>,
    selected: Int,
    lineBrush: Brush,
    accent: Color,
    measurer: TextMeasurer,
    labelStyle: TextStyle,
    selectedStyle: TextStyle,
    minRange: Double = 4.0,
    floorAtZero: Boolean = false,
) {
    if (values.isEmpty()) return
    val column = ColumnWidth.toPx()
    val top = 26.dp.toPx()
    val bottom = size.height - 14.dp.toPx()
    val low = if (floorAtZero) 0.0 else values.min()
    val high = maxOf(values.max(), low + minRange)
    val points = values.mapIndexed { index, value ->
        val fraction = ((value - low) / (high - low)).toFloat()
        Offset(column * index + column / 2f, bottom - fraction * (bottom - top))
    }
    val line = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            val previous = points[i - 1]
            val current = points[i]
            val midX = (previous.x + current.x) / 2f
            cubicTo(midX, previous.y, midX, current.y, current.x, current.y)
        }
    }
    val fill = Path().apply {
        addPath(line)
        lineTo(points.last().x, size.height)
        lineTo(points.first().x, size.height)
        close()
    }
    drawPath(fill, Brush.verticalGradient(listOf(accent.copy(alpha = 0.28f), accent.copy(alpha = 0f)), startY = top, endY = size.height))
    drawLine(CardBorder, Offset(0f, size.height - 1f), Offset(size.width, size.height - 1f), strokeWidth = 1.dp.toPx())
    drawPath(line, lineBrush, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
    points.forEachIndexed { index, point ->
        val isSelected = index == selected
        if (isSelected) {
            drawLine(
                accent.copy(alpha = 0.35f),
                Offset(point.x, point.y),
                Offset(point.x, size.height),
                strokeWidth = 1.5.dp.toPx(),
            )
        }
        drawCircle(Color.White, radius = if (isSelected) 6.5.dp.toPx() else 4.dp.toPx(), center = point)
        drawCircle(
            brush = lineBrush,
            radius = if (isSelected) 6.5.dp.toPx() else 4.dp.toPx(),
            center = point,
            style = Stroke(width = if (isSelected) 3.dp.toPx() else 2.dp.toPx()),
        )
        drawCenteredText(measurer, labels[index], if (isSelected) selectedStyle else labelStyle, point.x, point.y - 10.dp.toPx())
    }
}

private fun DrawScope.drawPrecipitationBars(
    probabilities: List<Int?>,
    selected: Int,
    measurer: TextMeasurer,
    labelStyle: TextStyle,
    selectedStyle: TextStyle,
) {
    val column = ColumnWidth.toPx()
    val top = 26.dp.toPx()
    val bottom = size.height - 2.dp.toPx()
    val barWidth = column * 0.42f
    drawLine(CardBorder, Offset(0f, size.height - 1f), Offset(size.width, size.height - 1f), strokeWidth = 1.dp.toPx())
    probabilities.forEachIndexed { index, probability ->
        val centerX = column * index + column / 2f
        val isSelected = index == selected
        val value = probability ?: 0
        val height = maxOf((bottom - top) * value / 100f, 3.dp.toPx())
        val barTop = bottom - height
        drawRoundRect(
            color = Info.copy(alpha = 0.12f),
            topLeft = Offset(centerX - barWidth / 2f, top),
            size = Size(barWidth, bottom - top),
            cornerRadius = CornerRadius(barWidth / 2f),
        )
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Info, Info.copy(alpha = if (isSelected) 0.9f else 0.55f)),
                startY = barTop,
                endY = bottom,
            ),
            topLeft = Offset(centerX - barWidth / 2f, barTop),
            size = Size(barWidth, height),
            cornerRadius = CornerRadius(barWidth / 2f),
            alpha = if (isSelected) 1f else 0.75f,
        )
        val label = probability?.let(::percentTr) ?: "–"
        drawCenteredText(measurer, label, if (isSelected) selectedStyle else labelStyle, centerX, top - 6.dp.toPx())
    }
}

private fun DrawScope.drawCenteredText(
    measurer: TextMeasurer,
    text: String,
    style: TextStyle,
    centerX: Float,
    bottomY: Float,
) {
    val layout = measurer.measure(text, style)
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(centerX - layout.size.width / 2f, (bottomY - layout.size.height).coerceAtLeast(0f)),
    )
}

@Composable
private fun HourDetailCard(forecast: HourlyForecast, label: String) {
    val none = stringResource(R.string.no_value)
    val directions = stringArrayResource(R.array.compass_directions)
    HavaCard(spacing = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WeatherGlyph(forecast.condition, Modifier.size(54.dp), isDay = forecast.isDay)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(weatherDescriptionRes(forecast.weatherCode)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            Text(
                stringResource(R.string.temperature_value, forecast.temperature.roundToInt()),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        val uvLevels = stringArrayResource(R.array.uv_levels)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
            DetailCell(
                icon = Icons.Filled.Thermostat,
                tint = Danger,
                label = stringResource(R.string.feels_like),
                value = forecast.apparentTemperature?.let { stringResource(R.string.temperature_value, it.roundToInt()) } ?: none,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            DetailCell(
                icon = Icons.Filled.WaterDrop,
                tint = Info,
                label = stringResource(R.string.precipitation),
                value = forecast.precipitationProbability?.let(::percentTr) ?: none,
                caption = forecast.precipitation?.let { "${oneDecimal(it)} mm" },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
            DetailCell(
                icon = Icons.Filled.Air,
                tint = WindColor,
                label = stringResource(R.string.wind),
                value = forecast.windSpeed?.let { stringResource(R.string.wind_kmh, it.roundToInt()) } ?: none,
                caption = forecast.windDirection?.let { directions[compassIndex(it)] },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            DetailCell(
                icon = Icons.Filled.WbSunny,
                tint = Warning,
                label = stringResource(R.string.uv_index),
                value = forecast.uvIndex?.roundToInt()?.toString() ?: none,
                caption = forecast.uvIndex?.let { uvLevels[uvLevelIndex(it)] },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun DetailCell(
    icon: ImageVector,
    tint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    size: Dp = 32.dp,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PageBackground)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        IconBadge(icon, tint = tint, background = Color.White, size = size)
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Muted, maxLines = 1)
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1)
            if (caption != null) {
                Text(caption, style = MaterialTheme.typography.labelSmall, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun hourTitle(forecast: HourlyForecast, isNow: Boolean): String =
    if (isNow) stringResource(R.string.now) else hourLabel(forecast.time)

private fun DayMetric.labelRes(): Int = when (this) {
    DayMetric.TEMPERATURE -> R.string.day_chart_temperature
    DayMetric.PRECIPITATION -> R.string.day_chart_precipitation
    DayMetric.WIND -> R.string.day_chart_wind
}

private fun DayMetric.unitRes(): Int = when (this) {
    DayMetric.TEMPERATURE -> R.string.day_chart_temperature_note
    DayMetric.PRECIPITATION -> R.string.day_chart_precipitation_note
    DayMetric.WIND -> R.string.day_chart_wind_note
}

@Preview(widthDp = 360, showBackground = true, backgroundColor = 0xFFF5F7FC)
@Composable
private fun DayHourlyContentPreview() {
    DayHourlyContent(
        day = PreviewData.report.daily[1],
        hourly = PreviewData.report.hourly,
        isToday = false,
    )
}
