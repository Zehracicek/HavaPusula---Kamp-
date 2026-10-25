package com.example.hava.ui.screens.planb

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hava.R
import com.example.hava.data.model.WeatherReport
import com.example.hava.planb.ActivityEvaluation
import com.example.hava.planb.ActivityType
import com.example.hava.planb.FactorKind
import com.example.hava.planb.HourAssessment
import com.example.hava.planb.IndoorSuggestion
import com.example.hava.planb.InsufficientReason
import com.example.hava.planb.Measurement
import com.example.hava.planb.MeasurementUnit
import com.example.hava.planb.OverallAssessment
import com.example.hava.planb.ReasonCode
import com.example.hava.planb.SuggestedWindow
import com.example.hava.planb.SuitabilityLevel
import com.example.hava.ui.components.ErrorView
import com.example.hava.ui.components.LoadingView
import com.example.hava.ui.components.WeatherCard
import com.example.hava.ui.components.fullDayTitle
import com.example.hava.ui.components.hourLabel
import com.example.hava.ui.components.oneDecimal
import com.example.hava.ui.components.shortDayName
import com.example.hava.ui.screens.city.ErrorType
import com.example.hava.ui.screens.city.messageRes
import kotlin.math.roundToInt

@Composable
fun PlanBRoute(
    cityId: Long,
    cityName: String,
    report: WeatherReport?,
    isLoading: Boolean,
    errorType: ErrorType?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onChangeCity: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: PlanBViewModel = viewModel(factory = PlanBViewModel.Factory),
) {
    if (report != null) {
        LaunchedEffect(cityId, report.observedAt, report.latitude, report.longitude) {
            viewModel.onReport(cityId, report)
        }
    }
    PlanBScreen(
        cityName = cityName,
        report = report,
        isLoading = isLoading,
        errorType = errorType,
        activity = viewModel.activity,
        selectedDate = viewModel.selectedDate,
        startHour = viewModel.startHour,
        endHour = viewModel.endHour,
        evaluation = viewModel.evaluation,
        onActivitySelected = viewModel::onActivitySelected,
        onDateSelected = viewModel::onDateSelected,
        onStartHour = viewModel::onStartHour,
        onEndHour = viewModel::onEndHour,
        onEvaluate = { report?.let(viewModel::evaluate) },
        onRetry = onRetry,
        onBack = onBack,
        onChangeCity = onChangeCity,
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

@Composable
fun PlanBScreen(
    cityName: String,
    report: WeatherReport?,
    isLoading: Boolean,
    errorType: ErrorType?,
    activity: ActivityType,
    selectedDate: String,
    startHour: Int,
    endHour: Int,
    evaluation: ActivityEvaluation?,
    onActivitySelected: (ActivityType) -> Unit,
    onDateSelected: (String) -> Unit,
    onStartHour: (Int) -> Unit,
    onEndHour: (Int) -> Unit,
    onEvaluate: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onChangeCity: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    CompositionLocalProvider(LocalContentColor provides com.example.hava.ui.theme.Ink) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.planb_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(text = cityName, style = MaterialTheme.typography.bodyMedium)
                }
                TextButton(onClick = onChangeCity) {
                    Text(stringResource(R.string.planb_change_city))
                }
            }

            when {
                errorType != null && report == null -> ErrorView(
                    message = stringResource(errorType.messageRes()),
                    onRetry = onRetry,
                )
                report == null || isLoading -> LoadingView()
                else -> PlanBForm(
                    report = report,
                    activity = activity,
                    selectedDate = selectedDate,
                    startHour = startHour,
                    endHour = endHour,
                    evaluation = evaluation,
                    onActivitySelected = onActivitySelected,
                    onDateSelected = onDateSelected,
                    onStartHour = onStartHour,
                    onEndHour = onEndHour,
                    onEvaluate = onEvaluate,
                )
            }
        }
    }
}

