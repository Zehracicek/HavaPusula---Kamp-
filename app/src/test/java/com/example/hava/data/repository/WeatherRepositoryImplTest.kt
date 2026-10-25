package com.example.hava.data.repository

import com.example.hava.data.model.DayPhase
import com.example.hava.data.model.WeatherCondition
import com.example.hava.data.remote.api.CityApi
import com.example.hava.data.remote.api.WeatherApi
import com.example.hava.data.remote.dto.City
import com.example.hava.data.remote.dto.CitySearchResponse
import com.example.hava.data.remote.dto.WeatherResponse
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class WeatherRepositoryImplTest {

    private val city = City(id = 1, name = "Ankara", latitude = 39.92, longitude = 32.85)

    @Test
    fun `başarılı yanıt alan modellerine çevrilir`() = runBlocking {
        val response = Gson().fromJson(FORECAST_JSON, WeatherResponse::class.java)
        val repository = WeatherRepositoryImpl(FakeWeatherApi { response })

        val report = repository.getWeather(city).getOrThrow()

        assertEquals("Ankara", report.current.cityName)
        assertEquals("2026-10-25T14:15", report.observedAt)
        assertEquals(12.4, report.current.temperature, 0.0)
        assertEquals(10.1, report.current.apparentTemperature, 0.0)
        assertEquals(60, report.current.humidity)
        assertEquals(WeatherCondition.RAIN, report.current.condition)
        assertEquals(DayPhase.DAY, report.current.dayPhase)
        // Güncel saatten önceki saatlik veri atlanır
        assertEquals(listOf("2026-10-25T14:00", "2026-10-25T15:00"), report.hourly.map { it.time })
        assertEquals(1, report.daily.size)
        assertEquals(15.0, report.daily.first().maxTemperature, 0.0)
    }

    @Test
    fun `saatlik tahmin günlere ayrılır`() = runBlocking {
        val json = FORECAST_JSON.replace(
            "\"time\": [\"2026-10-25T13:00\", \"2026-10-25T14:00\", \"2026-10-25T15:00\"]",
            "\"time\": [\"2026-10-25T13:00\", \"2026-10-25T14:00\", \"2026-10-26T15:00\"]",
        )
        val response = Gson().fromJson(json, WeatherResponse::class.java)
        val report = WeatherRepositoryImpl(FakeWeatherApi { response }).getWeather(city).getOrThrow()

        assertEquals(listOf("2026-10-25", "2026-10-26"), report.hourlyByDate.keys.toList())
        assertEquals(listOf("2026-10-25T14:00"), report.hourlyByDate.getValue("2026-10-25").map { it.time })
        assertEquals(listOf("2026-10-26T15:00"), report.hourlyByDate.getValue("2026-10-26").map { it.time })
    }

    @Test
    fun `ağ hatası failure olarak döner`() = runBlocking {
        val repository = WeatherRepositoryImpl(FakeWeatherApi { throw IOException("offline") })

        val result = repository.getWeather(city)

        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `güncel veri olmayan yanıt failure olarak döner`() = runBlocking {
        val repository = WeatherRepositoryImpl(FakeWeatherApi { WeatherResponse() })

        assertTrue(repository.getWeather(city).isFailure)
    }

    @Test(expected = CancellationException::class)
    fun `iptal yutulmaz`(): Unit = runBlocking {
        WeatherRepositoryImpl(FakeWeatherApi { throw CancellationException() }).getWeather(city)
    }

    @Test
    fun `sonuç alanı olmayan arama boş liste döner`() = runBlocking {
        val response = Gson().fromJson("""{"generationtime_ms":0.5}""", CitySearchResponse::class.java)
        val repository = CityRepositoryImpl(FakeCityApi { response })

        assertEquals(emptyList<City>(), repository.searchCities("xyz", countryCode = "TR").getOrThrow())
    }

    @Test
    fun `arama sonucu City modeline çevrilir`() = runBlocking {
        val json = """{"results":[{"id":323786,"name":"Ankara","latitude":39.91987,
            "longitude":32.85427,"country":"Türkiye","country_code":"TR","admin1":"Ankara"}]}"""
        val response = Gson().fromJson(json, CitySearchResponse::class.java)
        val repository = CityRepositoryImpl(FakeCityApi { response })

        val city = repository.searchCities("ank", countryCode = "TR").getOrThrow().single()

        assertEquals(323786L, city.id)
        assertEquals("TR", city.countryCode)
        assertEquals("Ankara", city.region)
    }

    @Test
    fun `yeni rüzgar ve ayrıntı alanları modele aktarılır`() = runBlocking {
        val response = Gson().fromJson(FORECAST_JSON, WeatherResponse::class.java)
        val report = WeatherRepositoryImpl(FakeWeatherApi { response }).getWeather(city).getOrThrow()

        assertEquals(315, report.current.windDirection)
        assertEquals(24.5, report.current.windGusts!!, 0.0)
        assertEquals(1013.2, report.current.pressure!!, 0.0)
        assertEquals(40, report.hourly.first().precipitationProbability)
        assertEquals(70, report.daily.first().precipitationProbability)
        assertEquals(31.0, report.daily.first().maxWindGusts!!, 0.0)
        assertEquals("2026-10-25T07:20", report.daily.first().sunrise)
        assertEquals(0.47, report.daily.first().moonPhase!!, 0.0)
        assertEquals("2026-10-25T18:10", report.moon?.nextSet)
        assertEquals(39.92, report.latitude, 0.0)
    }

    @Test
    fun `toplu güncel durum şehir kimliklerine eşlenir`() = runBlocking {
        val istanbul = City(id = 34, name = "İstanbul", latitude = 41.01, longitude = 28.95)
        var requestedLatitudes = ""
        val api = FakeWeatherApi(
            batch = { latitudes ->
                requestedLatitudes = latitudes
                Gson().fromJson(BATCH_JSON, Array<WeatherResponse>::class.java).toList()
            },
        )

        val conditions = WeatherRepositoryImpl(api).getCurrentConditions(listOf(city, istanbul)).getOrThrow()

        assertEquals("39.92,41.01", requestedLatitudes)
        assertEquals(20.3, conditions.getValue(1).temperature, 0.0)
        assertEquals(WeatherCondition.CLOUDY, conditions.getValue(34).condition)
    }

    @Test
    fun `tek şehirlik toplu istekte dizi yanıtı için konum tekrarlanır`() = runBlocking {
        var requestedLatitudes = ""
        val api = FakeWeatherApi(
            batch = { latitudes ->
                requestedLatitudes = latitudes
                Gson().fromJson(BATCH_JSON, Array<WeatherResponse>::class.java).toList()
            },
        )

        val conditions = WeatherRepositoryImpl(api).getCurrentConditions(listOf(city)).getOrThrow()

        assertEquals("39.92,39.92", requestedLatitudes)
        assertEquals(setOf(1L), conditions.keys)
    }

    @Test
    fun `arama seçili ülkeyle sınırlandırılır`() = runBlocking {
        var requestedCountry: String? = null
        val repository = CityRepositoryImpl(FakeCityApi { countryCode ->
            requestedCountry = countryCode
            CitySearchResponse()
        })

        repository.searchCities("par", countryCode = "FR")

        assertEquals("FR", requestedCountry)
    }

    @Test
    fun `aynı şehir kısa sürede yeniden indirilmez`() = runBlocking {
        var calls = 0
        var now = 1_000L
        val response = Gson().fromJson(FORECAST_JSON, WeatherResponse::class.java)
        val repository = WeatherRepositoryImpl(
            FakeWeatherApi {
                calls += 1
                response
            },
            nowMillis = { now },
        )

        repository.getWeather(city).getOrThrow()
        repository.getWeather(city).getOrThrow()
        assertEquals(1, calls)

        now += 15 * 60 * 1000L + 1
        repository.getWeather(city).getOrThrow()
        assertEquals(2, calls)
    }

    @Test
    fun `zorunlu yenileme önbelleği atlar`() = runBlocking {
        var calls = 0
        val response = Gson().fromJson(FORECAST_JSON, WeatherResponse::class.java)
        val repository = WeatherRepositoryImpl(
            FakeWeatherApi {
                calls += 1
                response
            },
            nowMillis = { 0L },
        )

        repository.getWeather(city).getOrThrow()
        repository.getWeather(city, forceRefresh = true).getOrThrow()
        assertEquals(2, calls)
    }

    @Test
    fun `saatlik ek alanlar eşlenir`() = runBlocking {
        val json = FORECAST_JSON.replace(
            "\"precipitation_probability\": [20, 40, 55]",
            "\"precipitation_probability\": [20, 40, 55], \"apparent_temperature\": [11.0, 11.2, 11.4], \"precipitation\": [0.0, 0.2, 0.4], \"wind_speed_10m\": [5.0, 6.0, 7.0], \"wind_direction_10m\": [10, 20, 30], \"wind_gusts_10m\": [9.0, 10.0, 11.0], \"uv_index\": [1.0, 1.5, 2.0]",
        )
        val response = Gson().fromJson(json, WeatherResponse::class.java)
        val report = WeatherRepositoryImpl(FakeWeatherApi { response }).getWeather(city).getOrThrow()
        val hour = report.hourly.first { it.time == "2026-10-25T14:00" }

        assertEquals(11.2, hour.apparentTemperature!!, 0.0)
        assertEquals(0.2, hour.precipitation!!, 0.0)
        assertEquals(6.0, hour.windSpeed!!, 0.0)
        assertEquals(20, hour.windDirection)
        assertEquals(10.0, hour.windGusts!!, 0.0)
        assertEquals(1.5, hour.uvIndex!!, 0.0)
    }

    private class FakeWeatherApi(
        private val batch: (latitudes: String) -> List<WeatherResponse> = { emptyList() },
        private val answer: () -> WeatherResponse = { WeatherResponse() },
    ) : WeatherApi {
        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            current: String,
            hourly: String,
            daily: String,
            timezone: String,
            forecastDays: Int,
        ): WeatherResponse = answer()

        override suspend fun getCurrentConditions(
            latitudes: String,
            longitudes: String,
            current: String,
            timezone: String,
            forecastDays: Int,
        ): List<WeatherResponse> = batch(latitudes)
    }

    private class FakeCityApi(private val answer: (countryCode: String?) -> CitySearchResponse) : CityApi {
        override suspend fun searchCities(
            name: String,
            countryCode: String?,
            count: Int,
            language: String,
            format: String,
        ): CitySearchResponse = answer(countryCode)
    }

    private companion object {
        val FORECAST_JSON = """
            {
              "current": {
                "time": "2026-10-25T14:15",
                "temperature_2m": 12.4,
                "apparent_temperature": 10.1,
                "relative_humidity_2m": 60,
                "wind_speed_10m": 8.3,
                "wind_direction_10m": 315,
                "wind_gusts_10m": 24.5,
                "pressure_msl": 1013.2,
                "weather_code": 61,
                "is_day": 1
              },
              "hourly": {
                "time": ["2026-10-25T13:00", "2026-10-25T14:00", "2026-10-25T15:00"],
                "temperature_2m": [12.0, 12.4, 12.8],
                "weather_code": [3, 61, 61],
                "is_day": [1, 1, 1],
                "precipitation_probability": [20, 40, 55]
              },
              "daily": {
                "time": ["2026-10-25"],
                "weather_code": [61],
                "temperature_2m_max": [15.0],
                "temperature_2m_min": [8.0],
                "sunrise": ["2026-10-25T07:20"],
                "sunset": ["2026-10-25T18:10"],
                "moonrise": ["2026-10-25T17:11"],
                "moonset": ["2026-10-25T18:10"],
                "moon_phase": [0.47],
                "precipitation_probability_max": [70],
                "wind_gusts_10m_max": [31.0]
              }
            }
        """.trimIndent()

        val BATCH_JSON = """
            [
              {"current": {"time": "2026-10-25T17:30", "temperature_2m": 20.3, "weather_code": 0, "is_day": 1}},
              {"current": {"time": "2026-10-25T17:30", "temperature_2m": 18.2, "weather_code": 3, "is_day": 1}}
            ]
        """.trimIndent()
    }
}
