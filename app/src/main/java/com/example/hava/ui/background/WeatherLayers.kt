package com.example.hava.ui.background

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

private const val TWO_PI = (PI * 2).toFloat()

private const val SUN_RAY_COUNT = 10
private const val STAR_COUNT = 90
private const val CLOUD_COUNT = 8
private const val RAIN_MAX_DROPS = 180
private const val RAIN_SLANT = -0.12f
private const val SNOW_MAX_FLAKES = 120

private val MoonColor = Color(0xFFF4F1D0)
private val RainColor = Color(0xFFD4E4F2)

// region Güneş

@Composable
internal fun SunLayer(
    time: () -> Float,
    alpha: () -> Float,
    color: () -> Color,
    height: () -> Float,
) {
    Canvas(Modifier.fillMaxSize()) {
        val a = alpha()
        val t = time()
        val c = color()
        val sunCenter = Offset(size.width * 0.8f, size.height * height())

        val glowRadius = size.minDimension * 0.8f * (1f + 0.05f * sin(t * 0.7f))
        drawCircle(
            brush = Brush.radialGradient(
                0f to c.copy(alpha = 0.55f * a),
                0.3f to c.copy(alpha = 0.18f * a),
                1f to Color.Transparent,
                center = sunCenter,
                radius = glowRadius,
            ),
            radius = glowRadius,
            center = sunCenter,
        )

        val rayLength = size.minDimension * 0.95f
        val rayWidth = 26.dp.toPx()
        rotate(degrees = t * 2.5f, pivot = sunCenter) {
            for (i in 0 until SUN_RAY_COUNT) {
                val angle = i * TWO_PI / SUN_RAY_COUNT
                val end = sunCenter + Offset(cos(angle), sin(angle)) * rayLength
                val shimmer = 0.6f + 0.4f * sin(t * 0.9f + i * 1.7f)
                drawLine(
                    brush = Brush.linearGradient(
                        listOf(c.copy(alpha = 0.1f * a * shimmer), Color.Transparent),
                        start = sunCenter,
                        end = end,
                    ),
                    start = sunCenter,
                    end = end,
                    strokeWidth = rayWidth,
                    cap = StrokeCap.Round,
                )
            }
        }

        val coreRadius = 44.dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                0f to Color.White.copy(alpha = a),
                0.4f to c.copy(alpha = 0.9f * a),
                1f to Color.Transparent,
                center = sunCenter,
                radius = coreRadius,
            ),
            radius = coreRadius,
            center = sunCenter,
        )
    }
}

// endregion

// region Ay ve yıldızlar

@Composable
internal fun MoonLayer(time: () -> Float, alpha: () -> Float) {
    Spacer(
        Modifier
            .fillMaxSize()
            .drawWithCache {
                val radius = 22.dp.toPx()
                val moonCenter = Offset(size.width * 0.16f, size.height * 0.1f)
                val crescent = Path.combine(
                    PathOperation.Difference,
                    Path().apply { addOval(Rect(moonCenter, radius)) },
                    Path().apply {
                        addOval(Rect(moonCenter + Offset(radius * 0.5f, -radius * 0.25f), radius * 0.9f))
                    },
                )
                onDrawBehind {
                    val a = alpha()
                    val glowRadius = radius * 5f * (1f + 0.04f * sin(time() * 0.5f))
                    drawCircle(
                        brush = Brush.radialGradient(
                            0f to MoonColor.copy(alpha = 0.3f * a),
                            1f to Color.Transparent,
                            center = moonCenter,
                            radius = glowRadius,
                        ),
                        radius = glowRadius,
                        center = moonCenter,
                    )
                    drawPath(crescent, MoonColor.copy(alpha = a))
                }
            }
    )
}

private class Star(
    val x: Float,
    val y: Float,
    val radiusDp: Float,
    val twinkleSpeed: Float,
    val phase: Float,
)

