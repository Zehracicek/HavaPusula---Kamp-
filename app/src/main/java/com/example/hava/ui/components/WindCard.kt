package com.example.hava.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hava.R
import com.example.hava.data.model.CurrentWeather
import com.example.hava.data.model.DailyForecast
import kotlin.math.roundToInt

private const val BEAUFORT_MAX = 12
private val CompassSize = 96.dp
private val BeaufortColors = listOf(Color(0xFF81C784), Color(0xFFFFF176), Color(0xFFFFB74D), Color(0xFFE57373))

@Composable
fun WindCard(
    weather: CurrentWeather,
    today: DailyForecast?,
    modifier: Modifier = Modifier,
) {
    val beaufort = beaufortScale(weather.windSpeed)
    val beaufortName = stringArrayResource(R.array.beaufort_names)[beaufort]
    val directions = stringArrayResource(R.array.compass_directions)

    WeatherCard(modifier = modifier, title = stringResource(R.string.wind)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WindCompass(directionDegrees = weather.windDirection)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) {},
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = weather.windSpeed.roundToInt().toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.wind_unit),
                        style = MaterialTheme.typography.titleMedium,
                        color = LocalContentColor.current.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.beaufort_level, beaufort, beaufortName),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                BeaufortBar(level = beaufort)
                weather.windDirection?.let { degrees ->
                    Text(
                        text = stringResource(
                            R.string.wind_from_direction,
                            directions[compassIndex(degrees)],
                            degrees,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalContentColor.current.copy(alpha = 0.8f),
                    )
                }
            }
        }

        HorizontalDivider(color = LocalContentColor.current.copy(alpha = 0.15f))

        val noValue = stringResource(R.string.no_value)
        Row(modifier = Modifier.fillMaxWidth()) {
            WindStat(
                label = stringResource(R.string.wind_gusts),
                value = weather.windGusts?.let { stringResource(R.string.wind_value, it.roundToInt()) } ?: noValue,
                modifier = Modifier.weight(1f),
            )
            WindStat(
                label = stringResource(R.string.wind_max_today),
                value = today?.maxWindSpeed?.let { stringResource(R.string.wind_value, it.roundToInt()) } ?: noValue,
                modifier = Modifier.weight(1f),
            )
            WindStat(
                label = stringResource(R.string.wind_gusts_max_today),
                value = today?.maxWindGusts?.let { stringResource(R.string.wind_value, it.roundToInt()) } ?: noValue,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Ok rüzgârın gittiği yönü gösterir; API ise geldiği yönü verir. */
@Composable
private fun WindCompass(directionDegrees: Int?, modifier: Modifier = Modifier) {
    val contentColor = LocalContentColor.current
    Box(modifier = modifier.size(CompassSize).clearAndSetSemantics {}) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            drawCircle(color = contentColor.copy(alpha = 0.3f), radius = radius - 1.dp.toPx(), style = Stroke(1.dp.toPx()))
            for (i in 0 until 8) {
                rotate(i * 45f) {
                    val tickLength = if (i % 2 == 0) 6.dp.toPx() else 3.dp.toPx()
                    drawLine(
                        color = contentColor.copy(alpha = 0.6f),
                        start = Offset(center.x, 1.dp.toPx()),
                        end = Offset(center.x, 1.dp.toPx() + tickLength),
                        strokeWidth = 1.5.dp.toPx(),
                    )
                }
            }
            if (directionDegrees != null) {
                rotate(directionDegrees + 180f) {
                    // Yön harflerinin üstüne taşmayacak uzunlukta
                    val tip = Offset(center.x, center.y - radius * 0.42f)
                    val tail = Offset(center.x, center.y + radius * 0.42f)
                    drawLine(color = contentColor, start = tail, end = tip, strokeWidth = 2.5.dp.toPx())
                    val head = Path().apply {
                        moveTo(tip.x, tip.y - 4.dp.toPx())
                        lineTo(tip.x - 7.dp.toPx(), tip.y + 9.dp.toPx())
                        lineTo(tip.x + 7.dp.toPx(), tip.y + 9.dp.toPx())
                        close()
                    }
                    drawPath(head, color = contentColor)
                    drawCircle(color = contentColor, radius = 3.dp.toPx(), center = tail)
                }
            }
        }
        CompassLetter("K", Alignment.TopCenter)
        CompassLetter("D", Alignment.CenterEnd)
        CompassLetter("G", Alignment.BottomCenter)
        CompassLetter("B", Alignment.CenterStart)
    }
}

@Composable
private fun BoxScope.CompassLetter(text: String, alignment: Alignment) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = LocalContentColor.current.copy(alpha = 0.85f),
        modifier = Modifier
            .align(alignment)
            .padding(10.dp),
    )
}

@Composable
private fun BeaufortBar(level: Int, modifier: Modifier = Modifier) {
    val trackColor = LocalContentColor.current.copy(alpha = 0.2f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp),
    ) {
        val radius = CornerRadius(size.height / 2)
        drawRoundRect(color = trackColor, cornerRadius = radius)
        val fraction = (level.toFloat() / BEAUFORT_MAX).coerceAtLeast(0.04f)
        drawRoundRect(
            brush = Brush.horizontalGradient(BeaufortColors, startX = 0f, endX = size.width),
            size = Size(size.width * fraction, size.height),
            cornerRadius = radius,
        )
    }
}

@Composable
private fun WindStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = LocalContentColor.current.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Preview(widthDp = 320, showBackground = true, backgroundColor = 0xFF1A73E8)
@Composable
private fun WindCardPreview() {
    WindCard(PreviewData.report.current, PreviewData.report.daily.first())
}
