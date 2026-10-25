package com.example.hava.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hava.R
import com.example.hava.data.local.TurkishPlates
import com.example.hava.data.model.CurrentConditions
import com.example.hava.data.remote.dto.City
import com.example.hava.ui.theme.HavaTheme
import kotlin.math.roundToInt

// Gerçek plakalardaki AB şeridi ve siyah çerçeve
private val PlateBlue = Color(0xFF1F4FB8)
private val PlateInk = Color(0xFF1B1B1F)
private val LeadingWidth = 54.dp

@Composable
fun CityCard(
    city: City,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    highlightQuery: String = "",
    conditions: CurrentConditions? = null,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
) {
    // Hafif saydamlık arka planın kartların arasından hissedilmesini sağlar
    val colors = if (isSelected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = colors,
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Plakalı ve plakasız satırlarda isimler aynı hizadan başlasın
            Box(modifier = Modifier.width(LeadingWidth), contentAlignment = Alignment.Center) {
                val plate = TurkishPlates.plateFor(city)
                val countryCode = city.countryCode
                if (plate != null) {
                    PlateBadge(plate)
                } else if (countryCode != null && countryCode.length == 2) {
                    Text(
                        text = flagEmoji(countryCode),
                        fontSize = 26.sp,
                        modifier = Modifier.clearAndSetSemantics {},
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = highlightMatch(city.name, highlightQuery),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = city.region?.takeIf { it != city.name }
                    ?: conditions?.let { stringResource(weatherDescriptionRes(it.weatherCode)) }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalContentColor.current.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (conditions != null) {
                Spacer(Modifier.width(8.dp))
                CityConditions(conditions)
            }
            if (onFavoriteClick != null) {
                val description = stringResource(
                    if (isFavorite) R.string.guard_favorite_remove else R.string.guard_favorite_add,
                )
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.semantics { contentDescription = description },
                ) {
                    Text(
                        text = if (isFavorite) "★" else "☆",
                        color = if (isFavorite) Color(0xFFFFE082) else LocalContentColor.current,
                    )
                }
            }
        }
    }
}

@Composable
fun PlateBadge(plate: String, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.plate_code, plate)
    Row(
        modifier = modifier
            .height(28.dp)
            .border(1.5.dp, PlateInk, RoundedCornerShape(5.dp))
            .background(Color.White, RoundedCornerShape(5.dp))
            .padding(1.5.dp)
            .semantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(11.dp)
                .fillMaxHeight()
                .background(PlateBlue, RoundedCornerShape(topStart = 3.5.dp, bottomStart = 3.5.dp)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Text(
                text = "TR",
                color = Color.White,
                fontSize = 6.sp,
                lineHeight = 6.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 2.dp),
            )
        }
        Text(
            text = plate,
            color = PlateInk,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
    }
}

@Composable
private fun CityConditions(conditions: CurrentConditions) {
    Row(
        modifier = Modifier.widthIn(min = 64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
    ) {
        WeatherIcon(
            condition = conditions.condition,
            contentDescription = stringResource(weatherDescriptionRes(conditions.weatherCode)),
            isDay = conditions.isDay,
            size = 20.sp,
        )
        Text(
            text = stringResource(R.string.temperature_value, conditions.temperature.roundToInt()),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

private fun highlightMatch(text: String, query: String): AnnotatedString {
    val start = if (query.isBlank()) -1 else text.indexOf(query.trim(), ignoreCase = true)
    if (start < 0) return AnnotatedString(text)
    val end = start + query.trim().length
    return buildAnnotatedString {
        append(text.substring(0, start))
        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
            append(text.substring(start, end))
        }
        append(text.substring(end))
    }
}

@Preview(showBackground = true)
@Composable
private fun CityCardPreview() {
    HavaTheme {
        val city = PreviewData.provinces.first()
        CityCard(
            city = city,
            onClick = {},
            modifier = Modifier.padding(16.dp),
            conditions = PreviewData.conditions[city.id],
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CityCardSelectedPreview() {
    HavaTheme {
        val city = PreviewData.provinces[1]
        CityCard(
            city = city,
            onClick = {},
            modifier = Modifier.padding(16.dp),
            isSelected = true,
            highlightQuery = city.name.take(3),
        )
    }
}
