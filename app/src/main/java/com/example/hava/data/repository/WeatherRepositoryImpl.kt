package com.example.hava.data.repository

import com.example.hava.data.mapper.toCurrentConditions
import com.example.hava.data.mapper.toWeatherReport
import com.example.hava.data.model.CurrentConditions
import com.example.hava.data.model.WeatherReport
import com.example.hava.data.remote.ApiClient
import com.example.hava.data.remote.api.WeatherApi
import com.example.hava.data.remote.dto.City
import java.util.concurrent.ConcurrentHashMap

class WeatherRepositoryImpl(
    private val api: WeatherApi = ApiClient.weatherApi,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : WeatherRepository {

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    override suspend fun getWeather(city: City, forceRefresh: Boolean): Result<WeatherReport> {
        val key = cacheKey(city)
        if (!forceRefresh) {
            cache[key]?.takeIf { nowMillis() - it.storedAtMillis < CACHE_TTL_MS }?.let { return Result.success(it.report) }
        }
        return resultOf {
            val report = api.getForecast(latitude = city.latitude, longitude = city.longitude)
                .toWeatherReport(cityName = city.name, latitude = city.latitude, longitude = city.longitude)
            cache[key] = CacheEntry(report, nowMillis())
            report
        }
    }

    override suspend fun getCurrentConditions(cities: List<City>): Result<Map<Long, CurrentConditions>> =
        resultOf {
            if (cities.isEmpty()) return@resultOf emptyMap()
            // API tek konumda dizi yerine nesne döndürdüğü için konum tekrarlanarak dizi yanıtı zorlanır
            val requested = if (cities.size == 1) cities + cities else cities
            val responses = api.getCurrentConditions(
                latitudes = requested.joinToString(",") { it.latitude.toString() },
                longitudes = requested.joinToString(",") { it.longitude.toString() },
            )
            cities.zip(responses)
                .mapNotNull { (city, response) -> response.toCurrentConditions()?.let { city.id to it } }
                .toMap()
        }

    private fun cacheKey(city: City): String = "${city.id}|${city.latitude}|${city.longitude}"

    private class CacheEntry(val report: WeatherReport, val storedAtMillis: Long)

    private companion object {
        /** WeatherViewModel.STALE_AFTER_MS ile aynı pencere; aynı şehir için ikinci indirmeyi keser. */
        const val CACHE_TTL_MS = 15 * 60 * 1000L
    }
}
