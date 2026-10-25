package com.example.hava.ui.screens.city

import com.example.hava.data.model.Country
import com.example.hava.data.model.CurrentConditions
import com.example.hava.data.remote.dto.City

data class CityUiState(
    val countries: List<Country>,
    val selectedCountry: Country,
    /** Ülkenin uygulamayla gelen şehirleri; arama varsa filtrelenmiş, her durumda alfabetik */
    val cities: List<City>,
    /** Listede olmayan yerler için API araması */
    val searchResults: SearchResults = SearchResults.Idle,
    val conditions: ConditionsState = ConditionsState.Loading,
) {
    val isEmpty: Boolean
        get() = cities.isEmpty() &&
            (searchResults == SearchResults.Idle ||
                (searchResults as? SearchResults.Success)?.cities?.isEmpty() == true)
}

sealed interface SearchResults {

    /** Sorgu API araması için çok kısa */
    data object Idle : SearchResults

    data object Loading : SearchResults

    data class Success(val cities: List<City>) : SearchResults

    data class Error(val type: ErrorType) : SearchResults
}

sealed interface ConditionsState {

    data object Loading : ConditionsState

    /** Anahtar şehir kimliği */
    data class Success(val byCityId: Map<Long, CurrentConditions>) : ConditionsState

    data class Error(val type: ErrorType) : ConditionsState
}

enum class ErrorType { NETWORK, TIMEOUT, SERVER, LOCATION, UNKNOWN }
