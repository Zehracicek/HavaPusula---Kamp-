package com.example.hava.data.remote.dto

import com.google.gson.annotations.SerializedName

// Gson constructor çağırmadığı için tüm alanlar nullable tutuluyor
data class WeatherResponse(
    @SerializedName("current") val current: CurrentWeatherDto? = null,
    @SerializedName("hourly") val hourly: HourlyWeatherDto? = null,
    @SerializedName("daily") val daily: DailyWeatherDto? = null,
)

data class CurrentWeatherDto(
    // Yerel saat, "yyyy-MM-ddTHH:mm" biçiminde (timezone=auto)
    @SerializedName("time") val time: String? = null,
    @SerializedName("temperature_2m") val temperature: Double? = null,
    @SerializedName("apparent_temperature") val apparentTemperature: Double? = null,
    @SerializedName("relative_humidity_2m") val humidity: Int? = null,
    // mm
    @SerializedName("precipitation") val precipitation: Double? = null,
    @SerializedName("weather_code") val weatherCode: Int? = null,
    // %
    @SerializedName("cloud_cover") val cloudCover: Int? = null,
    // Deniz seviyesine indirgenmiş, hPa
    @SerializedName("pressure_msl") val pressure: Double? = null,
    // km/h
    @SerializedName("wind_speed_10m") val windSpeed: Double? = null,
    // Rüzgârın estiği yön, derece (0 = kuzeyden)
    @SerializedName("wind_direction_10m") val windDirection: Int? = null,
    @SerializedName("wind_gusts_10m") val windGusts: Double? = null,
    @SerializedName("is_day") val isDay: Int? = null,
    // Metre
    @SerializedName("visibility") val visibility: Double? = null,
    @SerializedName("uv_index") val uvIndex: Double? = null,
)

// Dizideki her indeks aynı saate karşılık gelir
data class HourlyWeatherDto(
    @SerializedName("time") val time: List<String>? = null,
    @SerializedName("temperature_2m") val temperature: List<Double?>? = null,
    @SerializedName("apparent_temperature") val apparentTemperature: List<Double?>? = null,
    @SerializedName("weather_code") val weatherCode: List<Int?>? = null,
    @SerializedName("is_day") val isDay: List<Int?>? = null,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Int?>? = null,
    /** mm */
    @SerializedName("precipitation") val precipitation: List<Double?>? = null,
    /** km/h */
    @SerializedName("wind_speed_10m") val windSpeed: List<Double?>? = null,
    @SerializedName("wind_direction_10m") val windDirection: List<Int?>? = null,
    /** km/h */
    @SerializedName("wind_gusts_10m") val windGusts: List<Double?>? = null,
    @SerializedName("uv_index") val uvIndex: List<Double?>? = null,
)

data class DailyWeatherDto(
    // "yyyy-MM-dd"
    @SerializedName("time") val time: List<String>? = null,
    @SerializedName("weather_code") val weatherCode: List<Int?>? = null,
    @SerializedName("temperature_2m_max") val maxTemperature: List<Double?>? = null,
    @SerializedName("temperature_2m_min") val minTemperature: List<Double?>? = null,
    @SerializedName("sunrise") val sunrise: List<String>? = null,
    @SerializedName("sunset") val sunset: List<String>? = null,
    @SerializedName("moonrise") val moonrise: List<String>? = null,
    @SerializedName("moonset") val moonset: List<String>? = null,
    /** 0 yeni ay, 0.5 dolunay */
    @SerializedName("moon_phase") val moonPhase: List<Double?>? = null,
    @SerializedName("precipitation_sum") val precipitationSum: List<Double?>? = null,
    @SerializedName("precipitation_probability_max") val precipitationProbability: List<Int?>? = null,
    @SerializedName("wind_speed_10m_max") val maxWindSpeed: List<Double?>? = null,
    @SerializedName("wind_gusts_10m_max") val maxWindGusts: List<Double?>? = null,
    @SerializedName("wind_direction_10m_dominant") val dominantWindDirection: List<Int?>? = null,
    @SerializedName("uv_index_max") val maxUvIndex: List<Double?>? = null,
)
