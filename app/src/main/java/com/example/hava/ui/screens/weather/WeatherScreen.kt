package com.example.hava.ui.screens.weather

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hava.R
import com.example.hava.data.model.DailyForecast
import com.example.hava.data.model.DayPhase
import com.example.hava.data.model.HourlyForecast
import com.example.hava.data.model.WeatherCondition
import com.example.hava.data.model.WeatherReport
import com.example.hava.ui.background.WeatherBackground
import com.example.hava.ui.background.WeatherScene
import com.example.hava.ui.components.DayHourlySheet
import com.example.hava.ui.components.Landscapes
import com.example.hava.ui.components.LandscapeScene
import com.example.hava.ui.components.MoonCard
import com.example.hava.ui.components.PrecipitationMapCard
import com.example.hava.ui.components.PreviewData
import com.example.hava.ui.components.SectionTitle
import com.example.hava.ui.components.WeatherDetailsCard
import com.example.hava.ui.components.WeatherGlyph
import com.example.hava.ui.components.WeatherIcon
import com.example.hava.ui.components.WindCard
import com.example.hava.ui.components.dayMonthWeekday
import com.example.hava.ui.components.hourLabel
import com.example.hava.ui.components.oneDecimal
import com.example.hava.ui.components.percentTr
import com.example.hava.ui.components.uvLevelIndex
import com.example.hava.ui.components.weatherDescriptionRes
import com.example.hava.ui.components.weekdayName
import com.example.hava.ui.screens.city.ErrorType
import com.example.hava.ui.screens.city.messageRes
import com.example.hava.ui.theme.Brand
import com.example.hava.ui.theme.BrandSoft
import com.example.hava.ui.theme.CardBorder
import com.example.hava.ui.theme.HavaTheme
import com.example.hava.ui.theme.Ink
import com.example.hava.ui.theme.Muted
import com.example.hava.ui.theme.PageBackground
import com.example.hava.ui.theme.Success
import com.example.hava.ui.theme.SuccessSoft
import kotlin.math.abs
import kotlin.math.roundToInt

private const val FORECAST_DAYS = 5
private val HeroShape = RoundedCornerShape(24.dp)
private val HeroTextShadow = Shadow(Color.Black.copy(alpha = 0.3f), Offset(0f, 2f), 8f)

@Composable
fun WeatherScreen(
    state: WeatherUiState,
    scene: WeatherScene,
    onRetry: () -> Unit,
    onMyLocationClick: () -> Unit,
    onCitiesClick: () -> Unit,
    onPlanBClick: () -> Unit,
    onGuardClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
) {
    var selectedDate by rememberSaveable { mutableStateOf<String?>(null) }

    CompositionLocalProvider(LocalContentColor provides Ink) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            HomeHeader(
                onSearchClick = onCitiesClick,
                onMyLocationClick = onMyLocationClick,
                onCitiesClick = onCitiesClick,
                onSettingsClick = onSettingsClick,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item(key = "hero") {
                    HeroCard(state = state, scene = scene, onRetry = onRetry, onLocationClick = onMyLocationClick)
                }
                if (state is WeatherUiState.Success) {
                    val report = state.report
                    item(key = "today") { TodaySection(report.hourly) }
                    val days = report.daily.drop(1).take(FORECAST_DAYS)
                    if (days.isNotEmpty()) {
                        item(key = "days") { DaysSection(days, onDayClick = { selectedDate = it.date }) }
                    }
                }
                item(key = "shortcuts") {
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ShortcutCard(
                            title = stringResource(R.string.planb_open),
                            body = stringResource(R.string.planb_shortcut),
                            icon = Icons.Filled.EventAvailable,
                            accent = Success,
                            background = SuccessSoft,
                            onClick = onPlanBClick,
                            modifier = Modifier.weight(1f),
                        )
                        ShortcutCard(
                            title = stringResource(R.string.guard_open),
                            body = stringResource(R.string.guard_shortcut),
                            icon = Icons.Filled.Shield,
                            accent = Brand,
                            background = BrandSoft,
                            onClick = onGuardClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (state is WeatherUiState.Success) {
                    val report = state.report
                    val today = report.daily.firstOrNull()
                    item(key = "details-title") { SectionTitle(stringResource(R.string.home_details)) }
                    item(key = "details") { WeatherDetailsCard(weather = report.current, today = today) }
                    item(key = "precip-map") {
                        PrecipitationMapCard(
                            latitude = report.latitude,
                            longitude = report.longitude,
                            temperature = report.current.temperature,
                        )
                    }
                    item(key = "wind") { WindCard(weather = report.current, today = today) }
                    val moon = report.moon
                    if (moon != null) {
                        item(key = "moon") { MoonCard(moon) }
                    }
                }
            }
        }

        val report = (state as? WeatherUiState.Success)?.report
        val selectedDay = report?.daily?.firstOrNull { it.date == selectedDate }
        if (selectedDay != null) {
            DayHourlySheet(
                day = selectedDay,
                hourly = report.hourlyByDate[selectedDay.date].orEmpty(),
                isToday = selectedDay == report.daily.first(),
                onDismiss = { selectedDate = null },
            )
        }
    }
}

