package com.example.hava.ui.screens.weather

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.hava.data.local.CountryCatalog
import com.example.hava.data.remote.dto.City
import com.example.hava.data.repository.LocationRepository
import com.example.hava.data.repository.LocationRepositoryImpl
import com.example.hava.data.repository.WeatherRepository
import com.example.hava.data.repository.WeatherRepositoryImpl
import com.example.hava.data.repository.hasLocationPermission
import com.example.hava.ui.background.WeatherScene
import com.example.hava.ui.background.toScene
import com.example.hava.ui.screens.city.toErrorType
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val locationRepository: LocationRepository,
    hasLocationPermission: Boolean,
    private val repository: WeatherRepository = WeatherRepositoryImpl(),
    defaultCity: City = CountryCatalog.defaultCity,
) : ViewModel() {

    private val savedCity: City? = savedStateHandle.get<String>(KEY_CITY)
        ?.let { runCatching { gson.fromJson(it, City::class.java) }.getOrNull() }

    /** Uygulama ilk kez açıldı; kullanıcıya konum izni sorulabilir. */
    val isFreshStart: Boolean = savedCity == null

    // Süreç sonlandırılıp geri dönüldüğünde varsayılan şehre değil son gösterilen şehre dönülür
    private val _currentCity = MutableStateFlow(savedCity ?: defaultCity)
    val currentCity: StateFlow<City> = _currentCity.asStateFlow()

    private var city: City
        get() = _currentCity.value
        set(value) {
            _currentCity.value = value
            savedStateHandle[KEY_CITY] = gson.toJson(value)
        }

    private val startWithLocation = isFreshStart && hasLocationPermission

    private val _uiState = MutableStateFlow<WeatherUiState>(
        WeatherUiState.Loading(if (startWithLocation) null else city.name)
    )
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    /** Listeden seçilen şehir; varsayılan şehir ve konum için null. */
    val selectedCityId: StateFlow<Long?> = savedStateHandle.getStateFlow(KEY_SELECTED_CITY_ID, null)

    // Yeni şehir yüklenirken veya hata olduğunda son başarılı sahne korunur
    private val _scene = MutableStateFlow(WeatherScene.fromDeviceClock())
    val scene: StateFlow<WeatherScene> = _scene.asStateFlow()

    private var isLocating = false
    private var locationFailed = false
    private var lastUpdatedAt = 0L
    private var loadJob: Job? = null

    init {
        if (startWithLocation) onMyLocationRequested() else load(city, showLoading = true)
    }

    fun onCitySelected(city: City) {
        savedStateHandle[KEY_SELECTED_CITY_ID] = city.id
        val state = _uiState.value
        val alreadyShown = state is WeatherUiState.Loading || state is WeatherUiState.Success
        if (city.id == this.city.id && !isLocating && !locationFailed && alreadyShown) return
        load(city, showLoading = true)
    }

    /** Konum izni alındıktan sonra çağrılmalı. */
    fun onMyLocationRequested() {
        if (isLocating) return
        savedStateHandle[KEY_SELECTED_CITY_ID] = null
        loadJob?.cancel()
        isLocating = true
        loadJob = viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading(cityName = null)
            val result = locationRepository.getCurrentCity()
            isLocating = false
            result
                .onSuccess { city -> fetch(city, forceRefresh = true) }
                .onFailure { error ->
                    locationFailed = true
                    _uiState.value = WeatherUiState.Error(cityName = null, type = error.toErrorType())
                }
        }
    }

    fun retry() {
        if (_uiState.value is WeatherUiState.Loading) return
        if (locationFailed) onMyLocationRequested() else load(city, showLoading = true, forceRefresh = true)
    }

    /** Gün doğumu/batımı geçişlerini kaçırmamak için ekrana dönüldüğünde çağrılır. */
    fun refreshIfStale() {
        if (_uiState.value is WeatherUiState.Loading) return
        if (SystemClock.elapsedRealtime() - lastUpdatedAt < STALE_AFTER_MS) return
        if (locationFailed) return
        load(city, showLoading = _uiState.value !is WeatherUiState.Success, forceRefresh = true)
    }

    private fun load(city: City, showLoading: Boolean, forceRefresh: Boolean = false) {
        loadJob?.cancel()
        isLocating = false
        loadJob = viewModelScope.launch {
            if (showLoading) _uiState.value = WeatherUiState.Loading(city.name)
            fetch(city, forceRefresh)
        }
    }

    private suspend fun fetch(city: City, forceRefresh: Boolean) {
        this.city = city
        locationFailed = false
        repository.getWeather(city, forceRefresh = forceRefresh)
            .onSuccess { report ->
                lastUpdatedAt = SystemClock.elapsedRealtime()
                _uiState.value = WeatherUiState.Success(
                    report = report,
                    fetchedAtMillis = System.currentTimeMillis(),
                )
                _scene.value = report.current.toScene()
            }
            .onFailure { error ->
                _uiState.value = WeatherUiState.Error(city.name, error.toErrorType())
            }
    }

    companion object {
        private const val STALE_AFTER_MS = 15 * 60 * 1000L
        private const val KEY_CITY = "city"
        private const val KEY_SELECTED_CITY_ID = "selectedCityId"
        private val gson = Gson()

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                WeatherViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    locationRepository = LocationRepositoryImpl(application),
                    hasLocationPermission = application.hasLocationPermission(),
                )
            }
        }
    }
}
