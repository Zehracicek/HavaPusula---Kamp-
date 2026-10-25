package com.example.hava.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hava.R
import com.example.hava.ui.theme.Brand
import com.example.hava.ui.theme.CardBorder
import com.example.hava.ui.theme.FieldBorder
import com.example.hava.ui.theme.Ink
import com.example.hava.ui.theme.Muted

val CardShape = RoundedCornerShape(20.dp)

/** Görseldeki beyaz, ince kenarlıklı ve hafif gölgeli kart. */
@Composable
fun HavaCard(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    borderColor: Color = CardBorder,
    shape: Shape = CardShape,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    spacing: Dp = 10.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val body: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(spacing),
            content = content,
        )
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = containerColor,
            contentColor = Ink,
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, borderColor),
            content = body,
        )
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = containerColor,
            contentColor = Ink,
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, borderColor),
            content = body,
        )
    }
}

@Composable
fun ScreenTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = if (onBack != null) 6.dp else 18.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Ink)
            }
        }
        if (leading != null) {
            leading()
            Spacer(Modifier.width(10.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() },
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        actions()
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Ink,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        if (action != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Brand,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
fun IconBadge(
    icon: ImageVector,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = size * 0.52f,
    shape: Shape = CircleShape,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun StatusChip(
    text: String,
    color: Color,
    background: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = color,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Takvim ve saat seçimi gibi tıklanabilir alanlar için ikonlu satır. */
@Composable
fun FieldRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        contentColor = Ink,
        border = BorderStroke(1.dp, FieldBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = Brand, modifier = Modifier.size(22.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
        }
    }
}

@Composable
fun ChevronIcon(modifier: Modifier = Modifier, tint: Color = Muted) {
    Icon(
        Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(22.dp),
    )
}

@Composable
fun BulletLine(text: String, bulletColor: Color = Ink, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(bulletColor),
        )
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Ink)
    }
}

/** "92/100" biçiminde puan halkası. */
@Composable
fun ScoreRing(
    score: Int,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 104.dp,
    strokeWidth: Dp = 11.dp,
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            drawArc(color.copy(alpha = 0.16f), -90f, 360f, false, Offset(inset, inset), arcSize, style = stroke)
            drawArc(
                brush = Brush.sweepGradient(listOf(color.copy(alpha = 0.75f), color, color.copy(alpha = 0.75f))),
                startAngle = -90f,
                sweepAngle = 360f * score.coerceIn(0, 100) / 100f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text("$score", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
            Text("/100", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color, modifier = Modifier.padding(bottom = 5.dp))
        }
    }
}

/** Ölçüye göre doldurulan yuvarlak uçlu ilerleme çubuğu. */
@Composable
fun ScoreBar(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.height(6.dp)) {
        val radius = androidx.compose.ui.geometry.CornerRadius(size.height / 2)
        drawRoundRect(color.copy(alpha = 0.15f), cornerRadius = radius)
        drawRoundRect(
            color = color,
            size = Size(size.width * fraction.coerceIn(0.04f, 1f), size.height),
            cornerRadius = radius,
        )
    }
}

data class LandscapePalette(
    val skyTop: Color? = null,
    val skyBottom: Color? = null,
    val clouds: Color? = null,
    val far: Color,
    val snow: Color? = null,
    val water: Color? = null,
    val ground: Color,
    val trees: Color,
)

object Landscapes {
    val Hero = LandscapePalette(
        far = Color(0xFF6D8DB0).copy(alpha = 0.55f),
        ground = Color(0xFF2F5A4B).copy(alpha = 0.7f),
        trees = Color(0xFF1B3B30).copy(alpha = 0.85f),
    )
    val Banner = LandscapePalette(
        skyTop = Color(0xFF8FCBF2),
        skyBottom = Color(0xFFE4F4FD),
        clouds = Color.White,
        far = Color(0xFF86A9CC),
        water = Color(0xFFB9E0F5),
        ground = Color(0xFF7DBB62),
        trees = Color(0xFF2D6A3E),
    )
    val Quote = LandscapePalette(
        skyTop = Color(0xFFF1F6FC),
        skyBottom = Color(0xFFE2EDF8),
        clouds = Color(0xFFCFE0F1),
        far = Color(0xFF8FAACB),
        snow = Color.White,
        water = Color(0xFFCFE4F4),
        ground = Color(0xFF5F8F79),
        trees = Color(0xFF2B5644),
    )
}

