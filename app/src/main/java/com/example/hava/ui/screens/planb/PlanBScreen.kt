package com.example.hava.ui.screens.planb

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Festival
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Phishing
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hava.R
import com.example.hava.data.model.HourlyForecast
import com.example.hava.data.model.WeatherReport
import com.example.hava.planb.ActivityEvaluation
import com.example.hava.planb.ActivityType
import com.example.hava.planb.CustomActivity
import com.example.hava.planb.CustomFactor
import com.example.hava.planb.FactorKind
import com.example.hava.planb.HourAssessment
import com.example.hava.planb.IndoorSuggestion
import com.example.hava.planb.InsufficientReason
import com.example.hava.planb.Measurement
import com.example.hava.planb.OverallAssessment
import com.example.hava.planb.ReasonCode
import com.example.hava.planb.SuggestedWindow
import com.example.hava.planb.SuitabilityLevel
import com.example.hava.ui.components.BulletLine
import com.example.hava.ui.components.ChevronIcon
import com.example.hava.ui.components.ErrorView
import com.example.hava.ui.components.FieldRow
import com.example.hava.ui.components.HavaCard
import com.example.hava.ui.components.IconBadge
import com.example.hava.ui.components.LandscapePalette
import com.example.hava.ui.components.LandscapeScene
import com.example.hava.ui.components.Landscapes
import com.example.hava.ui.components.LoadingView
import com.example.hava.ui.components.PrimaryButton
import com.example.hava.ui.components.ScoreBar
import com.example.hava.ui.components.ScoreRing
import com.example.hava.ui.components.ScreenTopBar
import com.example.hava.ui.components.SectionTitle
import com.example.hava.ui.components.StatusChip
import com.example.hava.ui.components.WeatherIcon
import com.example.hava.ui.components.hourLabel
import com.example.hava.ui.components.longDate
import com.example.hava.ui.components.lowercaseTr
import com.example.hava.ui.components.oneDecimal
import com.example.hava.ui.components.percentTr
import com.example.hava.ui.components.weatherDescriptionRes
import com.example.hava.ui.screens.city.ErrorType
import com.example.hava.ui.screens.city.messageRes
import com.example.hava.ui.theme.Brand
import com.example.hava.ui.theme.BrandSoft
import com.example.hava.ui.theme.CardBorder
import com.example.hava.ui.theme.Cream
import com.example.hava.ui.theme.Danger
import com.example.hava.ui.theme.DangerSoft
import com.example.hava.ui.theme.Info
import com.example.hava.ui.theme.InfoSoft
import com.example.hava.ui.theme.Ink
import com.example.hava.ui.theme.Muted
import com.example.hava.ui.theme.Success
import com.example.hava.ui.theme.SuccessSoft
import com.example.hava.ui.theme.Warning
import com.example.hava.ui.theme.WarningSoft
import kotlin.math.roundToInt

private val ScoreYellow = Color(0xFFE2B714)
private val ScoreOrange = Color(0xFFF08C00)
private const val VERY_SUITABLE_AT = 85

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
    val step = viewModel.step
    BackHandler(enabled = step != PlanBStep.FORM) { viewModel.back() }
    PlanBScreen(
        cityName = cityName,
        report = report,
        isLoading = isLoading,
        errorType = errorType,
        step = step,
        activity = viewModel.activity,
        customActivity = viewModel.customActivity,
        selectedDate = viewModel.selectedDate,
        startHour = viewModel.startHour,
        endHour = viewModel.endHour,
        evaluation = viewModel.evaluation,
        onActivitySelected = viewModel::onActivitySelected,
        onCustomActivity = viewModel::onCustomActivity,
        onDateSelected = viewModel::onDateSelected,
        onStartHour = viewModel::onStartHour,
        onEndHour = viewModel::onEndHour,
        onEvaluate = { report?.let(viewModel::showResult) },
        onOpen = viewModel::open,
        onApplyWindow = { window -> report?.let { viewModel.applyWindow(it, window) } },
        onApplyActivity = { activity -> report?.let { viewModel.applyActivity(it, activity) } },
        onRetry = onRetry,
        onBack = { if (!viewModel.back()) onBack() },
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
    step: PlanBStep,
    activity: ActivityType,
    customActivity: CustomActivity?,
    selectedDate: String,
    startHour: Int,
    endHour: Int,
    evaluation: ActivityEvaluation?,
    onActivitySelected: (ActivityType) -> Unit,
    onCustomActivity: (CustomActivity) -> Unit,
    onDateSelected: (String) -> Unit,
    onStartHour: (Int) -> Unit,
    onEndHour: (Int) -> Unit,
    onEvaluate: () -> Unit,
    onOpen: (PlanBStep) -> Unit,
    onApplyWindow: (SuggestedWindow) -> Unit,
    onApplyActivity: (ActivityType) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onChangeCity: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    CompositionLocalProvider(LocalContentColor provides Ink) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            ScreenTopBar(
                title = stringResource(R.string.planb_title),
                subtitle = if (step == PlanBStep.FORM) stringResource(R.string.planb_subtitle) else null,
                onBack = onBack,
                actions = { if (step == PlanBStep.FORM) CityChip(cityName, onChangeCity) },
            )
            when {
                errorType != null && report == null -> ErrorView(
                    message = stringResource(errorType.messageRes()),
                    onRetry = onRetry,
                )
                report == null || isLoading -> LoadingView()
                else -> AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "planbStep",
                ) { target ->
                    val result = evaluation
                    if (target == PlanBStep.FORM || result == null) {
                        PlanBForm(
                            report = report,
                            activity = activity,
                            customActivity = customActivity,
                            selectedDate = selectedDate,
                            startHour = startHour,
                            endHour = endHour,
                            onActivitySelected = onActivitySelected,
                            onCustomActivity = onCustomActivity,
                            onDateSelected = onDateSelected,
                            onStartHour = onStartHour,
                            onEndHour = onEndHour,
                            onEvaluate = onEvaluate,
                        )
                    } else {
                        val context = ResultContext(result, report, cityName)
                        when (target) {
                            PlanBStep.RESULT -> ResultContent(context, onOpen, onApplyWindow, onApplyActivity)
                            PlanBStep.ALTERNATIVES -> AlternativesContent(context, onOpen, onApplyWindow, onApplyActivity)
                            PlanBStep.COMPARE -> CompareContent(context, onSummary = onBack)
                            PlanBStep.PICK_ACTIVITY -> PickActivityContent(onApplyActivity)
                            PlanBStep.FORM -> Unit
                        }
                    }
                }
            }
        }
    }
}