@Composable
private fun HomeHeader(
    onSearchClick: () -> Unit,
    onMyLocationClick: () -> Unit,
    onCitiesClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WeatherGlyph(condition = WeatherCondition.PARTLY_CLOUDY, modifier = Modifier.size(38.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge, color = Ink)
            Text(
                stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onSearchClick) {
            Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search), tint = Ink)
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more_options), tint = Ink)
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                containerColor = Color.White,
            ) {
                MenuItem(stringResource(R.string.use_my_location), Icons.Filled.MyLocation) {
                    menuOpen = false
                    onMyLocationClick()
                }
                MenuItem(stringResource(R.string.cities), Icons.AutoMirrored.Filled.List) {
                    menuOpen = false
                    onCitiesClick()
                }
                MenuItem(stringResource(R.string.settings_title), Icons.Filled.Settings) {
                    menuOpen = false
                    onSettingsClick()
                }
            }
        }
    }
}

@Composable
private fun MenuItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(text, color = Ink) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Brand) },
        onClick = onClick,
    )
}

@Composable
private fun HeroCard(
    state: WeatherUiState,
    scene: WeatherScene,
    onRetry: () -> Unit,
    onLocationClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HeroShape)
            .heightIn(min = 236.dp),
    ) {
        WeatherBackground(scene = scene, modifier = Modifier.matchParentSize())
        LandscapeScene(Landscapes.Hero, Modifier.matchParentSize())
        Box(
            Modifier
                .matchParentSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.32f)))),
        )
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            when (state) {
                is WeatherUiState.Success -> HeroContent(state.report, onLocationClick)
                is WeatherUiState.Loading -> HeroMessage(state.cityName ?: stringResource(R.string.locating)) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(30.dp))
                }
                is WeatherUiState.Error -> HeroMessage(state.cityName ?: stringResource(R.string.my_location)) {
                    Text(
                        text = stringResource(R.string.weather_load_failed),
                        style = MaterialTheme.typography.bodyLarge.shadowed(),
                    )
                    Text(
                        text = stringResource(state.type.messageRes()),
                        style = MaterialTheme.typography.bodySmall.shadowed(),
                        textAlign = TextAlign.Center,
                    )
                    TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.retry), color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroMessage(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 236.dp)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge.shadowed())
        content()
    }
}

@Composable
private fun HeroContent(report: WeatherReport, onLocationClick: () -> Unit) {
    val current = report.current
    val today = report.daily.firstOrNull()
    val date = today?.date ?: report.observedAt?.take(10)
    Column(modifier = Modifier.padding(start = 18.dp, end = 14.dp, top = 16.dp, bottom = 14.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onLocationClick),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = current.cityName,
                        style = MaterialTheme.typography.titleLarge.shadowed(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = stringResource(R.string.use_my_location),
                        modifier = Modifier.size(18.dp),
                    )
                }
                if (date != null) {
                    Text(
                        text = dayMonthWeekday(date),
                        style = MaterialTheme.typography.bodySmall.shadowed(),
                        color = Color.White.copy(alpha = 0.92f),
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.temperature_value, current.temperature.roundToInt()),
                        style = TextStyle(fontSize = 64.sp, fontWeight = FontWeight.SemiBold, shadow = HeroTextShadow),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(weatherDescriptionRes(current.weatherCode)),
                            style = MaterialTheme.typography.titleSmall.shadowed(),
                        )
                        if (today != null) {
                            Text(
                                text = stringResource(
                                    R.string.home_high_low,
                                    today.maxTemperature.roundToInt(),
                                    today.minTemperature.roundToInt(),
                                ),
                                style = MaterialTheme.typography.bodySmall.shadowed(),
                            )
                        }
                    }
                }
            }
            WeatherIcon(
                condition = current.condition,
                contentDescription = stringResource(weatherDescriptionRes(current.weatherCode)),
                isDay = current.dayPhase != DayPhase.NIGHT,
                size = 64.sp,
            )
        }
        Spacer(Modifier.height(12.dp))
        HeroStats(report)
    }
}

