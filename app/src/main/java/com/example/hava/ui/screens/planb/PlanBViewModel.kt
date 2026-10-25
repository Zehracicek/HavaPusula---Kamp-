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
import com.example.hava.planb.PlanBEvaluator
import com.example.hava.planb.forecastHour
import com.example.hava.planb.toHourConditions

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

    val activity: ActivityType
        get() = runCatching { ActivityType.valueOf(activityName) }.getOrDefault(ActivityType.PICNIC)

    private var boundCityId: Long? = null

    fun onActivitySelected(activity: ActivityType) {
        if (activityName == activity.name) return
        activityName = activity.name
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
            boundCityId = cityId
            evaluation = null
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
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { PlanBViewModel(createSavedStateHandle()) }
        }
    }
}
