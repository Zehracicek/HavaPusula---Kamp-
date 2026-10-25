package com.example.hava.ui.screens.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hava.R
import com.example.hava.data.model.DayPhase
import com.example.hava.data.model.WeatherCondition
import com.example.hava.ui.background.WeatherBackground
import com.example.hava.ui.background.WeatherScene
import com.example.hava.ui.components.CurrentWeatherHeader
import com.example.hava.ui.components.DailyForecastCard
import com.example.hava.ui.components.DayHourlySheet
import com.example.hava.ui.components.HourlyForecastCard
import com.example.hava.ui.components.MoonCard
import com.example.hava.ui.components.MyLocationButton
import com.example.hava.ui.components.PrecipitationMapCard
import com.example.hava.ui.components.PreviewData
import com.example.hava.ui.components.WeatherCard
import com.example.hava.ui.components.WeatherDetailsCard
import com.example.hava.ui.components.WindCard
import com.example.hava.ui.screens.city.ErrorType
import com.example.hava.ui.theme.Brand
import com.example.hava.ui.theme.HavaTheme
import com.example.hava.ui.theme.Ink
import com.example.hava.ui.theme.Muted
import com.example.hava.ui.theme.PageBackground

private val TopBarButtonHeight = 44.dp

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
    contentPadding: PaddingValues = PaddingValues(),
) {
    var selectedDate by rememberSaveable { mutableStateOf<String?>(null) }

    CompositionLocalProvider(LocalContentColor provides Ink) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                    modifier = Modifier.weight(1f),
                )
                MyLocationButton(
                    onClick = onMyLocationClick,
                    size = TopBarButtonHeight,
                    containerColor = Color.White,
                    contentColor = Brand,
                )
                FilledTonalButton(
                    onClick = onCitiesClick,
                    modifier = Modifier.height(TopBarButtonHeight),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White,
                        contentColor = Ink,
                    ),
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.cities), style = MaterialTheme.typography.labelLarge, maxLines = 1)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item(key = "header") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp)),
                    ) {
                        WeatherBackground(scene = scene, modifier = Modifier.matchParentSize())
                        CompositionLocalProvider(LocalContentColor provides Color.White) {
                            CurrentWeatherHeader(state = state, onRetry = onRetry)
                        }
                    }
                }
                item(key = "shortcuts") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ShortcutCard(
                            title = stringResource(R.string.planb_open),
                            body = stringResource(R.string.planb_shortcut),
                            onClick = onPlanBClick,
                            modifier = Modifier.weight(1f),
                        )
                        ShortcutCard(
                            title = stringResource(R.string.guard_open),
                            body = stringResource(R.string.guard_shortcut),
                            onClick = onGuardClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (state is WeatherUiState.Success) {
                    val today = state.report.daily.firstOrNull()
                    item(key = "details") { WeatherDetailsCard(weather = state.report.current, today = today) }
                    item(key = "precip-map") {
                        PrecipitationMapCard(
                            latitude = state.report.latitude,
                            longitude = state.report.longitude,
                            temperature = state.report.current.temperature,
                        )
                    }
                    item(key = "wind") { WindCard(weather = state.report.current, today = today) }
                    val moon = state.report.moon
                    if (moon != null) {
                        item(key = "moon") { MoonCard(moon) }
                    }
                    item(key = "hourly") { HourlyForecastCard(hourly = state.report.hourly) }
                    item(key = "daily") {
                        DailyForecastCard(
                            daily = state.report.daily,
                            onDayClick = { selectedDate = it.date },
                        )
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
private fun ShortcutCard(
    title: String,
    body: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = if (title.contains("Plan", ignoreCase = true)) Color(0xFFE7F8F0) else Color(0xFFEEEDFF)
    WeatherCard(modifier = modifier.clickable(onClick = onClick), containerColor = tint) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Brand)
        Text(body, style = MaterialTheme.typography.bodySmall, color = Muted)
    }
}

@Composable
private fun WeatherScreenPreviewContainer(content: @Composable () -> Unit) {
    HavaTheme {
        Box(Modifier.background(PageBackground)) {
            content()
        }
    }
}

@Preview(showBackground = true, heightDp = 1600)
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
private fun WeatherScreenLoadingPreview() {
    WeatherScreenPreviewContainer {
        WeatherScreen(
            state = WeatherUiState.Loading(cityName = null),
            scene = WeatherScene(WeatherCondition.CLOUDY, DayPhase.DAY),
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
