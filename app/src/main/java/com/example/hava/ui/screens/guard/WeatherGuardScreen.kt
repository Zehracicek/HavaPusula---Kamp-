package com.example.hava.ui.screens.guard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hava.R
import com.example.hava.data.mapper.weatherConditionOf
import com.example.hava.data.model.WeatherCondition
import com.example.hava.data.model.WeatherReport
import com.example.hava.data.remote.dto.City
import com.example.hava.guard.GuardReport
import com.example.hava.guard.GuardRisk
import com.example.hava.guard.RiskFinding
import com.example.hava.guard.RiskSettings
import com.example.hava.guard.window
import com.example.hava.planb.HourConditions
import com.example.hava.ui.components.BulletLine
import com.example.hava.ui.components.ChevronIcon
import com.example.hava.ui.components.ErrorView
import com.example.hava.ui.components.HavaCard
import com.example.hava.ui.components.IconBadge
import com.example.hava.ui.components.LoadingView
import com.example.hava.ui.components.ScreenTopBar
import com.example.hava.ui.components.SectionTitle
import com.example.hava.ui.components.StatusChip
import com.example.hava.ui.components.WeatherGlyph
import com.example.hava.ui.components.longDate
import com.example.hava.ui.components.lowercaseTr
import com.example.hava.ui.screens.city.ErrorType
import com.example.hava.ui.screens.city.messageRes
import com.example.hava.ui.theme.Brand
import com.example.hava.ui.theme.BrandSoft
import com.example.hava.ui.theme.CardBorder
import com.example.hava.ui.theme.Ink
import com.example.hava.ui.theme.Muted
import kotlin.math.roundToInt

private const val COLLAPSED_RISKS = 3

@Composable
fun WeatherGuardRoute(
    city: City,
    report: WeatherReport?,
    isLoading: Boolean,
    errorType: ErrorType?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onChangeCity: () -> Unit,
    onOpenFavorite: (City) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: WeatherGuardViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(city.id, city.latitude, city.longitude, report?.observedAt) {
        viewModel.bind(city, report)
    }
    var selectedName by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = selectedName?.let { name ->
        state.report?.let { guard -> (guard.active + listOfNotNull(guard.thunderstorm)).firstOrNull { it.risk.name == name } }
    }
    BackHandler(enabled = selected != null) { selectedName = null }
    val cityName = report?.current?.cityName ?: city.name
    if (selected != null) {
        RiskDetailScreen(
            cityName = cityName,
            finding = selected,
            hours = state.hours,
            isFavorite = state.isFavorite,
            onBack = { selectedName = null },
            onToggleFavorite = viewModel::toggleFavorite,
            modifier = modifier,
            contentPadding = contentPadding,
        )
    } else {
        WeatherGuardScreen(
            city = city,
            cityName = cityName,
            state = state,
            isLoading = isLoading && report == null,
            errorType = if (report == null) errorType else null,
            onRetry = onRetry,
            onOpenSettings = onOpenSettings,
            onChangeCity = onChangeCity,
            onOpenFavorite = onOpenFavorite,
            onOpenRisk = { selectedName = it.risk.name },
            modifier = modifier,
            contentPadding = contentPadding,
        )
    }
}

@Composable
fun WeatherGuardScreen(
    city: City,
    cityName: String,
    state: GuardUiState,
    isLoading: Boolean,
    errorType: ErrorType?,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    onChangeCity: () -> Unit,
    onOpenFavorite: (City) -> Unit,
    onOpenRisk: (RiskFinding) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(modifier = modifier.fillMaxSize().padding(contentPadding)) {
        ScreenTopBar(
            title = stringResource(R.string.guard_title),
            subtitle = stringResource(R.string.guard_subtitle),
            leading = {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = Brand, modifier = Modifier.size(40.dp))
            },
            actions = {
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings_title), tint = Brand)
                }
            },
        )
        when {
            errorType != null -> ErrorView(message = stringResource(errorType.messageRes()), onRetry = onRetry)
            isLoading || state.report == null -> LoadingView()
            else -> GuardBody(
                city = city,
                cityName = cityName,
                settings = state.settings,
                report = state.report,
                hours = state.hours,
                favorites = state.favorites,
                onChangeCity = onChangeCity,
                onOpenFavorite = onOpenFavorite,
                onOpenRisk = onOpenRisk,
            )
        }
    }
}

