package com.example.hava.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.hava.data.model.WeatherCondition

private const val GLYPH_SCALE = 1.3f

@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    contentDescription: String,
    modifier: Modifier = Modifier,
    isDay: Boolean = true,
    size: TextUnit = 24.sp,
) {
    val side = with(LocalDensity.current) { size.toDp() } * GLYPH_SCALE
    WeatherGlyph(
        condition = condition,
        isDay = isDay,
        modifier = modifier
            .size(side)
            .clearAndSetSemantics { this.contentDescription = contentDescription },
    )
}

@Composable
fun WeatherGlyph(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    isDay: Boolean = true,
) {
    Canvas(modifier) {
        when (condition) {
            WeatherCondition.CLEAR -> if (isDay) sun(Offset(0.5f, 0.5f), 0.22f) else moon(Offset(0.5f, 0.5f), 0.32f)
            WeatherCondition.PARTLY_CLOUDY -> {
                if (isDay) sun(Offset(0.62f, 0.38f), 0.18f) else moon(Offset(0.64f, 0.36f), 0.24f)
                cloud(Rect(0.06f, 0.42f, 0.8f, 0.88f))
            }
            WeatherCondition.CLOUDY -> {
                cloud(Rect(0.34f, 0.2f, 0.96f, 0.62f), shade = CloudShade.BACK)
                cloud(Rect(0.04f, 0.38f, 0.78f, 0.86f))
            }
            WeatherCondition.FOG -> {
                cloud(Rect(0.12f, 0.12f, 0.88f, 0.58f), shade = CloudShade.BACK)
                fogLines()
            }
            WeatherCondition.RAIN -> {
                cloud(Rect(0.08f, 0.1f, 0.92f, 0.62f))
                raindrops()
            }
            WeatherCondition.STORM -> {
                cloud(Rect(0.08f, 0.08f, 0.92f, 0.6f), shade = CloudShade.STORM)
                bolt()
            }
            WeatherCondition.SNOW -> {
                cloud(Rect(0.08f, 0.1f, 0.92f, 0.62f))
                snowflakes()
            }
        }
    }
}

private enum class CloudShade { FRONT, BACK, STORM }

private val SunCore = listOf(Color(0xFFFFE27A), Color(0xFFFFB321))
private val SunRay = Color(0xFFFFBE2E)
private val MoonColors = listOf(Color(0xFFFFF4C2), Color(0xFFF5D76E))
private val RainColor = Color(0xFF4B9DF2)
private val SnowColor = Color(0xFF8EC5FF)
private val BoltColor = Color(0xFFFFC42E)
private val FogColor = Color(0xFFB4C1D3)
private val CloudShadow = Color(0x1F2B3D63)

private fun DrawScope.unit(): Float = size.minDimension

private fun DrawScope.at(fraction: Offset): Offset {
    val u = unit()
    val dx = (size.width - u) / 2f
    val dy = (size.height - u) / 2f
    return Offset(dx + fraction.x * u, dy + fraction.y * u)
}

private fun DrawScope.sun(center: Offset, radius: Float) {
    val u = unit()
    val c = at(center)
    val r = radius * u
    for (i in 0 until 8) {
        rotate(degrees = i * 45f, pivot = c) {
            drawLine(
                color = SunRay,
                start = Offset(c.x, c.y - r * 1.38f),
                end = Offset(c.x, c.y - r * 1.82f),
                strokeWidth = u * 0.055f,
                cap = StrokeCap.Round,
            )
        }
    }
    drawCircle(
        brush = Brush.radialGradient(SunCore, center = Offset(c.x - r * 0.3f, c.y - r * 0.3f), radius = r * 1.5f),
        radius = r,
        center = c,
    )
}

private fun DrawScope.moon(center: Offset, radius: Float) {
    val u = unit()
    val c = at(center)
    val r = radius * u
    val full = Path().apply { addOval(Rect(c, r)) }
    val bite = Path().apply { addOval(Rect(Offset(c.x + r * 0.55f, c.y - r * 0.35f), r * 0.85f)) }
    val crescent = Path().apply { op(full, bite, PathOperation.Difference) }
    drawPath(crescent, Brush.linearGradient(MoonColors, start = Offset(c.x - r, c.y - r), end = Offset(c.x + r, c.y + r)))
}

private fun DrawScope.cloud(area: Rect, shade: CloudShade = CloudShade.FRONT) {
    val u = unit()
    val origin = at(Offset(area.left, area.top))
    val w = area.width * u
    val h = area.height * u
    val path = Path().apply {
        addOval(Rect(Offset(origin.x + w * 0.3f, origin.y + h * 0.58f), h * 0.3f))
        addOval(Rect(Offset(origin.x + w * 0.56f, origin.y + h * 0.44f), h * 0.42f))
        addRoundRect(
            RoundRect(
                left = origin.x + w * 0.04f,
                top = origin.y + h * 0.5f,
                right = origin.x + w * 0.96f,
                bottom = origin.y + h,
                radiusX = h * 0.26f,
                radiusY = h * 0.26f,
            ),
        )
    }
    val colors = when (shade) {
        CloudShade.FRONT -> listOf(Color.White, Color(0xFFDCE7F3))
        CloudShade.BACK -> listOf(Color(0xFFE6EDF6), Color(0xFFB9C7D9))
        CloudShade.STORM -> listOf(Color(0xFFA9B5C8), Color(0xFF6E7C94))
    }
    translate(top = u * 0.025f) { drawPath(path, CloudShadow) }
    drawPath(path, Brush.verticalGradient(colors, startY = origin.y, endY = origin.y + h))
}

private fun DrawScope.raindrops() {
    val u = unit()
    listOf(0.32f, 0.52f, 0.72f).forEachIndexed { index, x ->
        val top = at(Offset(x, 0.7f + if (index == 1) 0.06f else 0f))
        drawLine(
            color = RainColor,
            start = top,
            end = Offset(top.x - u * 0.06f, top.y + u * 0.14f),
            strokeWidth = u * 0.06f,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.snowflakes() {
    val u = unit()
    listOf(Offset(0.3f, 0.78f), Offset(0.52f, 0.86f), Offset(0.74f, 0.78f)).forEach { point ->
        val c = at(point)
        for (i in 0 until 3) {
            rotate(degrees = i * 60f, pivot = c) {
                drawLine(
                    color = SnowColor,
                    start = Offset(c.x, c.y - u * 0.06f),
                    end = Offset(c.x, c.y + u * 0.06f),
                    strokeWidth = u * 0.035f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

private fun DrawScope.bolt() {
    val p = { x: Float, y: Float -> at(Offset(x, y)) }
    val path = Path().apply {
        val a = p(0.54f, 0.5f)
        moveTo(a.x, a.y)
        p(0.38f, 0.76f).let { lineTo(it.x, it.y) }
        p(0.5f, 0.76f).let { lineTo(it.x, it.y) }
        p(0.42f, 0.98f).let { lineTo(it.x, it.y) }
        p(0.66f, 0.68f).let { lineTo(it.x, it.y) }
        p(0.53f, 0.68f).let { lineTo(it.x, it.y) }
        p(0.62f, 0.5f).let { lineTo(it.x, it.y) }
        close()
    }
    drawPath(path, BoltColor)
}

private fun DrawScope.fogLines() {
    val u = unit()
    listOf(0.68f to 0.16f, 0.8f to 0.26f, 0.92f to 0.2f).forEach { (y, inset) ->
        val start = at(Offset(inset, y))
        val end = at(Offset(1f - inset, y))
        drawLine(FogColor, start, end, strokeWidth = u * 0.06f, cap = StrokeCap.Round)
    }
}