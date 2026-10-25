package com.example.hava.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hava.R
import com.example.hava.data.local.moonIlluminationPercent
import com.example.hava.data.local.moonPhaseIndex
import com.example.hava.data.model.MoonInfo
import kotlin.math.roundToInt

private val MoonDiskSize = 88.dp
private val MoonColor = Color(0xFFF4F1D0)
private val MoonShadow = Color(0xFF1A2236)

@Composable
fun MoonCard(moon: MoonInfo, modifier: Modifier = Modifier) {
    val phases = stringArrayResource(R.array.moon_phases)
    val noValue = stringResource(R.string.no_value)
    WeatherCard(modifier = modifier, title = stringResource(R.string.moon)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) {},
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = phases[moonPhaseIndex(moon.phase)],
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                MoonFact(
                    label = stringResource(R.string.moon_illumination),
                    value = stringResource(R.string.percent_value, (moon.illumination * 100).roundToInt()),
                )
                MoonFact(
                    label = stringResource(R.string.next_moonset),
                    value = moon.nextSet?.let(::hourLabel) ?: noValue,
                )
                MoonFact(
                    label = stringResource(R.string.next_full_moon),
                    value = if (moon.daysUntilFullMoon == 0) {
                        stringResource(R.string.today)
                    } else {
                        stringResource(R.string.days_later, moon.daysUntilFullMoon)
                    },
                )
            }
            MoonDisk(phase = moon.phase)
        }
    }
}

@Composable
private fun MoonFact(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalContentColor.current.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun MoonDisk(phase: Double, modifier: Modifier = Modifier) {
    val illumination = moonIlluminationPercent(phase) / 100f
    Canvas(modifier = modifier.size(MoonDiskSize)) {
        val radius = size.minDimension / 2
        val disk = Path().apply {
            addOval(Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius))
        }
        clipPath(disk) {
            drawCircle(color = MoonColor, radius = radius)
            val direction = if (phase <= 0.5) -1f else 1f
            drawCircle(
                color = MoonShadow,
                radius = radius,
                center = Offset(center.x + direction * radius * 2f * illumination, center.y),
            )
        }
    }
}

@Preview(widthDp = 320, showBackground = true, backgroundColor = 0xFF1A73E8)
@Composable
private fun MoonCardPreview() {
    MoonCard(PreviewData.report.moon!!)
}