@Composable
private fun GuardBody(
    city: City,
    cityName: String,
    settings: RiskSettings,
    report: GuardReport,
    hours: List<HourConditions>,
    favorites: List<FavoriteGuard>,
    onChangeCity: () -> Unit,
    onOpenFavorite: (City) -> Unit,
    onOpenRisk: (RiskFinding) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val risks = listOfNotNull(report.thunderstorm) + report.active
    val visible = if (expanded) risks else risks.take(COLLAPSED_RISKS)
    val hasMore = risks.size > COLLAPSED_RISKS || report.insufficient.isNotEmpty()
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "favorites-title") {
            SectionTitle(
                text = stringResource(R.string.guard_favorites),
                action = stringResource(R.string.guard_edit),
                onAction = onChangeCity,
            )
        }
        item(key = "chips") {
            CityChips(
                currentCity = city,
                currentName = cityName,
                favorites = favorites,
                onOpenFavorite = onOpenFavorite,
                onAdd = onChangeCity,
            )
        }
        item(key = "risks-title") {
            SectionTitle(
                text = stringResource(R.string.guard_active_risks),
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        if (risks.isEmpty()) {
            item(key = "summary") { Summary(settings, report) }
        }
        items(visible, key = { "risk-${it.risk.name}" }) { finding ->
            RiskCard(finding = finding, cityName = cityName, hours = hours, onClick = { onOpenRisk(finding) })
        }
        if (expanded) {
            items(report.insufficient, key = { "missing-${it.risk.name}" }) { finding ->
                InsufficientCard(finding)
            }
        }
        if (hasMore) {
            item(key = "toggle") {
                HavaCard(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(if (expanded) R.string.guard_see_less else R.string.guard_see_all),
                            style = MaterialTheme.typography.labelLarge,
                            color = Brand,
                            modifier = Modifier.weight(1f),
                        )
                        ChevronIcon()
                    }
                }
            }
        }
        item(key = "disclaimer") {
            Text(
                text = stringResource(R.string.guard_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }
        item(key = "favorites-grid-title") {
            SectionTitle(text = stringResource(R.string.guard_favorites), modifier = Modifier.padding(top = 6.dp))
        }
        item(key = "favorites-grid") {
            if (favorites.isEmpty()) {
                Text(stringResource(R.string.guard_favorites_empty), style = MaterialTheme.typography.bodyMedium, color = Muted)
            } else {
                FavoriteGrid(favorites, onOpenFavorite)
            }
        }
    }
}

@Composable
private fun CityChips(
    currentCity: City,
    currentName: String,
    favorites: List<FavoriteGuard>,
    onOpenFavorite: (City) -> Unit,
    onAdd: () -> Unit,
) {
    val others = favorites.filterNot { it.city.id == currentCity.id }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item(key = "current") { CityChip(text = currentName, selected = true, onClick = {}) }
        items(others, key = { it.city.id }) { favorite ->
            CityChip(text = favorite.city.name, selected = false, onClick = { onOpenFavorite(favorite.city) })
        }
        item(key = "add") {
            Surface(
                onClick = onAdd,
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.guard_add_city), tint = Brand)
                }
            }
        }
    }
}

@Composable
private fun CityChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) BrandSoft else Color.White,
        border = BorderStroke(1.dp, if (selected) BrandSoft else CardBorder),
        modifier = Modifier.height(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 22.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Brand else Ink,
            )
        }
    }
}