@Composable
private fun HeroStats(report: WeatherReport) {
    val current = report.current
    val precipitation = report.daily.firstOrNull()?.precipitationSum ?: current.precipitation ?: 0.0
    val uv = current.uvIndex
    val uvLevels = stringArrayResource(R.array.uv_levels)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeroStat(percentTr(current.humidity), stringResource(R.string.humidity))
        StatDivider()
        HeroStat(stringResource(R.string.wind_kmh, current.windSpeed.roundToInt()), stringResource(R.string.wind))
        StatDivider()
        HeroStat(stringResource(R.string.precip_mm, compactNumber(precipitation)), stringResource(R.string.precipitation))
        StatDivider()
        HeroStat(
            value = uv?.let { "UV ${it.roundToInt()}" } ?: stringResource(R.string.no_value),
            label = uv?.let { uvLevels[uvLevelIndex(it)] } ?: "UV",
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HeroStat(value: String, label: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.labelLarge, color = Color.White, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
    }
}

@Composable
private fun StatDivider() {
    Box(
        Modifier
            .width(1.dp)
            .height(28.dp)
            .background(Color.White.copy(alpha = 0.3f)),
    )
}

@Composable
private fun TodaySection(hourly: List<HourlyForecast>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(stringResource(R.string.home_today))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(hourly, key = { _, item -> item.time }) { index, item ->
                ForecastTile(
                    label = if (index == 0) stringResource(R.string.now) else hourLabel(item.time),
                    condition = item.condition,
                    isDay = item.isDay,
                    description = stringResource(weatherDescriptionRes(item.weatherCode)),
                    width = 62,
                ) {
                    Text(
                        stringResource(R.string.temperature_value, item.temperature.roundToInt()),
                        style = MaterialTheme.typography.labelLarge,
                        color = Ink,
                    )
                }
            }
        }
    }
}

@Composable
private fun DaysSection(days: List<DailyForecast>, onDayClick: (DailyForecast) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(stringResource(R.string.home_daily_title, days.size))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(days, key = { it.date }) { day ->
                ForecastTile(
                    label = weekdayName(day.date),
                    condition = day.condition,
                    isDay = true,
                    description = stringResource(weatherDescriptionRes(day.weatherCode)),
                    width = 76,
                    onClick = { onDayClick(day) },
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            stringResource(R.string.temperature_value, day.maxTemperature.roundToInt()),
                            style = MaterialTheme.typography.labelLarge,
                            color = Ink,
                        )
                        Text(
                            "/" + stringResource(R.string.temperature_value, day.minTemperature.roundToInt()),
                            style = MaterialTheme.typography.labelMedium,
                            color = Muted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastTile(
    label: String,
    condition: WeatherCondition,
    isDay: Boolean,
    description: String,
    width: Int,
    onClick: (() -> Unit)? = null,
    value: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(width.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            WeatherIcon(condition = condition, contentDescription = description, isDay = isDay, size = 22.sp)
            value()
        }
    }
}

@Composable
private fun ShortcutCard(
    title: String,
    body: String,
    icon: ImageVector,
    accent: Color,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(20.dp),
        color = background,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, color = accent, maxLines = 1)
            }
            Text(body, style = MaterialTheme.typography.bodySmall, color = Ink.copy(alpha = 0.8f))
        }
    }
}

private fun TextStyle.shadowed() = copy(shadow = HeroTextShadow)

private fun compactNumber(value: Double): String =
    if (abs(value - value.roundToInt()) < 0.05) value.roundToInt().toString() else oneDecimal(value)

@Composable
private fun WeatherScreenPreviewContainer(content: @Composable () -> Unit) {
    HavaTheme {
        Box(Modifier.background(PageBackground)) {
            content()
        }
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun WeatherScreenSuccessPreview() {
    WeatherScreenPreviewContainer {
        WeatherScreen(
            state = WeatherUiState.Success(PreviewData.report),
            scene = WeatherScene(WeatherCondition.PARTLY_CLOUDY, DayPhase.DAY),
            onRetry = {},
            onMyLocationClick = {},
            onCitiesClick = {},
            onPlanBClick = {},
            onGuardClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeatherScreenErrorPreview() {
    WeatherScreenPreviewContainer {
        WeatherScreen(
            state = WeatherUiState.Error("Ankara", ErrorType.NETWORK),
            scene = WeatherScene(WeatherCondition.RAIN, DayPhase.NIGHT, intensity = 0.7f),
            onRetry = {},
            onMyLocationClick = {},
            onCitiesClick = {},
            onPlanBClick = {},
            onGuardClick = {},
        )
    }
}