@Composable
internal fun StarLayer(time: () -> Float, alpha: () -> Float) {
    val stars = remember {
        val random = Random(7)
        List(STAR_COUNT) {
            Star(
                x = random.nextFloat(),
                // Ufka doğru seyrelsin
                y = random.nextFloat().pow(1.4f) * 0.7f,
                radiusDp = 0.5f + random.nextFloat() * 1.3f,
                twinkleSpeed = 0.6f + random.nextFloat() * 1.6f,
                phase = random.nextFloat() * TWO_PI,
            )
        }
    }

    Canvas(Modifier.fillMaxSize()) {
        val a = alpha()
        val t = time()
        for (star in stars) {
            val twinkle = 0.55f + 0.45f * sin(t * star.twinkleSpeed + star.phase)
            drawCircle(
                color = Color.White.copy(alpha = a * twinkle),
                radius = star.radiusDp.dp.toPx(),
                center = Offset(star.x * size.width, star.y * size.height),
            )
        }
    }
}

// endregion

// region Bulutlar

private class Cloud(
    val y: Float,
    val scale: Float,
    val speed: Float,
    val offset: Float,
    val alpha: Float,
)

private class Puff(val dx: Float, val dy: Float, val radius: Float)

private val CloudShape = listOf(
    Puff(0f, 0f, 1f),
    Puff(-0.85f, 0.2f, 0.75f),
    Puff(0.9f, 0.18f, 0.8f),
    Puff(-0.35f, -0.35f, 0.8f),
    Puff(0.4f, -0.3f, 0.7f),
    Puff(1.6f, 0.3f, 0.55f),
    Puff(-1.5f, 0.32f, 0.5f),
)

@Composable
internal fun CloudLayer(time: () -> Float, cover: () -> Float, color: () -> Color) {
    val clouds = remember {
        val random = Random(11)
        List(CLOUD_COUNT) {
            val scale = 0.6f + random.nextFloat() * 0.8f
            Cloud(
                y = 0.02f + random.nextFloat() * 0.4f,
                scale = scale,
                // Büyük (yakın) bulutlar biraz daha hızlı: derinlik hissi
                speed = 0.003f + 0.005f * scale,
                offset = random.nextFloat(),
                alpha = 0.55f + random.nextFloat() * 0.45f,
            )
        }
    }

    Canvas(Modifier.fillMaxSize()) {
        val c = cover()
        val t = time()
        val tint = color()

        clouds.forEachIndexed { index, cloud ->
            // Kapalılık arttıkça bulutlar tek tek belirir
            val visibility = (c * clouds.size - index).coerceIn(0f, 1f)
            if (visibility <= 0f) return@forEachIndexed

            val baseRadius = size.width * 0.15f * cloud.scale
            val span = size.width + baseRadius * 5f
            val x = ((cloud.offset + t * cloud.speed) % 1f) * span - baseRadius * 2.5f
            val y = size.height * cloud.y
            val a = cloud.alpha * visibility

            for (puff in CloudShape) {
                val puffCenter = Offset(x + puff.dx * baseRadius, y + puff.dy * baseRadius)
                val radius = baseRadius * puff.radius
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to tint.copy(alpha = 0.75f * a),
                        0.6f to tint.copy(alpha = 0.45f * a),
                        1f to Color.Transparent,
                        center = puffCenter,
                        radius = radius,
                    ),
                    radius = radius,
                    center = puffCenter,
                )
            }
        }
    }
}

// endregion

// region Yağmur ve kar

private class Drop(
    val x: Float,
    val speed: Float,
    val lengthDp: Float,
    val widthDp: Float,
    val alpha: Float,
    val phase: Float,
)