/** Fotoğraf yerine kullanılan dağ, göl ve çam ağaçlı manzara. */
@Composable
fun LandscapeScene(palette: LandscapePalette, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) { drawLandscape(palette) }
}

fun DrawScope.drawLandscape(palette: LandscapePalette) {
    val w = size.width
    val h = size.height
    if (palette.skyTop != null && palette.skyBottom != null) {
        drawRect(Brush.verticalGradient(listOf(palette.skyTop, palette.skyBottom)))
    }
    palette.clouds?.let { cloud ->
        drawPuff(cloud, Offset(w * 0.68f, h * 0.16f), w * 0.09f)
        drawPuff(cloud, Offset(w * 0.86f, h * 0.24f), w * 0.06f)
        drawPuff(cloud, Offset(w * 0.2f, h * 0.2f), w * 0.07f)
    }
    val peaks = listOf(
        0f to 0.62f, 0.12f to 0.44f, 0.24f to 0.56f, 0.4f to 0.32f,
        0.56f to 0.52f, 0.7f to 0.38f, 0.86f to 0.5f, 1f to 0.42f,
    )
    val mountains = Path().apply {
        moveTo(0f, h)
        peaks.forEach { (x, y) -> lineTo(x * w, y * h) }
        lineTo(w, h)
        close()
    }
    drawPath(mountains, palette.far)
    palette.snow?.let { snow ->
        listOf(0.4f to 0.32f, 0.7f to 0.38f, 0.12f to 0.44f).forEach { (x, y) ->
            val cap = Path().apply {
                moveTo(x * w, y * h)
                lineTo(x * w - w * 0.045f, y * h + h * 0.08f)
                lineTo(x * w, y * h + h * 0.06f)
                lineTo(x * w + w * 0.04f, y * h + h * 0.075f)
                close()
            }
            drawPath(cap, snow)
        }
    }
    palette.water?.let { water ->
        drawRect(water, topLeft = Offset(0f, h * 0.66f), size = Size(w, h * 0.14f))
    }
    val ground = Path().apply {
        moveTo(0f, h * 0.76f)
        quadraticTo(w * 0.22f, h * 0.7f, w * 0.42f, h * 0.8f)
        quadraticTo(w * 0.7f, h * 0.9f, w, h * 0.74f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(ground, palette.ground)
    val trees = listOf(
        0.03f to 0.34f, 0.08f to 0.44f, 0.13f to 0.3f, 0.18f to 0.38f,
        0.78f to 0.32f, 0.84f to 0.46f, 0.9f to 0.36f, 0.96f to 0.42f,
    )
    trees.forEach { (x, height) -> drawPine(palette.trees, x * w, h * 0.86f, h * height) }
}

private fun DrawScope.drawPuff(color: Color, center: Offset, radius: Float) {
    drawCircle(color, radius, center)
    drawCircle(color, radius * 0.75f, Offset(center.x - radius * 1.05f, center.y + radius * 0.25f))
    drawCircle(color, radius * 0.7f, Offset(center.x + radius * 1.0f, center.y + radius * 0.3f))
    drawRect(
        color,
        topLeft = Offset(center.x - radius * 1.05f, center.y + radius * 0.2f),
        size = Size(radius * 2.05f, radius * 0.75f),
    )
}

private fun DrawScope.drawPine(color: Color, x: Float, base: Float, height: Float) {
    val width = height * 0.42f
    for (tier in 0 until 3) {
        val top = base - height + tier * height * 0.22f
        val bottom = base - height * 0.12f - (2 - tier) * height * 0.18f
        val half = width * (0.55f + tier * 0.22f) / 2f
        val path = Path().apply {
            moveTo(x, top)
            lineTo(x + half, bottom)
            lineTo(x - half, bottom)
            close()
        }
        drawPath(path, color)
    }
    drawRect(color, topLeft = Offset(x - width * 0.06f, base - height * 0.14f), size = Size(width * 0.12f, height * 0.14f))
}