@Composable
private fun Summary(settings: RiskSettings, report: GuardReport) {
    val text = when {
        settings.enabled.isEmpty() -> stringResource(R.string.guard_none_selected)
        report.insufficient.isEmpty() -> stringResource(R.string.guard_no_exceedance)
        else -> stringResource(R.string.guard_partial_clear)
    }
    HavaCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBadge(Icons.Filled.Shield, tint = Brand, background = BrandSoft)
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RiskCard(finding: RiskFinding, cityName: String, hours: List<HourConditions>, onClick: () -> Unit) {
    val level = finding.level ?: return
    val style = finding.risk.style()
    val title = stringResource(finding.cardTitleRes())
    val levelName = stringResource(level.labelRes())
    val (chipText, chipBackground) = level.chipColors()
    val measurement = finding.primaryMeasurement()?.takeIf { finding.risk != GuardRisk.THUNDERSTORM }
    val range = finding.window(hours)?.label()
    HavaCard(
        onClick = onClick,
        containerColor = lerp(Color.White, style.soft, 0.7f),
        borderColor = lerp(Color.White, style.color, 0.25f),
        modifier = Modifier.semantics { contentDescription = "$title, $levelName" },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            IconBadge(style.icon, tint = Color.White, background = style.color, size = 56.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Ink)
                if (measurement != null) {
                    Text(
                        text = formatValue(measurement.value, measurement.unit),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = style.color,
                    )
                }
                if (range != null) {
                    Text(
                        text = stringResource(R.string.guard_time_city, range, cityName),
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            StatusChip(levelName, color = chipText, background = chipBackground, modifier = Modifier.align(Alignment.Top))
        }
        Text(stringResource(finding.risk.bodyRes()), style = MaterialTheme.typography.bodyMedium, color = Ink)
    }
}

@Composable
private fun InsufficientCard(finding: RiskFinding) {
    val style = finding.risk.style()
    HavaCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBadge(style.icon, tint = Muted, background = CardBorder)
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(finding.risk.labelRes()), style = MaterialTheme.typography.titleSmall, color = Ink)
                Text(
                    text = stringResource(R.string.guard_missing, joinedFactors(finding.missing)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            StatusChip(stringResource(R.string.guard_insufficient_title), color = Muted, background = CardBorder)
        }
    }
}

@Composable
private fun FavoriteGrid(favorites: List<FavoriteGuard>, onOpen: (City) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (row in favorites.chunked(3)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (favorite in row) {
                    FavoriteTile(favorite, onOpen, Modifier.weight(1f))
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun FavoriteTile(favorite: FavoriteGuard, onOpen: (City) -> Unit, modifier: Modifier = Modifier) {
    val hour = favorite.hours.firstOrNull()
    val risks = favorite.report?.let { listOfNotNull(it.thunderstorm) + it.active }.orEmpty()
    HavaCard(
        onClick = { onOpen(favorite.city) },
        modifier = modifier,
        contentPadding = PaddingValues(12.dp),
        spacing = 6.dp,
    ) {
        Text(
            text = favorite.city.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.height(16.dp)) {
            for (finding in risks.take(3)) {
                val style = finding.risk.style()
                Icon(
                    style.icon,
                    contentDescription = stringResource(finding.risk.labelRes()),
                    tint = style.color,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = when {
                    hour?.temperatureC != null -> "${hour.temperatureC.roundToInt()}°"
                    favorite.failed -> "–"
                    else -> "…"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier.weight(1f),
            )
            val condition = hour?.weatherCode?.let(::weatherConditionOf) ?: WeatherCondition.CLOUDY
            WeatherGlyph(condition, Modifier.size(28.dp))
        }
    }
}

@Composable
private fun RiskDetailScreen(
    cityName: String,
    finding: RiskFinding,
    hours: List<HourConditions>,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val style = finding.risk.style()
    val measurement = finding.primaryMeasurement()
    val range = finding.window(hours)?.label()
    Column(modifier = modifier.fillMaxSize().padding(contentPadding)) {
        ScreenTopBar(title = stringResource(R.string.guard_title), onBack = onBack)
        LazyColumn(
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "title") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(finding.risk.detailRes()),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                    )
                    val date = finding.validAt?.let { longDate(it.substringBefore('T')) }
                    Text(
                        text = if (date != null) stringResource(R.string.planb_city_date, cityName, date) else cityName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
            }
            item(key = "hero") {
                HavaCard(
                    containerColor = lerp(Color.White, style.soft, 0.8f),
                    borderColor = lerp(Color.White, style.color, 0.2f),
                    contentPadding = PaddingValues(18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        Box(
                            modifier = Modifier
                                .size(108.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            when (finding.risk) {
                                GuardRisk.PRECIPITATION -> WeatherGlyph(WeatherCondition.RAIN, Modifier.size(72.dp))
                                GuardRisk.THUNDERSTORM -> WeatherGlyph(WeatherCondition.STORM, Modifier.size(72.dp))
                                GuardRisk.UV, GuardRisk.HEAT -> WeatherGlyph(WeatherCondition.CLEAR, Modifier.size(72.dp))
                                else -> Icon(style.icon, contentDescription = null, tint = style.color, modifier = Modifier.size(56.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            finding.level?.let { level ->
                                val (chipText, chipBackground) = level.chipColors()
                                StatusChip(stringResource(level.labelRes()), color = chipText, background = chipBackground)
                            }
                            if (measurement != null) {
                                Text(
                                    text = formatValue(measurement.value, measurement.unit),
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink,
                                )
                                Text(finding.valueLabel(measurement), style = MaterialTheme.typography.bodyMedium, color = Muted)
                            }
                            if (range != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(range, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Ink)
                                Text(stringResource(R.string.guard_estimated_range), style = MaterialTheme.typography.labelSmall, color = Muted)
                            }
                        }
                    }
                }
            }
            item(key = "why") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SectionTitle(stringResource(R.string.guard_why))
                    WhyText(finding)
                    Text(stringResource(finding.reason.actionRes()), style = MaterialTheme.typography.bodyMedium, color = Muted)
                    Text(stringResource(R.string.guard_why_impact), style = MaterialTheme.typography.bodyMedium, color = Muted)
                }
            }
            item(key = "tips") {
                HavaCard(spacing = 10.dp) {
                    Text(stringResource(R.string.guard_what_to_do), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    for (tip in stringArrayResource(finding.risk.tipsRes())) {
                        BulletLine(tip, bulletColor = Brand)
                    }
                }
            }
            item(key = "source") {
                HavaCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.NotificationsNone, contentDescription = null, tint = Muted)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(stringResource(R.string.guard_source_title), style = MaterialTheme.typography.titleSmall)
                            val kinds = (finding.measurements.map { it.kind } + finding.thresholds.map { it.kind }).distinct()
                            Text(
                                text = stringResource(R.string.guard_source_body, joinedFactors(kinds)),
                                style = MaterialTheme.typography.bodySmall,
                                color = Muted,
                            )
                            Text(stringResource(R.string.guard_not_official), style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                    }
                }
            }
            item(key = "follow") {
                OutlinedButton(
                    onClick = onToggleFavorite,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, Brand),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Brand, containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                ) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(if (isFavorite) R.string.guard_unfollow else R.string.guard_follow),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun WhyText(finding: RiskFinding) {
    val measurement = finding.primaryMeasurement() ?: return
    val threshold = finding.thresholdFor(measurement.kind) ?: return
    val label = finding.valueLabel(measurement).lowercaseTr()
    val text = stringResource(
        if (finding.risk == GuardRisk.FROST) R.string.guard_why_below else R.string.guard_why_above,
        label,
        formatValue(measurement.value, measurement.unit),
        formatValue(threshold.value, threshold.unit),
    )
    Text(text, style = MaterialTheme.typography.bodyMedium, color = Ink)
}