private class ResultContext(
    val evaluation: ActivityEvaluation,
    val report: WeatherReport,
    val cityName: String,
) {
    val hoursByTime: Map<String, HourlyForecast> =
        report.hourlyByDate[evaluation.date].orEmpty().associateBy { it.time }
}

@Composable
private fun CityChip(cityName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(BrandSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.LocationOn, contentDescription = stringResource(R.string.planb_change_city), tint = Brand, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(cityName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Brand, maxLines = 1)
    }
}

@Composable
private fun PlanBForm(
    report: WeatherReport,
    activity: ActivityType,
    customActivity: CustomActivity?,
    selectedDate: String,
    startHour: Int,
    endHour: Int,
    onActivitySelected: (ActivityType) -> Unit,
    onCustomActivity: (CustomActivity) -> Unit,
    onDateSelected: (String) -> Unit,
    onStartHour: (Int) -> Unit,
    onEndHour: (Int) -> Unit,
    onEvaluate: () -> Unit,
) {
    var hoursDialog by rememberSaveable { mutableStateOf(false) }
    var customDialog by rememberSaveable { mutableStateOf(false) }
    val customSelected = activity == ActivityType.CUSTOM && customActivity != null
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(key = "banner") { PlanBanner(if (customSelected) ActivityType.CUSTOM.style().icon else activity.style().icon) }
        item(key = "question") { SectionTitle(stringResource(R.string.planb_question)) }
        item(key = "grid") {
            ActivityGrid(
                selected = activity,
                customName = customActivity?.name,
                onSelected = onActivitySelected,
                onCustom = { customDialog = true },
            )
        }
        if (customSelected) {
            item(key = "custom") { CustomActivityCard(customActivity, onEdit = { customDialog = true }) }
        }
        item(key = "date") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(stringResource(R.string.planb_date))
                DateField(report, selectedDate, onDateSelected)
            }
        }
        item(key = "hours") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(stringResource(R.string.planb_time_range))
                FieldRow(
                    icon = Icons.Outlined.Schedule,
                    text = stringResource(R.string.planb_range_value, clockLabel(startHour), clockLabel(endHour)),
                    onClick = { hoursDialog = true },
                )
            }
        }
        item(key = "evaluate") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Spacer(Modifier.height(2.dp))
                PrimaryButton(
                    text = stringResource(R.string.planb_evaluate),
                    onClick = onEvaluate,
                    enabled = selectedDate.isNotEmpty() && (activity != ActivityType.CUSTOM || customSelected),
                )
                Text(
                    text = stringResource(R.string.planb_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
    if (hoursDialog) {
        HoursDialog(
            startHour = startHour,
            endHour = endHour,
            onStartHour = onStartHour,
            onEndHour = onEndHour,
            onDismiss = { hoursDialog = false },
        )
    }
    if (customDialog) {
        CustomActivityDialog(
            initial = customActivity,
            onDismiss = { customDialog = false },
            onSave = {
                onCustomActivity(it)
                customDialog = false
            },
        )
    }
}

@Composable
private fun PlanBanner(icon: ImageVector) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        LandscapeScene(Landscapes.Banner, Modifier.matchParentSize())
        Box(
            Modifier
                .matchParentSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)), startY = 140f)),
        )
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF243B53),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 26.dp)
                .size(46.dp),
        )
        Text(
            text = stringResource(R.string.planb_banner),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
        )
    }
}

