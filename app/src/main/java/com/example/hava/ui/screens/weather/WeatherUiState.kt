package com.example.hava.ui.screens.weather

import com.example.hava.data.model.WeatherReport
import com.example.hava.ui.screens.city.ErrorType

// cityName null ise konum henüz şehre çevrilmemiştir
sealed interface WeatherUiState {

    data class Loading(val cityName: String?) : WeatherUiState

    data class Success(
        val report: WeatherReport,
        val fetchedAtMillis: Long = 0L,
    ) : WeatherUiState

    data class Error(val cityName: String?, val type: ErrorType) : WeatherUiState
}