@Composable
private fun PlanBForm(
    report: WeatherReport,
    activity: ActivityType,
    selectedDate: String,
    startHour: Int,
    endHour: Int,
    evaluation: ActivityEvaluation?,
    onActivitySelected: (ActivityType) -> Unit,
    onDateSelected: (String) -> Unit,
    onStartHour: (Int) -> Unit,
    onEndHour: (Int) -> Unit,
    onEvaluate: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "activity") {
            SectionLabel(stringResource(R.string.planb_activity))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ActivityType.entries, key = { it.name }) { item ->
                    ActivityTile(
                        label = stringResource(item.labelRes()),
                        emoji = item.emoji(),
                        selected = item == activity,
                        onClick = { onActivitySelected(item) },
                    )
                }
            }
        }
        item(key = "date") {
            SectionLabel(stringResource(R.string.planb_date))
            if (report.daily.isEmpty()) {
                Text(stringResource(R.string.planb_no_date))
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(report.daily, key = { it.date }) { day ->
                        ChoiceChip(
                            label = shortDayName(day.date),
                            selected = day.date == selectedDate,
                            onClick = { onDateSelected(day.date) },
                        )
                    }
                }
            }
        }
        item(key = "hours") {
            HourSlider(
                label = stringResource(R.string.planb_start_hour, clockLabel(startHour)),
                hour = startHour,
                onHour = onStartHour,
            )
            HourSlider(
                label = stringResource(R.string.planb_end_hour, clockLabel(endHour)),
                hour = endHour,
                onHour = onEndHour,
            )
        }
        item(key = "evaluate") {
            Button(
                onClick = onEvaluate,
                enabled = selectedDate.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.planb_evaluate))
            }
            Text(
                text = stringResource(R.string.planb_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = LocalContentColor.current.copy(alpha = 0.75f),
            )
        }
        if (evaluation != null) {
            item(key = "result") { EvaluationCard(evaluation) }
        }
    }
}

