package com.example.hava.ui.screens.planb

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.hava.data.model.WeatherReport
import com.example.hava.planb.ActivityEvaluation
import com.example.hava.planb.ActivityType
import com.example.hava.planb.CustomActivity
import com.example.hava.planb.CustomFactor
import com.example.hava.planb.PlanBEvaluator
import com.example.hava.planb.SuggestedWindow
import com.example.hava.planb.TemperaturePreference
import com.example.hava.planb.forecastHour
import com.example.hava.planb.toHourConditions

/** PlanB içindeki ekranlar; görseldeki akışın sırası. */
enum class PlanBStep { FORM, RESULT, COMPARE, ALTERNATIVES, PICK_ACTIVITY }

@OptIn(SavedStateHandleSaveableApi::class)
class PlanBViewModel(
    savedStateHandle: SavedStateHandle,
    private val evaluator: PlanBEvaluator = PlanBEvaluator(),
) : ViewModel() {

    var activityName by savedStateHandle.saveable { mutableStateOf(ActivityType.PICNIC.name) }
        private set

    var selectedDate by savedStateHandle.saveable { mutableStateOf("") }
        private set

    var startHour by savedStateHandle.saveable { mutableStateOf(10) }
        private set

    var endHour by savedStateHandle.saveable { mutableStateOf(16) }
        private set

    var evaluation by mutableStateOf<ActivityEvaluation?>(null)
        private set

    private var stackNames by savedStateHandle.saveable { mutableStateOf(PlanBStep.FORM.name) }

    private var customName by savedStateHandle.saveable { mutableStateOf("") }
    private var customFactorNames by savedStateHandle.saveable { mutableStateOf("") }
    private var customTemperatureName by savedStateHandle.saveable { mutableStateOf(TemperaturePreference.MILD.name) }

    val activity: ActivityType
        get() = runCatching { ActivityType.valueOf(activityName) }.getOrDefault(ActivityType.PICNIC)

    /** Kullanıcı henüz özel aktivite tanımlamadıysa null. */
    val customActivity: CustomActivity?
        get() {
            if (customName.isBlank()) return null
            val factors = customFactorNames.split(',')
                .mapNotNull { name -> runCatching { CustomFactor.valueOf(name) }.getOrNull() }
                .toSet()
            val temperature = runCatching { TemperaturePreference.valueOf(customTemperatureName) }
                .getOrDefault(TemperaturePreference.MILD)
            return CustomActivity(customName, factors, temperature)
        }

    /** Değerlendirme yoksa (ör. süreç yeniden başladıysa) sonuç ekranları yerine form gösterilir. */
    val step: PlanBStep
        get() {
            val last = stack().last()
            return if (last != PlanBStep.FORM && evaluation == null) PlanBStep.FORM else last
        }

    private var boundCityId: Long? = null

    fun onActivitySelected(activity: ActivityType) {
        if (activityName == activity.name) return
        activityName = activity.name
        evaluation = null
    }

    fun onCustomActivity(custom: CustomActivity) {
        customName = custom.name.trim()
        customFactorNames = custom.factors.joinToString(",") { it.name }
        customTemperatureName = custom.temperature.name
        activityName = ActivityType.CUSTOM.name
        evaluation = null
    }

    fun onDateSelected(date: String) {
        if (selectedDate == date) return
        selectedDate = date
        evaluation = null
    }

    fun onStartHour(hour: Int) {
        val next = hour.coerceIn(0, 23)
        if (next == startHour) return
        startHour = next
        if (endHour < startHour) endHour = startHour
        evaluation = null
    }

    fun onEndHour(hour: Int) {
        val next = hour.coerceIn(0, 23)
        if (next == endHour) return
        endHour = next
        if (startHour > endHour) startHour = endHour
        evaluation = null
    }

    /** Yeni şehirde saat aralığı, o günün ilk tahmin saatine çekilir. */
    fun onReport(cityId: Long, report: WeatherReport) {
        if (boundCityId != cityId) {
            val firstBind = boundCityId == null
            boundCityId = cityId
            evaluation = null
            if (!firstBind) stackNames = PlanBStep.FORM.name
            val date = report.daily.firstOrNull { report.hourlyByDate[it.date].orEmpty().isNotEmpty() }?.date
                ?: report.daily.firstOrNull()?.date
            selectedDate = date.orEmpty()
            val firstHour = selectedDate
                .takeIf { it.isNotEmpty() }
                ?.let { report.hourlyByDate[it].orEmpty().firstOrNull()?.time?.forecastHour() }
            if (firstHour != null) {
                startHour = firstHour
                endHour = (firstHour + 4).coerceAtMost(23)
            }
        } else if (selectedDate.isNotEmpty() && report.daily.none { it.date == selectedDate }) {
            selectedDate = report.daily.firstOrNull()?.date.orEmpty()
            evaluation = null
        }
    }

    fun evaluate(report: WeatherReport) {
        val date = selectedDate
        if (date.isEmpty()) return
        val dayHours = report.hourlyByDate[date].orEmpty().map { it.toHourConditions() }
        evaluation = evaluator.evaluate(
            activity = activity,
            date = date,
            startHour = startHour,
            endHour = endHour,
            dayHours = dayHours,
            custom = customActivity,
        )
    }

    fun showResult(report: WeatherReport) {
        evaluate(report)
        if (evaluation != null) stackNames = listOf(PlanBStep.FORM, PlanBStep.RESULT).joinToString(",") { it.name }
    }

    fun applyWindow(report: WeatherReport, window: SuggestedWindow) {
        val start = window.startTime.forecastHour() ?: return
        val end = window.endTime.forecastHour() ?: start
        startHour = start
        endHour = end.coerceAtLeast(start)
        showResult(report)
    }

    fun applyActivity(report: WeatherReport, activity: ActivityType) {
        activityName = activity.name
        showResult(report)
    }

    fun open(step: PlanBStep) {
        if (step == PlanBStep.FORM) {
            stackNames = PlanBStep.FORM.name
            return
        }
        val current = stack()
        if (current.last() == step) return
        stackNames = (current + step).joinToString(",") { it.name }
    }

    /** Geri tuşu PlanB içinde işlendiyse true; formdaysa false döner. */
    fun back(): Boolean {
        if (step == PlanBStep.FORM) return false
        stackNames = stack().dropLast(1).ifEmpty { listOf(PlanBStep.FORM) }.joinToString(",") { it.name }
        return true
    }

    private fun stack(): List<PlanBStep> = stackNames
        .split(',')
        .mapNotNull { name -> runCatching { PlanBStep.valueOf(name) }.getOrNull() }
        .ifEmpty { listOf(PlanBStep.FORM) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { PlanBViewModel(createSavedStateHandle()) }
        }
    }
}