@Composable
internal fun RainLayer(time: () -> Float, intensity: () -> Float) {
    val drops = remember {
        val random = Random(23)
        List(RAIN_MAX_DROPS) {
            Drop(
                // Eğik düştükleri için sağ kenarın biraz dışından da başlıyorlar
                x = random.nextFloat() * 1.15f,
                speed = 0.75f + random.nextFloat() * 0.55f,
                lengthDp = 14f + random.nextFloat() * 16f,
                widthDp = 1f + random.nextFloat() * 0.8f,
                alpha = 0.25f + random.nextFloat() * 0.35f,
                phase = random.nextFloat(),
            )
        }
    }

    Canvas(Modifier.fillMaxSize()) {
        val level = intensity()
        val t = time()
        val count = (RAIN_MAX_DROPS * level).toInt()
        val layerAlpha = (level * 1.6f).coerceAtMost(1f)

        for (i in 0 until count) {
            val drop = drops[i]
            val length = drop.lengthDp.dp.toPx()
            val progress = (drop.phase + t * drop.speed) % 1f
            val y = progress * (size.height + length) - length
            val x = drop.x * size.width + RAIN_SLANT * y
            drawLine(
                color = RainColor.copy(alpha = drop.alpha * layerAlpha),
                start = Offset(x, y),
                end = Offset(x + RAIN_SLANT * length, y + length),
                strokeWidth = drop.widthDp.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

private class Flake(
    val x: Float,
    val speed: Float,
    val radiusDp: Float,
    val swayDp: Float,
    val swaySpeed: Float,
    val phase: Float,
    val alpha: Float,
)

@Composable
internal fun SnowLayer(time: () -> Float, intensity: () -> Float) {
    val flakes = remember {
        val random = Random(31)
        List(SNOW_MAX_FLAKES) {
            val size = random.nextFloat().pow(2)
            Flake(
                x = random.nextFloat(),
                // Büyük taneler öne yakın: daha hızlı ve daha opak
                speed = 0.03f + size * 0.05f,
                radiusDp = 1.2f + size * 2.8f,
                swayDp = 6f + random.nextFloat() * 16f,
                swaySpeed = 0.4f + random.nextFloat() * 0.7f,
                phase = random.nextFloat(),
                alpha = 0.5f + size * 0.45f,
            )
        }
    }

    Canvas(Modifier.fillMaxSize()) {
        val level = intensity()
        val t = time()
        val count = (SNOW_MAX_FLAKES * level).toInt()
        val layerAlpha = (level * 1.6f).coerceAtMost(1f)

        for (i in 0 until count) {
            val flake = flakes[i]
            val radius = flake.radiusDp.dp.toPx()
            val progress = (flake.phase + t * flake.speed) % 1f
            val y = progress * (size.height + radius * 2) - radius
            val x = flake.x * size.width +
                sin(t * flake.swaySpeed + flake.phase * TWO_PI) * flake.swayDp.dp.toPx()
            drawCircle(
                color = Color.White.copy(alpha = flake.alpha * layerAlpha),
                radius = radius,
                center = Offset(x, y),
            )
        }
    }
}

// endregion

// region Şimşek ve sis

@Composable
internal fun LightningLayer(alpha: () -> Float) {
    val flash = remember { Animatable(0f) }
    var bolt by remember { mutableStateOf(emptyList<Offset>()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(5_000, 11_000))
            bolt = generateBolt()
            flash.snapTo(0f)
            flash.animateTo(1f, tween(70))
            flash.animateTo(0.25f, tween(110))
            flash.animateTo(0.8f, tween(60))
            flash.animateTo(0f, tween(650, easing = LinearOutSlowInEasing))
        }
    }

    Canvas(Modifier.fillMaxSize()) {
        val f = flash.value * alpha()
        if (f <= 0.001f) return@Canvas

        drawRect(Color.White.copy(alpha = 0.2f * f))
        if (bolt.isEmpty()) return@Canvas

        val path = Path().apply {
            bolt.forEachIndexed { index, point ->
                val x = point.x * size.width
                val y = point.y * size.height
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.3f * f),
            style = Stroke(width = 10.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round),
        )
        drawPath(
            path = path,
            color = Color.White.copy(alpha = f),
            style = Stroke(width = 2.5.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round),
        )
    }
}

/** Ekranın üst kısmında, oransal koordinatlarda zikzak bir şimşek çizgisi üretir. */
private fun generateBolt(): List<Offset> {
    var x = 0.2f + Random.nextFloat() * 0.6f
    val segments = 9
    return List(segments + 1) { i ->
        if (i > 0) x += (Random.nextFloat() - 0.5f) * 0.09f
        Offset(x, i * 0.045f)
    }
}

@Composable
internal fun HazeLayer(alpha: () -> Float) {
    Canvas(Modifier.fillMaxSize()) {
        val a = alpha()
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.White.copy(alpha = 0.08f * a),
                0.5f to Color.White.copy(alpha = 0.3f * a),
                1f to Color.White.copy(alpha = 0.18f * a),
            )
        )
    }
}

/** Metinlerin her sahnede okunabilir kalması için üst ve alt kenarları hafifçe koyulaştırır. */
@Composable
internal fun ReadabilityScrim() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Black.copy(alpha = 0.22f),
                0.35f to Color.Transparent,
                0.7f to Color.Transparent,
                1f to Color.Black.copy(alpha = 0.25f),
            )
        )
    }
}

// endregion
