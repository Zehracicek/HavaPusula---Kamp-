package com.example.hava.data.remote

import com.example.hava.data.remote.api.CityApi
import com.example.hava.data.remote.api.WeatherApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
    private const val FORECAST_BASE_URL = "https://api.open-meteo.com/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private fun retrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val cityApi: CityApi by lazy { retrofit(GEOCODING_BASE_URL).create(CityApi::class.java) }

    val weatherApi: WeatherApi by lazy { retrofit(FORECAST_BASE_URL).create(WeatherApi::class.java) }
}