@Composable
private fun ActivityGrid(
    selected: ActivityType,
    customName: String?,
    onSelected: (ActivityType) -> Unit,
    onCustom: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ActivityType.entries.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { item ->
                    val custom = item == ActivityType.CUSTOM
                    ActivityTile(
                        activity = item,
                        label = if (custom) customName ?: stringResource(R.string.planb_custom_tile) else stringResource(item.labelRes()),
                        selected = item == selected,
                        onClick = { if (custom) onCustom() else onSelected(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ActivityTile(
    activity: ActivityType,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = activity.style()
    Surface(
        onClick = onClick,
        modifier = modifier.height(88.dp),
        shape = RoundedCornerShape(18.dp),
        color = style.soft,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) style.accent else style.accent.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            Icon(style.icon, contentDescription = null, tint = style.accent, modifier = Modifier.size(30.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) style.accent else Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DateField(report: WeatherReport, selectedDate: String, onDateSelected: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        FieldRow(
            icon = Icons.Outlined.CalendarMonth,
            text = if (selectedDate.isEmpty()) stringResource(R.string.planb_no_date) else longDate(selectedDate),
            onClick = { if (report.daily.isNotEmpty()) open = true },
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = Color.White) {
            report.daily.forEach { day ->
                DropdownMenuItem(
                    text = {
                        Text(
                            longDate(day.date),
                            color = if (day.date == selectedDate) Brand else Ink,
                            fontWeight = if (day.date == selectedDate) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onDateSelected(day.date)
                        open = false
                    },
                )
            }
        }
    }
}

@Composable
private fun HoursDialog(
    startHour: Int,
    endHour: Int,
    onStartHour: (Int) -> Unit,
    onEndHour: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(stringResource(R.string.planb_pick_hours), color = Ink) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                HourSlider(stringResource(R.string.planb_start_hour, clockLabel(startHour)), startHour, onStartHour)
                HourSlider(stringResource(R.string.planb_end_hour, clockLabel(endHour)), endHour, onEndHour)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok), color = Brand) }
        },
    )
}

@Composable
private fun HourSlider(label: String, hour: Int, onHour: (Int) -> Unit) {
    Text(label, style = MaterialTheme.typography.titleSmall, color = Ink)
    Slider(
        value = hour.toFloat(),
        onValueChange = { onHour(it.roundToInt()) },
        valueRange = 0f..23f,
        steps = 22,
        colors = SliderDefaults.colors(thumbColor = Brand, activeTrackColor = Brand),
    )
}

@Composable
private fun ResultContent(
    context: ResultContext,
    onOpen: (PlanBStep) -> Unit,
    onApplyWindow: (SuggestedWindow) -> Unit,
    onApplyActivity: (ActivityType) -> Unit,
) {
    val overall = context.evaluation.window.overall
    when {
        overall is OverallAssessment.Scored && overall.level == SuitabilityLevel.UNSUITABLE ->
            AlternativesContent(context, onOpen, onApplyWindow, onApplyActivity)
        overall is OverallAssessment.Scored -> ScoredResult(context, overall, onOpen)
        overall is OverallAssessment.Insufficient -> InsufficientResult(context, overall, onOpen)
    }
}

@Composable
private fun ResultList(content: LazyListScope.() -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun ScoredResult(
    context: ResultContext,
    overall: OverallAssessment.Scored,
    onOpen: (PlanBStep) -> Unit,
) {
    val evaluation = context.evaluation
    val suitable = overall.level == SuitabilityLevel.SUITABLE
    val activityName = evaluation.displayName()
    val factors = factorLines(evaluation, overall)
    ResultList {
        item(key = "header") {
            ActivityHeader(
                evaluation = evaluation,
                cityName = context.cityName,
                date = evaluation.date,
                recommended = suitable,
                onClick = { onOpen(PlanBStep.PICK_ACTIVITY) },
            )
        }
        item(key = "score") { ScoreCard(overall, activityName) }
        item(key = "window") {
            BestWindowCard(evaluation.bestWindow, onClick = { onOpen(PlanBStep.COMPARE) })
        }
        if (suitable) {
            if (factors.positive.isNotEmpty()) {
                item(key = "factors") {
                    FactorCard(stringResource(R.string.planb_factors_title), Icons.Outlined.Eco) {
                        factors.positive.forEach { FactorRow(it, Icons.Filled.CheckCircle, Success, Ink) }
                    }
                }
            }
            val watch = factors.neutral + factors.negative
            if (watch.isNotEmpty()) {
                item(key = "watch") {
                    FactorCard(stringResource(R.string.planb_watch_title), null) {
                        watch.forEach { FactorRow(it, Icons.Filled.Error, Warning, Ink) }
                    }
                }
            }
            item(key = "hourly") { HourlyStripCard(context, onClick = { onOpen(PlanBStep.COMPARE) }) }
            item(key = "no-need") { NoNeedBanner() }
        } else {
            item(key = "factors") {
                FactorCard(stringResource(R.string.planb_factors_title), Icons.Outlined.Eco) {
                    factors.positive.forEach { FactorRow(it, Icons.Filled.AddCircle, Success, Ink) }
                    factors.neutral.forEach { FactorRow(it, Icons.Filled.Error, Warning, Ink) }
                    factors.negative.forEach { FactorRow(it, Icons.Filled.Error, Warning, Danger) }
                }
            }
            item(key = "tip") { TipCard(evaluation, overall) }
            item(key = "alternatives") {
                PrimaryButton(
                    text = stringResource(R.string.planb_see_alternatives),
                    onClick = { onOpen(PlanBStep.ALTERNATIVES) },
                )
            }
        }
        item(key = "notes") { DataNotes(evaluation) }
    }
}

@Composable
private fun InsufficientResult(
    context: ResultContext,
    overall: OverallAssessment.Insufficient,
    onOpen: (PlanBStep) -> Unit,
) {
    val evaluation = context.evaluation
    ResultList {
        item(key = "header") {
            ActivityHeader(evaluation, context.cityName, evaluation.date, false) { onOpen(PlanBStep.PICK_ACTIVITY) }
        }
        item(key = "message") {
            StatusBanner(
                icon = Icons.Filled.Error,
                tint = Warning,
                background = WarningSoft,
                title = stringResource(R.string.planb_partly_title),
                body = when (overall.reason) {
                    InsufficientReason.NO_HOURLY_DATA -> stringResource(R.string.planb_no_hourly)
                    InsufficientReason.MISSING_CRITICAL -> stringResource(
                        R.string.planb_missing_critical,
                        joinedLabels(overall.missing),
                    )
                },
            )
        }
        if (evaluation.window.hours.isNotEmpty()) {
            item(key = "compare") {
                PrimaryButton(stringResource(R.string.planb_tab_hourly), onClick = { onOpen(PlanBStep.COMPARE) })
            }
        }
        item(key = "notes") { DataNotes(evaluation) }
    }
}

@Composable
private fun ActivityHeader(
    evaluation: ActivityEvaluation,
    cityName: String,
    date: String,
    recommended: Boolean,
    onClick: (() -> Unit)?,
) {
    val style = evaluation.activity.style()
    HavaCard(onClick = onClick, contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(style.icon, tint = style.accent, background = style.soft, size = 50.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(evaluation.displayName(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.planb_city_date, cityName, longDate(date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            if (recommended) StatusChip(stringResource(R.string.planb_recommended), Success, SuccessSoft)
        }
    }
}

@Composable
private fun ScoreCard(overall: OverallAssessment.Scored, activityName: String) {
    val color = overall.level.color()
    val title = overall.levelTitle()
    val summary = when (overall.level) {
        SuitabilityLevel.SUITABLE -> stringResource(R.string.planb_summary_suitable, activityName.lowercaseTr())
        SuitabilityLevel.CAUTION -> stringResource(R.string.planb_summary_caution)
        SuitabilityLevel.UNSUITABLE -> stringResource(R.string.planb_summary_unsuitable, activityName.lowercaseTr())
    }
    val description = stringResource(R.string.planb_score_cd, activityName, overall.score, title)
    HavaCard(modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ScoreRing(score = overall.score, color = color)
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
                Text(summary, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
    }
}

@Composable
private fun BestWindowCard(window: SuggestedWindow?, onClick: () -> Unit) {
    HavaCard(onClick = onClick) {
        Text(stringResource(R.string.planb_best_window_title), style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = Brand, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = window?.rangeText() ?: stringResource(R.string.no_value),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (window != null) {
                    Text(stringResource(R.string.planb_score_value, window.score), style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
            ChevronIcon()
        }
    }
}

@Composable
private fun FactorCard(title: String, icon: ImageVector?, content: @Composable () -> Unit) {
    HavaCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Success, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(title, style = MaterialTheme.typography.titleSmall)
        }
        content()
    }
}

@Composable
private fun FactorRow(text: String, icon: ImageVector, iconTint: Color, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = textColor)
    }
}

@Composable
private fun HourlyStripCard(context: ResultContext, onClick: () -> Unit) {
    val hours = context.evaluation.window.hours
    if (hours.isEmpty()) return
    HavaCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Outlined.WbSunny, tint = Info, background = InfoSoft, size = 26.dp)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.planb_hourly_title), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            ChevronIcon()
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(hours, key = { it.time }) { hour ->
                val forecast = context.hoursByTime[hour.time]
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.width(84.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(hourLabel(hour.time), style = MaterialTheme.typography.labelMedium, color = Muted)
                        if (forecast != null) {
                            WeatherIcon(
                                condition = forecast.condition,
                                contentDescription = stringResource(weatherDescriptionRes(forecast.weatherCode)),
                                isDay = forecast.isDay,
                                size = 22.sp,
                            )
                        }
                        Text(
                            hour.snapshot.temperatureC?.let { stringResource(R.string.temperature_value, it.roundToInt()) }
                                ?: stringResource(R.string.no_value),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoNeedBanner() {
    StatusBanner(
        icon = Icons.Filled.CheckCircle,
        tint = Success,
        background = SuccessSoft,
        title = stringResource(R.string.planb_no_need_title),
        body = stringResource(R.string.planb_no_need_body),
        titleColor = Success,
    )
}

@Composable
private fun StatusBanner(
    icon: ImageVector,
    tint: Color,
    background: Color,
    title: String,
    body: String,
    titleColor: Color = Ink,
    iconSize: Int = 28,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(18.dp),
        color = background,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize.dp))
            Spacer(Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = titleColor)
                Text(body, style = MaterialTheme.typography.bodySmall, color = Ink.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun TipCard(evaluation: ActivityEvaluation, overall: OverallAssessment.Scored) {
    val lines = suggestionLines(evaluation).ifEmpty { listOf(overall.explanation()) }
    HavaCard(containerColor = Cream, borderColor = Warning.copy(alpha = 0.2f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.TipsAndUpdates, contentDescription = null, tint = Brand, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.planb_tip_title), style = MaterialTheme.typography.titleSmall)
        }
        lines.forEach { BulletLine(it, bulletColor = Warning) }
    }
}

@Composable
private fun AlternativesContent(
    context: ResultContext,
    onOpen: (PlanBStep) -> Unit,
    onApplyWindow: (SuggestedWindow) -> Unit,
    onApplyActivity: (ActivityType) -> Unit,
) {
    val evaluation = context.evaluation
    val overall = evaluation.window.overall as? OverallAssessment.Scored
    val unsuitable = overall == null || overall.level == SuitabilityLevel.UNSUITABLE
    val suggestions = evaluation.suggestions
    val reason = stringResource((suggestions.reason ?: ReasonCode.WEATHER).labelRes())
    ResultList {
        item(key = "status") {
            StatusBanner(
                icon = Icons.Filled.Umbrella,
                tint = if (unsuitable) Danger else Warning,
                background = if (unsuitable) DangerSoft else WarningSoft,
                title = stringResource(if (unsuitable) R.string.planb_not_suitable_title else R.string.planb_partly_title),
                body = statusDetail(evaluation, overall),
                iconSize = 44,
                onClick = { onOpen(PlanBStep.COMPARE) },
            )
        }
        item(key = "title") { SectionTitle(stringResource(R.string.planb_alternatives_title)) }
        suggestions.betterWindow?.let { window ->
            item(key = "better") {
                AlternativeCard(
                    icon = Icons.Filled.Alarm,
                    tint = Success,
                    background = SuccessSoft,
                    label = stringResource(R.string.planb_better_time),
                    title = window.rangeText(),
                    body = null,
                    score = window.score,
                    onClick = { onApplyWindow(window) },
                )
            }
        }
        suggestions.alternativeActivity?.let { alternative ->
            item(key = "activity") {
                val style = alternative.activity.style()
                AlternativeCard(
                    icon = style.icon,
                    tint = ScoreOrange,
                    background = WarningSoft,
                    label = stringResource(R.string.planb_alt_activity),
                    title = stringResource(alternative.activity.labelRes()),
                    body = stringResource(R.string.planb_alt_activity_body),
                    score = alternative.score,
                    onClick = { onApplyActivity(alternative.activity) },
                )
            }
        }
        suggestions.indoor?.let { indoor ->
            item(key = "indoor") {
                AlternativeCard(
                    icon = Icons.Filled.Home,
                    tint = Info,
                    background = InfoSoft,
                    label = stringResource(R.string.planb_indoor_title),
                    title = stringResource(indoor.labelRes()),
                    body = stringResource(R.string.planb_indoor_body, reason.replaceFirstChar { it.uppercase() }),
                    score = null,
                    onClick = null,
                )
            }
        }
        if (suggestions.isEmpty) {
            item(key = "none") {
                HavaCard { Text(stringResource(R.string.planb_no_alternative), style = MaterialTheme.typography.bodyMedium) }
            }
        }
        item(key = "button") {
            PrimaryButton(
                text = stringResource(R.string.planb_see_alternatives),
                onClick = { onOpen(PlanBStep.PICK_ACTIVITY) },
            )
        }
        item(key = "quote") { QuoteCard() }
        item(key = "notes") { DataNotes(evaluation) }
    }
}

@Composable
private fun AlternativeCard(
    icon: ImageVector,
    tint: Color,
    background: Color,
    label: String,
    title: String,
    body: String?,
    score: Int?,
    onClick: (() -> Unit)?,
) {
    HavaCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(background),
                contentAlignment = Alignment.Center,
            ) {
                IconBadge(icon, tint = Color.White, background = tint, size = 38.dp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (body != null) Text(body, style = MaterialTheme.typography.bodySmall, color = Muted)
                if (score != null) {
                    Text(stringResource(R.string.planb_score_value, score), style = MaterialTheme.typography.bodySmall, color = Success)
                }
            }
            if (onClick != null) ChevronIcon()
        }
    }
}

@Composable
private fun QuoteCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        LandscapeScene(Landscapes.Quote, Modifier.matchParentSize())
        Text(
            text = "“" + stringResource(R.string.planb_quote) + "”",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = Ink.copy(alpha = 0.85f),
            textAlign = TextAlign.End,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(start = 56.dp, end = 18.dp, top = 18.dp),
        )
    }
}

@Composable
private fun CompareContent(context: ResultContext, onSummary: () -> Unit) {
    val evaluation = context.evaluation
    ResultList {
        item(key = "header") { ActivityHeader(evaluation, context.cityName, evaluation.date, false, null) }
        item(key = "tabs") { CompareTabs(onSummary) }
        items(evaluation.window.hours, key = { it.time }) { hour ->
            HourCompareCard(hour, context.hoursByTime[hour.time])
        }
        evaluation.bestWindow?.let { window ->
            item(key = "suggested") {
                val activityName = evaluation.displayName().lowercaseTr()
                HavaCard(containerColor = SuccessSoft, borderColor = Success.copy(alpha = 0.25f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.Filled.Alarm, tint = Color.White, background = Success, size = 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(stringResource(R.string.planb_suggested_time), style = MaterialTheme.typography.titleSmall, color = Success)
                            Text(window.rangeText(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = if (window.fullySuitable) {
                            stringResource(R.string.planb_suggested_body_full, activityName)
                        } else {
                            stringResource(R.string.planb_suggested_body_partial)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CompareTabs(onSummary: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE9ECF5))
            .padding(4.dp),
    ) {
        Text(
            text = stringResource(R.string.planb_tab_hourly),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1.4f)
                .clip(RoundedCornerShape(10.dp))
                .background(Brand)
                .padding(vertical = 9.dp),
        )
        Text(
            text = stringResource(R.string.planb_tab_summary),
            style = MaterialTheme.typography.labelLarge,
            color = Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onSummary)
                .padding(vertical = 9.dp),
        )
    }
}

@Composable
private fun HourCompareCard(hour: HourAssessment, forecast: HourlyForecast?) {
    val score = (hour as? HourAssessment.Scored)?.score
    val color = score?.let(::scoreColor) ?: Muted
    HavaCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp), spacing = 8.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(hourLabel(hour.time), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(54.dp))
            if (forecast != null) {
                WeatherIcon(
                    condition = forecast.condition,
                    contentDescription = stringResource(weatherDescriptionRes(forecast.weatherCode)),
                    isDay = forecast.isDay,
                    size = 18.sp,
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                hour.snapshot.temperatureC?.let { stringResource(R.string.temperature_value, it.roundToInt()) }
                    ?: stringResource(R.string.no_value),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(40.dp),
            )
            Icon(Icons.Outlined.Umbrella, contentDescription = null, tint = Muted, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                hour.snapshot.precipitationProbabilityPercent?.let(::percentTr) ?: stringResource(R.string.no_value),
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                modifier = Modifier.weight(1f),
            )
            if (score != null) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("/100", style = MaterialTheme.typography.labelSmall, color = Muted, modifier = Modifier.padding(bottom = 2.dp))
                }
            } else {
                Text(stringResource(R.string.planb_hour_no_data), style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
        if (score != null) {
            Row {
                Spacer(Modifier.weight(1f))
                ScoreBar(fraction = score / 100f, color = color, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PickActivityContent(onPick: (ActivityType) -> Unit) {
    ResultList {
        item(key = "title") {
            Text(
                stringResource(R.string.planb_pick_activity),
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
        items(ActivityType.entries.filter { it != ActivityType.CUSTOM }, key = { it.name }) { activity ->
            HavaCard(onClick = { onPick(activity) }, contentPadding = PaddingValues(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ActivityThumb(activity)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(activity.labelRes()), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(stringResource(activity.factorsRes()), style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    ChevronIcon()
                }
            }
        }
    }
}

@Composable
private fun ActivityThumb(activity: ActivityType) {
    val style = activity.style()
    val palette = remember(activity) {
        LandscapePalette(
            skyTop = style.accent.copy(alpha = 0.35f).compositeOver(Color.White),
            skyBottom = style.soft,
            clouds = Color.White.copy(alpha = 0.8f),
            far = style.accent.copy(alpha = 0.3f),
            water = if (activity == ActivityType.BEACH || activity == ActivityType.FISHING) Info.copy(alpha = 0.45f) else null,
            ground = style.accent.copy(alpha = 0.45f),
            trees = style.accent.copy(alpha = 0.8f),
        )
    }
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        LandscapeScene(palette, Modifier.matchParentSize())
        Icon(style.icon, contentDescription = null, tint = Ink.copy(alpha = 0.85f), modifier = Modifier.size(34.dp))
    }
}

@Composable
private fun DataNotes(evaluation: ActivityEvaluation) {
    val notes = buildList {
        if (evaluation.requestedHourCount > evaluation.window.hours.size && evaluation.window.hours.isNotEmpty()) {
            add(stringResource(R.string.planb_partial_hours, evaluation.requestedHourCount, evaluation.window.hours.size))
        }
        if (FactorKind.AIR_QUALITY in evaluation.window.unavailable) add(stringResource(R.string.planb_aqi_missing))
        if (FactorKind.WAVE_HEIGHT in evaluation.window.unavailable) add(stringResource(R.string.planb_wave_missing))
        val overall = evaluation.window.overall as? OverallAssessment.Scored
        if (overall?.missingOptional?.contains(FactorKind.WIND_DIRECTION) == true) {
            add(stringResource(R.string.planb_direction_missing))
        } else if (evaluation.windDirectionKnown) {
            add(stringResource(R.string.planb_direction_unused))
        }
        val otherMissing = overall?.missingOptional.orEmpty().filter { it != FactorKind.WIND_DIRECTION }
        if (otherMissing.isNotEmpty()) add(stringResource(R.string.planb_data_partial, joinedLabels(otherMissing)))
        add(stringResource(R.string.planb_disclaimer))
    }
    Column(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        notes.forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = Muted) }
    }
}

private class FactorLines(val positive: List<String>, val neutral: List<String>, val negative: List<String>)

@Composable
private fun factorLines(evaluation: ActivityEvaluation, overall: OverallAssessment.Scored): FactorLines {
    val measured = evaluation.measurements.associateBy { it.kind }
    val present = evaluation.window.hours
        .filterIsInstance<HourAssessment.Scored>()
        .flatMap { hour -> hour.factors.map { it.kind } }
        .distinct()
    val neutral = present.filter { it !in overall.positive && it !in overall.negative }
    return FactorLines(
        positive = overall.positive.mapNotNull { kind -> kind.positiveRes()?.let { factorLine(it, measured[kind]) } },
        neutral = neutral.mapNotNull { kind -> kind.neutralRes()?.let { factorLine(it, measured[kind]) } },
        negative = overall.negative.mapNotNull { kind -> kind.negativeRes()?.let { factorLine(it, measured[kind]) } },
    )
}

@Composable
private fun factorLine(res: Int, measurement: Measurement?): String {
    val label = stringResource(res)
    val value = measurement?.shortValue()
    return if (value.isNullOrEmpty()) label else "$label ($value)"
}

@Composable
private fun suggestionLines(evaluation: ActivityEvaluation): List<String> {
    val suggestions = evaluation.suggestions
    val reason = stringResource((suggestions.reason ?: ReasonCode.WEATHER).labelRes())
    return buildList {
        suggestions.betterWindow?.let { window ->
            add(
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
            add(
                stringResource(
                    R.string.planb_alternative,
                    stringResource(alternative.activity.labelRes()),
                    alternative.score,
                    stringResource(alternative.level.labelRes()),
                ),
            )
        }
        suggestions.indoor?.let { indoor ->
            add(stringResource(R.string.planb_indoor_line, reason.replaceFirstChar { it.uppercase() }, stringResource(indoor.labelRes())))
        }
    }
}

@Composable
private fun statusDetail(evaluation: ActivityEvaluation, overall: OverallAssessment.Scored?): String {
    val worst = evaluation.window.hours
        .filterIsInstance<HourAssessment.Scored>()
        .minByOrNull { it.score }
    val parts = buildList {
        worst?.snapshot?.precipitationProbabilityPercent?.let { add(stringResource(R.string.planb_detail_precip, percentTr(it))) }
        worst?.snapshot?.windSpeedKmh?.let { add(stringResource(R.string.planb_detail_wind, it.roundToInt())) }
        worst?.snapshot?.temperatureC?.let { add(stringResource(R.string.planb_detail_temp, it.roundToInt())) }
    }
    val worstText = if (worst != null && parts.isNotEmpty()) {
        stringResource(R.string.planb_worst_hour, hourLabel(worst.time), parts.joinToString(", "))
    } else {
        null
    }
    val explanation = overall?.explanation()
    return listOfNotNull(worstText, explanation).joinToString(" ")
}

@Composable
private fun OverallAssessment.Scored.explanation(): String = when {
    safetyOverride -> stringResource(R.string.planb_explain_severe)
    level == SuitabilityLevel.SUITABLE -> stringResource(R.string.planb_explain_suitable)
    level == SuitabilityLevel.CAUTION -> stringResource(R.string.planb_explain_caution, stringResource((dominantReason ?: ReasonCode.WEATHER).labelRes()))
    else -> stringResource(R.string.planb_explain_unsuitable, stringResource((dominantReason ?: ReasonCode.WEATHER).labelRes()))
}

@Composable
private fun OverallAssessment.Scored.levelTitle(): String = when {
    level == SuitabilityLevel.SUITABLE && score >= VERY_SUITABLE_AT -> stringResource(R.string.planb_level_very_suitable)
    else -> stringResource(level.labelRes())
}

@Composable
private fun SuggestedWindow.rangeText(): String {
    val start = hourLabel(startTime)
    val end = hourLabel(endTime)
    return if (start == end) start else stringResource(R.string.planb_range_value, start, end)
}

@Composable
private fun joinedLabels(kinds: List<FactorKind>): String {
    val labels = ArrayList<String>(kinds.size)
    for (kind in kinds) labels += stringResource(kind.labelRes())
    return labels.joinToString()
}

private fun Measurement.shortValue(): String = when (kind) {
    FactorKind.TEMPERATURE -> {
        val low = min.roundToInt()
        val high = max.roundToInt()
        if (low == high) "$high°" else "$low° - $high°"
    }
    FactorKind.PRECIPITATION_PROBABILITY -> percentTr(max.roundToInt())
    FactorKind.PRECIPITATION_AMOUNT -> "${oneDecimal(max)} mm"
    FactorKind.WIND_SPEED, FactorKind.WIND_GUSTS -> "${max.roundToInt()} km/sa"
    FactorKind.UV_INDEX -> max.roundToInt().toString()
    else -> ""
}

private fun scoreColor(score: Int): Color = when {
    score >= 80 -> Success
    score >= 70 -> ScoreYellow
    score >= 45 -> ScoreOrange
    else -> Danger
}

private fun SuitabilityLevel.color(): Color = when (this) {
    SuitabilityLevel.SUITABLE -> Success
    SuitabilityLevel.CAUTION -> Warning
    SuitabilityLevel.UNSUITABLE -> Danger
}

private fun clockLabel(hour: Int): String = "%02d:00".format(hour)

private data class ActivityStyle(val icon: ImageVector, val accent: Color, val soft: Color)

private fun ActivityType.style(): ActivityStyle = when (this) {
    ActivityType.PICNIC -> ActivityStyle(Icons.Filled.Park, Color(0xFF2E9E5B), Color(0xFFE6F6EC))
    ActivityType.WALK -> ActivityStyle(Icons.AutoMirrored.Filled.DirectionsWalk, Brand, BrandSoft)
    ActivityType.RUN -> ActivityStyle(Icons.AutoMirrored.Filled.DirectionsRun, Danger, DangerSoft)
    ActivityType.BIKE -> ActivityStyle(Icons.AutoMirrored.Filled.DirectionsBike, ScoreOrange, WarningSoft)
    ActivityType.BEACH -> ActivityStyle(Icons.Filled.Waves, Info, InfoSoft)
    ActivityType.CAMPING -> ActivityStyle(Icons.Filled.Festival, Color(0xFF3E7C4F), Color(0xFFE8F3EA))
    ActivityType.HIKING -> ActivityStyle(Icons.Filled.Hiking, Color(0xFF8D6E3F), Color(0xFFF5EEE3))
    ActivityType.YOGA -> ActivityStyle(Icons.Filled.SelfImprovement, Color(0xFF9B5DE5), Color(0xFFF3EAFD))
    ActivityType.TENNIS -> ActivityStyle(Icons.Filled.SportsTennis, Color(0xFF6A9A12), Color(0xFFF1F8E2))
    ActivityType.BASKETBALL -> ActivityStyle(Icons.Filled.SportsBasketball, Color(0xFFE8590C), Color(0xFFFDEDE3))
    ActivityType.VOLLEYBALL -> ActivityStyle(Icons.Filled.SportsVolleyball, Color(0xFFC9900A), Color(0xFFFFF6DB))
    ActivityType.FOOTBALL -> ActivityStyle(Icons.Filled.SportsSoccer, Color(0xFF2B8A3E), Color(0xFFE6F4EA))
    ActivityType.FISHING -> ActivityStyle(Icons.Filled.Phishing, Color(0xFF0B7285), Color(0xFFE3F4F7))
    ActivityType.CUSTOM -> ActivityStyle(Icons.Filled.Tune, Color(0xFF495579), Color(0xFFEEF0F6))
}

private fun ActivityType.labelRes(): Int = when (this) {
    ActivityType.PICNIC -> R.string.planb_picnic
    ActivityType.WALK -> R.string.planb_walk
    ActivityType.RUN -> R.string.planb_run
    ActivityType.BIKE -> R.string.planb_bike
    ActivityType.BEACH -> R.string.planb_beach
    ActivityType.CAMPING -> R.string.planb_camping
    ActivityType.HIKING -> R.string.planb_hiking
    ActivityType.YOGA -> R.string.planb_yoga
    ActivityType.TENNIS -> R.string.planb_tennis
    ActivityType.BASKETBALL -> R.string.planb_basketball
    ActivityType.VOLLEYBALL -> R.string.planb_volleyball
    ActivityType.FOOTBALL -> R.string.planb_football
    ActivityType.FISHING -> R.string.planb_fishing
    ActivityType.CUSTOM -> R.string.planb_custom
}

private fun ActivityType.factorsRes(): Int = when (this) {
    ActivityType.PICNIC -> R.string.planb_factors_picnic
    ActivityType.WALK -> R.string.planb_factors_walk
    ActivityType.RUN -> R.string.planb_factors_run
    ActivityType.BIKE -> R.string.planb_factors_bike
    ActivityType.BEACH -> R.string.planb_factors_beach
    ActivityType.CAMPING -> R.string.planb_factors_camping
    ActivityType.HIKING -> R.string.planb_factors_hiking
    ActivityType.YOGA -> R.string.planb_factors_yoga
    ActivityType.TENNIS -> R.string.planb_factors_tennis
    ActivityType.BASKETBALL -> R.string.planb_factors_basketball
    ActivityType.VOLLEYBALL -> R.string.planb_factors_volleyball
    ActivityType.FOOTBALL -> R.string.planb_factors_football
    ActivityType.FISHING -> R.string.planb_factors_fishing
    ActivityType.CUSTOM -> R.string.planb_factors_custom
}

@Composable
private fun ActivityEvaluation.displayName(): String =
    custom?.name?.takeIf { it.isNotBlank() } ?: stringResource(activity.labelRes())

@Composable
private fun CustomActivityCard(custom: CustomActivity, onEdit: () -> Unit) {
    val style = ActivityType.CUSTOM.style()
    HavaCard(onClick = onEdit, contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(style.icon, tint = style.accent, background = style.soft, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(custom.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.planb_custom_tracking), style = MaterialTheme.typography.labelSmall, color = Muted)
            }
            Text(
                stringResource(R.string.planb_custom_edit),
                style = MaterialTheme.typography.labelLarge,
                color = Brand,
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (factor in CustomFactor.entries.filter { it in custom.factors }) {
                val label = if (factor == CustomFactor.TEMPERATURE) {
                    "${stringResource(factor.labelRes())} · ${stringResource(custom.temperature.labelRes())}"
                } else {
                    stringResource(factor.labelRes())
                }
                StatusChip(label, color = Brand, background = BrandSoft)
            }
        }
    }
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

private fun FactorKind.positiveRes(): Int? = when (this) {
    FactorKind.TEMPERATURE -> R.string.planb_pos_temperature
    FactorKind.PRECIPITATION_PROBABILITY -> R.string.planb_pos_precip_prob
    FactorKind.PRECIPITATION_AMOUNT -> R.string.planb_pos_precip_amount
    FactorKind.WIND_SPEED -> R.string.planb_pos_wind
    FactorKind.WIND_GUSTS -> R.string.planb_pos_gust
    FactorKind.UV_INDEX -> R.string.planb_pos_uv
    FactorKind.WEATHER_CODE -> R.string.planb_pos_code
    else -> null
}

private fun FactorKind.neutralRes(): Int? = when (this) {
    FactorKind.TEMPERATURE -> R.string.planb_mid_temperature
    FactorKind.PRECIPITATION_PROBABILITY -> R.string.planb_mid_precip_prob
    FactorKind.PRECIPITATION_AMOUNT -> R.string.planb_mid_precip_amount
    FactorKind.WIND_SPEED -> R.string.planb_mid_wind
    FactorKind.WIND_GUSTS -> R.string.planb_mid_gust
    FactorKind.UV_INDEX -> R.string.planb_mid_uv
    FactorKind.WEATHER_CODE -> R.string.planb_mid_code
    else -> null
}

private fun FactorKind.negativeRes(): Int? = when (this) {
    FactorKind.TEMPERATURE -> R.string.planb_neg_temperature
    FactorKind.PRECIPITATION_PROBABILITY -> R.string.planb_neg_precip_prob
    FactorKind.PRECIPITATION_AMOUNT -> R.string.planb_neg_precip_amount
    FactorKind.WIND_SPEED -> R.string.planb_neg_wind
    FactorKind.WIND_GUSTS -> R.string.planb_neg_gust
    FactorKind.UV_INDEX -> R.string.planb_neg_uv
    FactorKind.WEATHER_CODE -> R.string.planb_neg_code
    else -> null
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
    IndoorSuggestion.CABIN -> R.string.planb_indoor_cabin
    IndoorSuggestion.YOGA_STUDIO -> R.string.planb_indoor_yoga
    IndoorSuggestion.INDOOR_COURT -> R.string.planb_indoor_court
    IndoorSuggestion.INDOOR_FOOTBALL -> R.string.planb_indoor_football
    IndoorSuggestion.GENERAL -> R.string.planb_indoor_general
}
