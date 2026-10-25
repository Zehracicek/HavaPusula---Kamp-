package com.example.hava.ui.screens.guard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.hava.data.local.AppPreferencesStore
import com.example.hava.data.model.WeatherReport
import com.example.hava.data.remote.dto.City
import com.example.hava.data.repository.WeatherRepository
import com.example.hava.data.repository.WeatherRepositoryImpl
import com.example.hava.guard.GuardReport
import com.example.hava.guard.RiskEngine
import com.example.hava.guard.RiskSettings
import com.example.hava.guard.UserRisk
import com.example.hava.guard.toGuardHours
import com.example.hava.planb.HourConditions
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoriteGuard(
    val city: City,
    val hours: List<HourConditions> = emptyList(),
    val report: GuardReport? = null,
    val loading: Boolean = false,
    val failed: Boolean = false,
)

data class GuardUiState(
    val cityName: String = "",
    val observedAt: String? = null,
    val settings: RiskSettings = RiskSettings(),
    val report: GuardReport? = null,
    val favorites: List<FavoriteGuard> = emptyList(),
    val waitingForWeather: Boolean = true,
    val notificationsEnabled: Boolean = false,
)

class WeatherGuardViewModel(
    private val repository: WeatherRepository,
    private val preferences: AppPreferencesStore,
    private val engine: RiskEngine = RiskEngine(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GuardUiState(
            settings = preferences.riskSettings(),
            notificationsEnabled = preferences.notificationsEnabled(),
        ),
    )
    val uiState: StateFlow<GuardUiState> = _uiState.asStateFlow()

    private var hours: List<HourConditions> = emptyList()
    private var favoriteJob: Job? = null

    fun bind(city: City, report: WeatherReport?) {
        val settings = preferences.riskSettings()
        hours = report?.toGuardHours().orEmpty()
        _uiState.update {
            it.copy(
                cityName = report?.current?.cityName ?: city.name,
                observedAt = report?.observedAt,
                settings = settings,
                report = report?.let { engine.evaluate(hours, settings) },
                waitingForWeather = report == null,
                notificationsEnabled = preferences.notificationsEnabled(),
            )
        }
        if (report != null) loadFavorites(city, settings)
    }

    fun setEnabled(risk: UserRisk, enabled: Boolean) {
        val current = _uiState.value.settings
        val enabledSet = if (enabled) current.enabled + risk else current.enabled - risk
        save(current.copy(enabled = enabledSet))
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        preferences.setNotificationsEnabled(enabled)
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun save(settings: RiskSettings) {
        val normalized = settings.normalized()
        preferences.saveRiskSettings(normalized)
        _uiState.update { state ->
            state.copy(
                settings = normalized,
                report = if (state.waitingForWeather) null else engine.evaluate(hours, normalized),
                favorites = state.favorites.map { favorite ->
                    if (favorite.hours.isEmpty()) favorite
                    else favorite.copy(report = engine.evaluate(favorite.hours, normalized))
                },
            )
        }
    }

    private fun loadFavorites(current: City, settings: RiskSettings) {
        favoriteJob?.cancel()
        val cities = preferences.favoriteCities()
        favoriteJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    favorites = cities.map { city ->
                        if (city.id == current.id) {
                            FavoriteGuard(city, hours, engine.evaluate(hours, settings))
                        } else {
                            FavoriteGuard(city, loading = true)
                        }
                    },
                )
            }
            for (city in cities) {
                if (city.id == current.id) continue
                val result = repository.getWeather(city)
                val favoriteHours = result.getOrNull()?.toGuardHours().orEmpty()
                _uiState.update { state ->
                    state.copy(
                        favorites = state.favorites.map { row ->
                            if (row.city.id != city.id) row
                            else if (result.isSuccess) {
                                row.copy(
                                    hours = favoriteHours,
                                    report = engine.evaluate(favoriteHours, state.settings),
                                    loading = false,
                                )
                            } else {
                                row.copy(loading = false, failed = true)
                            }
                        },
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                WeatherGuardViewModel(
                    repository = WeatherRepositoryImpl(),
                    preferences = AppPreferencesStore(application),
                )
            }
        }
    }
}

private fun RiskSettings.normalized(): RiskSettings = copy(
    precipProbabilityCautionPercent = precipProbabilityCautionPercent.coerceIn(10, 95),
    precipProbabilityHighPercent = precipProbabilityHighPercent.coerceIn(precipProbabilityCautionPercent, 100),
    precipAmountCautionMm = precipAmountCautionMm.coerceAtLeast(0.1),
    precipAmountHighMm = precipAmountHighMm.coerceAtLeast(precipAmountCautionMm),
    windCautionKmh = windCautionKmh.coerceIn(10.0, 80.0),
    windHighKmh = windHighKmh.coerceAtLeast(windCautionKmh),
    gustCautionKmh = gustCautionKmh.coerceIn(20.0, 100.0),
    gustHighKmh = gustHighKmh.coerceAtLeast(gustCautionKmh),
    heatCautionC = heatCautionC.coerceIn(25.0, 45.0),
    heatHighC = heatHighC.coerceAtLeast(heatCautionC),
    frostCautionC = frostCautionC.coerceIn(-5.0, 8.0),
    frostHighC = frostHighC.coerceAtMost(frostCautionC),
    uvCaution = uvCaution.coerceIn(2.0, 10.0),
    uvHigh = uvHigh.coerceAtLeast(uvCaution),
)
