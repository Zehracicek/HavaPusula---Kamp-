package com.example.hava.ui.screens.city

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.hava.data.local.AppPreferencesStore
import com.example.hava.data.local.TurkishPlates
import com.example.hava.data.local.toSearchKey
import com.example.hava.data.model.Country
import com.example.hava.data.model.CurrentConditions
import com.example.hava.data.remote.dto.City
import com.example.hava.data.repository.CityRepository
import com.example.hava.data.repository.CityRepositoryImpl
import com.example.hava.data.repository.WeatherRepository
import com.example.hava.data.repository.WeatherRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(SavedStateHandleSaveableApi::class)
class CityViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val preferences: AppPreferencesStore,
    private val cityRepository: CityRepository = CityRepositoryImpl(),
    private val weatherRepository: WeatherRepository = WeatherRepositoryImpl(),
) : ViewModel() {

    // TextField'ın imleç kaymadan çalışması için StateFlow değil senkron Compose state'i
    var query by savedStateHandle.saveable { mutableStateOf("") }
        private set

    private val countries = cityRepository.getCountries()
    private var selectedCountry = savedStateHandle.get<String>(KEY_COUNTRY)
        ?.let { code -> countries.firstOrNull { it.code == code } }
        ?: countries.first()
    private var countryCities = cityRepository.getCities(selectedCountry.code)

    private val _uiState = MutableStateFlow(
        CityUiState(countries = countries, selectedCountry = selectedCountry, cities = filterCities())
    )
    val uiState: StateFlow<CityUiState> = _uiState.asStateFlow()

    private val _favoriteIds = MutableStateFlow(preferences.favoriteCities().map { it.id }.toSet())
    val favoriteIds: StateFlow<Set<Long>> = _favoriteIds.asStateFlow()

    private val conditionsCache = mutableMapOf<String, CachedConditions>()
    private var conditionsJob: Job? = null
    private var searchJob: Job? = null
    private var lastSearchedQuery: String? = null

    init {
        loadConditions(force = false)
        search(query.trim(), debounce = false)
    }

    fun onCountrySelected(country: Country) {
        if (country.code == selectedCountry.code) return
        selectedCountry = country
        savedStateHandle[KEY_COUNTRY] = country.code
        countryCities = cityRepository.getCities(country.code)
        _uiState.update { it.copy(selectedCountry = country, cities = filterCities()) }
        search(query.trim(), debounce = false)
        loadConditions(force = false)
    }

    fun toggleFavorite(city: City) {
        val favorite = city.id !in _favoriteIds.value
        preferences.setFavorite(city, favorite)
        _favoriteIds.value = preferences.favoriteCities().map { it.id }.toSet()
    }

    fun onQueryChange(newQuery: String) {
        query = newQuery
        _uiState.update { it.copy(cities = filterCities()) }
        val trimmed = newQuery.trim()
        if (trimmed == lastSearchedQuery) return
        search(trimmed, debounce = true)
    }

    fun retrySearch() {
        if (_uiState.value.searchResults is SearchResults.Loading) return
        lastSearchedQuery?.let { search(it, debounce = false) }
    }

    fun retryConditions() {
        if (_uiState.value.conditions is ConditionsState.Loading) return
        loadConditions(force = true)
    }

    /** Listedeki sıcaklıklar eskidiyse ekrana dönüldüğünde yenilenir. */
    fun refreshIfStale() {
        if (_uiState.value.conditions is ConditionsState.Loading) return
        loadConditions(force = false)
    }

    private fun search(query: String, debounce: Boolean) {
        searchJob?.cancel()
        lastSearchedQuery = query

        // Geocoding API 2 karakterden kısa sorgularda sonuç döndürmüyor; plaka aramaları yereldir
        if (query.length < MIN_REMOTE_QUERY_LENGTH || query.all { it.isDigit() }) {
            _uiState.update { it.copy(searchResults = SearchResults.Idle) }
            return
        }

        val countryCode = selectedCountry.code
        searchJob = viewModelScope.launch {
            if (debounce) delay(SEARCH_DEBOUNCE_MS)
            _uiState.update { it.copy(searchResults = SearchResults.Loading) }

            val results = cityRepository.searchCities(query, countryCode).fold(
                onSuccess = { cities -> SearchResults.Success(cities.withoutListedCities()) },
                onFailure = { error -> SearchResults.Error(error.toErrorType()) },
            )
            _uiState.update { it.copy(searchResults = results) }
        }
    }

    private fun loadConditions(force: Boolean) {
        val countryCode = selectedCountry.code
        val cached = conditionsCache[countryCode]
        val isFresh = cached != null && SystemClock.elapsedRealtime() - cached.loadedAt < STALE_AFTER_MS

        conditionsJob?.cancel()
        if (cached != null) {
            _uiState.update { it.copy(conditions = ConditionsState.Success(cached.byCityId)) }
            if (isFresh && !force) return
        } else {
            _uiState.update { it.copy(conditions = ConditionsState.Loading) }
        }

        val cities = countryCities
        conditionsJob = viewModelScope.launch {
            weatherRepository.getCurrentConditions(cities)
                .onSuccess { byCityId ->
                    conditionsCache[countryCode] = CachedConditions(byCityId, SystemClock.elapsedRealtime())
                    _uiState.update { it.copy(conditions = ConditionsState.Success(byCityId)) }
                }
                .onFailure { error ->
                    // Eski sıcaklıklar gösteriliyorsa hata yerine onlar kalır
                    if (cached == null) {
                        _uiState.update { it.copy(conditions = ConditionsState.Error(error.toErrorType())) }
                    }
                }
        }
    }

    private fun filterCities(): List<City> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return countryCities
        val key = trimmed.toSearchKey()
        val plate = trimmed.takeIf { it.all(Char::isDigit) }?.toIntOrNull()
        return countryCities.filter { city ->
            if (plate != null) {
                TurkishPlates.plateFor(city)?.toInt() == plate
            } else {
                city.name.toSearchKey().contains(key)
            }
        }
    }

    // API sonuçlarında listede zaten bulunan şehirler tekrar gösterilmez
    private fun List<City>.withoutListedCities(): List<City> = filterNot { result ->
        val key = result.name.toSearchKey()
        countryCities.any { listed ->
            listed.name.toSearchKey() == key &&
                abs(listed.latitude - result.latitude) < SAME_PLACE_DEGREES &&
                abs(listed.longitude - result.longitude) < SAME_PLACE_DEGREES
        }
    }

    private class CachedConditions(val byCityId: Map<Long, CurrentConditions>, val loadedAt: Long)

    companion object {
        private const val MIN_REMOTE_QUERY_LENGTH = 2
        private const val SEARCH_DEBOUNCE_MS = 400L
        private const val STALE_AFTER_MS = 15 * 60 * 1000L
        private const val SAME_PLACE_DEGREES = 0.5
        private const val KEY_COUNTRY = "countryCode"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                CityViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    preferences = AppPreferencesStore(application),
                )
            }
        }
    }
}
