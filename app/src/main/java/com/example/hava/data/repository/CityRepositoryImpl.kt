package com.example.hava.data.repository

import com.example.hava.data.local.CountryCatalog
import com.example.hava.data.model.Country
import com.example.hava.data.remote.ApiClient
import com.example.hava.data.remote.api.CityApi
import com.example.hava.data.remote.dto.City

class CityRepositoryImpl(
    private val api: CityApi = ApiClient.cityApi,
) : CityRepository {

    override fun getCountries(): List<Country> = CountryCatalog.countries

    override fun getCities(countryCode: String): List<City> = CountryCatalog.citiesOf(countryCode)

    override suspend fun searchCities(query: String, countryCode: String): Result<List<City>> = resultOf {
        api.searchCities(name = query, countryCode = countryCode).results.orEmpty()
    }
}
