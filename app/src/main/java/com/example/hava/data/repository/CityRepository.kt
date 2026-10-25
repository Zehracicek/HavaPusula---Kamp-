package com.example.hava.data.repository

import com.example.hava.data.model.Country
import com.example.hava.data.remote.dto.City

interface CityRepository {

    fun getCountries(): List<Country>

    /** Ülkenin uygulamayla gelen şehirleri, alfabetik sıralı. */
    fun getCities(countryCode: String): List<City>

    /** Listede olmayan ilçe/kasabalar için yalnızca verilen ülkede arama yapar. */
    suspend fun searchCities(query: String, countryCode: String): Result<List<City>>
}