@Composable
private fun EvaluationCard(evaluation: ActivityEvaluation) {
    val activityName = stringResource(evaluation.activity.labelRes())
    WeatherCard(title = "$activityName · ${fullDayTitle(evaluation.date)}") {
        when (val overall = evaluation.window.overall) {
            is OverallAssessment.Insufficient -> {
                Text(
                    when (overall.reason) {
                        InsufficientReason.NO_HOURLY_DATA -> stringResource(R.string.planb_no_hourly)
                        InsufficientReason.MISSING_CRITICAL -> stringResource(
                            R.string.planb_missing_critical,
                            joinedLabels(overall.missing),
                        )
                    },
                )
            }
            is OverallAssessment.Scored -> ScoredSummary(activityName, evaluation, overall)
        }
        if (evaluation.requestedHourCount > evaluation.window.hours.size && evaluation.window.hours.isNotEmpty()) {
            Text(
                stringResource(
                    R.string.planb_partial_hours,
                    evaluation.requestedHourCount,
                    evaluation.window.hours.size,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        UnavailableNotes(evaluation)
        if (evaluation.measurements.isNotEmpty()) {
            Text(stringResource(R.string.planb_measurements), fontWeight = FontWeight.SemiBold)
            for (measurement in evaluation.measurements) {
                Text(stringResource(R.string.planb_measurement, stringResource(measurement.kind.labelRes()), measurement.valueText()))
            }
        }
        if (evaluation.window.hours.isNotEmpty()) {
            Text(stringResource(R.string.planb_compare), fontWeight = FontWeight.SemiBold)
            for (hour in evaluation.window.hours) {
                HourLine(hour)
            }
        }
        Suggestions(evaluation)
    }
}

@Composable
private fun ScoredSummary(
    activityName: String,
    evaluation: ActivityEvaluation,
    overall: OverallAssessment.Scored,
) {
    val levelName = stringResource(overall.level.labelRes())
    val description = stringResource(R.string.planb_score_cd, activityName, overall.score, levelName)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.semantics { contentDescription = description },
    ) {
        ScoreRing(score = overall.score, level = overall.level)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = levelName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = activityName, style = MaterialTheme.typography.bodyMedium)
        }
    }
    evaluation.bestWindow?.let { window -> Text(window.summary()) }
    Text(overall.explanation())
    if (overall.positive.isNotEmpty()) {
        Text(stringResource(R.string.planb_positive) + ": " + joinedLabels(overall.positive))
    }
    if (overall.negative.isNotEmpty()) {
        Text(stringResource(R.string.planb_negative) + ": " + joinedLabels(overall.negative))
    }
    if (overall.missingOptional.isNotEmpty()) {
        Text(
            stringResource(R.string.planb_data_partial, joinedLabels(overall.missingOptional)),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun Suggestions(evaluation: ActivityEvaluation) {
    val suggestions = evaluation.suggestions
    if (suggestions.isEmpty) return
    val reason = stringResource((suggestions.reason ?: ReasonCode.WEATHER).labelRes())
    Text(stringResource(R.string.planb_suggestion_title), fontWeight = FontWeight.SemiBold)
    suggestions.betterWindow?.let { window ->
        Text(
            stringResource(
                R.string.planb_better_window,
                hourLabel(window.startTime),
                hourLabel(window.endTime),
                window.score,
                stringResource(window.level.labelRes()),
            ),
        )
    }
    suggestions.alternativeActivity?.let { alternative ->
        Text(
            stringResource(
                R.string.planb_alternative,
                stringResource(alternative.activity.labelRes()),
                alternative.score,
                stringResource(alternative.level.labelRes()),
            ),
        )
    }
    suggestions.indoor?.let { indoor ->
        Text(stringResource(R.string.planb_indoor_line, reason, stringResource(indoor.labelRes())))
    }
}

@Composable
private fun UnavailableNotes(evaluation: ActivityEvaluation) {
    if (FactorKind.AIR_QUALITY in evaluation.window.unavailable) {
        Text(stringResource(R.string.planb_aqi_missing), style = MaterialTheme.typography.bodySmall)
    }
    if (FactorKind.WAVE_HEIGHT in evaluation.window.unavailable) {
        Text(stringResource(R.string.planb_wave_missing), style = MaterialTheme.typography.bodySmall)
    }
    val missingDirection = (evaluation.window.overall as? OverallAssessment.Scored)
        ?.missingOptional
        ?.contains(FactorKind.WIND_DIRECTION) == true
    if (missingDirection) {
        Text(stringResource(R.string.planb_direction_missing), style = MaterialTheme.typography.bodySmall)
    } else if (evaluation.windDirectionKnown) {
        Text(stringResource(R.string.planb_direction_unused), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun HourLine(hour: HourAssessment) {
    when (hour) {
        is HourAssessment.Insufficient -> Text(stringResource(R.string.planb_hour_missing, hourLabel(hour.time)))
        is HourAssessment.Scored -> {
            val temp = hour.snapshot.temperatureC?.let { stringResource(R.string.planb_temp_value, oneDecimal(it)) }
                ?: stringResource(R.string.planb_no_value_short)
            val rain = hour.snapshot.precipitationProbabilityPercent?.let { stringResource(R.string.percent_value, it) }
                ?: stringResource(R.string.planb_no_value_short)
            val wind = hour.snapshot.windSpeedKmh?.let { stringResource(R.string.planb_wind_value, it.roundToInt()) }
                ?: stringResource(R.string.planb_no_value_short)
            val score = "${hour.score} · ${stringResource(hour.level.labelRes())}"
            Text(stringResource(R.string.planb_hour_line, hourLabel(hour.time), temp, rain, wind, score))
        }
    }
}

@Composable
private fun SuggestedWindow.summary(): String {
    val start = hourLabel(startTime)
    val end = hourLabel(endTime)
    return if (fullySuitable && start != end) {
        stringResource(R.string.planb_best_range, start, end)
    } else if (fullySuitable) {
        stringResource(R.string.planb_best_hour, start)
    } else {
        stringResource(R.string.planb_best_available, start, stringResource(level.labelRes()))
    }
}

@Composable
private fun OverallAssessment.Scored.explanation(): String = when {
    safetyOverride -> stringResource(R.string.planb_explain_severe)
    level == SuitabilityLevel.SUITABLE -> stringResource(R.string.planb_explain_suitable)
    level == SuitabilityLevel.CAUTION -> stringResource(R.string.planb_explain_caution, stringResource((dominantReason ?: ReasonCode.WEATHER).labelRes()))
    else -> stringResource(R.string.planb_explain_unsuitable, stringResource((dominantReason ?: ReasonCode.WEATHER).labelRes()))
}

@Composable
private fun ScoreRing(score: Int, level: SuitabilityLevel) {
    val color = when (level) {
        SuitabilityLevel.SUITABLE -> Color(0xFF1F9D62)
        SuitabilityLevel.CAUTION -> Color(0xFFE09A12)
        SuitabilityLevel.UNSUITABLE -> Color(0xFFD64550)
    }
    Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 8.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
            drawArc(color.copy(alpha = 0.18f), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke)
            drawArc(color, startAngle = -90f, sweepAngle = 360f * score / 100f, useCenter = false, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$score", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text("/100", style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

@Composable
private fun ActivityTile(
    label: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) com.example.hava.ui.theme.Brand else Color.White
    val content = if (selected) Color.White else com.example.hava.ui.theme.Ink
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = background,
        contentColor = content,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) background else com.example.hava.ui.theme.CardBorder),
        modifier = Modifier.width(108.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = LocalContentColor.current.copy(alpha = 0.8f),
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = com.example.hava.ui.theme.Brand,
            selectedLabelColor = Color.White,
        ),
    )
}

@Composable
private fun HourSlider(label: String, hour: Int, onHour: (Int) -> Unit) {
    Text(label, style = MaterialTheme.typography.titleSmall)
    Slider(
        value = hour.toFloat(),
        onValueChange = { onHour(it.roundToInt()) },
        valueRange = 0f..23f,
        steps = 22,
        colors = SliderDefaults.colors(),
    )
}

private fun Measurement.valueText(): String {
    fun number(value: Double): String = when (unit) {
        MeasurementUnit.PERCENT, MeasurementUnit.KILOMETERS_PER_HOUR -> value.roundToInt().toString()
        else -> oneDecimal(value)
    }
    val span = if (min == max) number(min) else "${number(min)}–${number(max)}"
    val suffix = when (unit) {
        MeasurementUnit.CELSIUS -> " °C"
        MeasurementUnit.PERCENT -> "%"
        MeasurementUnit.MILLIMETER -> " mm"
        MeasurementUnit.KILOMETERS_PER_HOUR -> " km/sa"
        MeasurementUnit.UV_INDEX -> ""
        MeasurementUnit.NONE -> ""
    }
    return span + suffix
}

private fun clockLabel(hour: Int): String = "%02d:00".format(hour)

@Composable
private fun joinedLabels(kinds: List<FactorKind>): String {
    val labels = ArrayList<String>(kinds.size)
    for (kind in kinds) labels += stringResource(kind.labelRes())
    return labels.joinToString()
}

private fun ActivityType.emoji(): String = when (this) {
    ActivityType.PICNIC -> "🧺"
    ActivityType.WALK -> "🚶"
    ActivityType.RUN -> "🏃"
    ActivityType.BIKE -> "🚲"
    ActivityType.BEACH -> "🏖"
}

private fun ActivityType.labelRes(): Int = when (this) {
    ActivityType.PICNIC -> R.string.planb_picnic
    ActivityType.WALK -> R.string.planb_walk
    ActivityType.RUN -> R.string.planb_run
    ActivityType.BIKE -> R.string.planb_bike
    ActivityType.BEACH -> R.string.planb_beach
}

private fun SuitabilityLevel.labelRes(): Int = when (this) {
    SuitabilityLevel.SUITABLE -> R.string.planb_level_suitable
    SuitabilityLevel.CAUTION -> R.string.planb_level_caution
    SuitabilityLevel.UNSUITABLE -> R.string.planb_level_unsuitable
}

private fun FactorKind.labelRes(): Int = when (this) {
    FactorKind.PRECIPITATION_PROBABILITY -> R.string.planb_factor_precip_prob
    FactorKind.PRECIPITATION_AMOUNT -> R.string.planb_factor_precip_amount
    FactorKind.TEMPERATURE -> R.string.planb_factor_temperature
    FactorKind.WIND_SPEED -> R.string.planb_factor_wind
    FactorKind.WIND_GUSTS -> R.string.planb_factor_gust
    FactorKind.WIND_DIRECTION -> R.string.planb_factor_direction
    FactorKind.UV_INDEX -> R.string.planb_factor_uv
    FactorKind.WEATHER_CODE -> R.string.planb_factor_code
    FactorKind.AIR_QUALITY -> R.string.planb_factor_aqi
    FactorKind.WAVE_HEIGHT -> R.string.planb_factor_wave
}

private fun ReasonCode.labelRes(): Int = when (this) {
    ReasonCode.PRECIPITATION -> R.string.planb_reason_precipitation
    ReasonCode.HEAT -> R.string.planb_reason_heat
    ReasonCode.COLD -> R.string.planb_reason_cold
    ReasonCode.WIND -> R.string.planb_reason_wind
    ReasonCode.UV -> R.string.planb_reason_uv
    ReasonCode.SEVERE_WEATHER -> R.string.planb_reason_severe
    ReasonCode.WEATHER -> R.string.planb_reason_weather
    ReasonCode.COMFORTABLE -> R.string.planb_reason_comfortable
}

private fun IndoorSuggestion.labelRes(): Int = when (this) {
    IndoorSuggestion.CAFE -> R.string.planb_indoor_cafe
    IndoorSuggestion.INDOOR_WALK -> R.string.planb_indoor_walk
    IndoorSuggestion.GYM_RUN -> R.string.planb_indoor_run
    IndoorSuggestion.INDOOR_BIKE -> R.string.planb_indoor_bike
    IndoorSuggestion.INDOOR_POOL -> R.string.planb_indoor_pool
}
