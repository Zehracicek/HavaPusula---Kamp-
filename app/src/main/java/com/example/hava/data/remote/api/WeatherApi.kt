package com.example.hava.data.remote.api

import com.example.hava.data.remote.dto.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String =
            "temperature_2m,apparent_temperature,relative_humidity_2m,precipitation,weather_code," +
                "cloud_cover,pressure_msl,wind_speed_10m,wind_direction_10m,wind_gusts_10m,is_day," +
                "visibility,uv_index",
        @Query("hourly") hourly: String =
            "temperature_2m,apparent_temperature,weather_code,is_day,precipitation_probability," +
                "precipitation,wind_speed_10m,wind_direction_10m,wind_gusts_10m,uv_index",
        @Query("daily") daily: String =
            "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,moonrise,moonset," +
                "moon_phase,precipitation_sum,precipitation_probability_max,wind_speed_10m_max," +
                "wind_gusts_10m_max,wind_direction_10m_dominant,uv_index_max",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 7,
    ): WeatherResponse

    /**
     * Birden çok konumun güncel durumu tek istekte; yanıt konum sırasıyla dizi olarak gelir.
     * Tek konum gönderilirse API dizi yerine nesne döndürür, bu yüzden en az iki konum gönderilmeli.
     */
    @GET("v1/forecast")
    suspend fun getCurrentConditions(
        /** Virgülle ayrılmış enlemler */
        @Query("latitude") latitudes: String,
        /** Virgülle ayrılmış boylamlar */
        @Query("longitude") longitudes: String,
        @Query("current") current: String = "temperature_2m,weather_code,is_day",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 1,
    ): List<WeatherResponse>
}
