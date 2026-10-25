package com.example.hava.data.repository

import com.example.hava.data.model.CurrentConditions
import com.example.hava.data.model.WeatherReport
import com.example.hava.data.remote.dto.City

interface WeatherRepository {

    /**
     * @param forceRefresh true ise 15 dakikalık bellek önbelleği atlanır.
     */
    suspend fun getWeather(city: City, forceRefresh: Boolean = false): Result<WeatherReport>

    /** Şehir listesi için tek istekte güncel özetler; anahtar şehir kimliğidir. */
    suspend fun getCurrentConditions(cities: List<City>): Result<Map<Long, CurrentConditions>>
}
