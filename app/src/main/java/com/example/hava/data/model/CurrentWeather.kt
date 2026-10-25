package com.example.hava.data.model

data class WeatherReport(
    val current: CurrentWeather,
    /** Mevcut saatten başlayarak sıralı */
    val hourly: List<HourlyForecast>,
    /** Bugünden başlayarak sıralı */
    val daily: List<DailyForecast>,
    /** Gün ("yyyy-MM-dd") → o günün saatlik tahmini; bugün için mevcut saatten başlar */
    val hourlyByDate: Map<String, List<HourlyForecast>> = emptyMap(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val moon: MoonInfo? = null,
    /** API'nin güncel gözlem zamanı, "yyyy-MM-ddTHH:mm" */
    val observedAt: String? = null,
)

// API'nin her zaman göndermediği ölçümler nullable; arayüz bunları "–" olarak gösterir
data class CurrentWeather(
    val cityName: String,
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    /** km/h */
    val windSpeed: Double,
    val weatherCode: Int,
    val condition: WeatherCondition,
    /** Yağmur/kar yoğunluğu, 0..1 */
    val precipitationIntensity: Float,
    val dayPhase: DayPhase,
    /** Rüzgârın estiği yön, derece (0 = kuzeyden) */
    val windDirection: Int? = null,
    /** km/h */
    val windGusts: Double? = null,
    /** Deniz seviyesi basıncı, hPa */
    val pressure: Double? = null,
    /** % */
    val cloudCover: Int? = null,
    /** Metre */
    val visibility: Double? = null,
    val uvIndex: Double? = null,
    /** Son 15 dakikadaki yağış, mm */
    val precipitation: Double? = null,
)

data class HourlyForecast(
    /** Yerel saat, "yyyy-MM-ddTHH:mm" */
    val time: String,
    val temperature: Double,
    val weatherCode: Int,
    val condition: WeatherCondition,
    val isDay: Boolean,
    /** % */
    val precipitationProbability: Int? = null,
    /** mm */
    val precipitation: Double? = null,
    /** km/h */
    val windSpeed: Double? = null,
    /** Derece, rüzgârın estiği yön */
    val windDirection: Int? = null,
    /** km/h */
    val windGusts: Double? = null,
    val uvIndex: Double? = null,
    val apparentTemperature: Double? = null,
)

data class DailyForecast(
    /** "yyyy-MM-dd" */
    val date: String,
    val maxTemperature: Double,
    val minTemperature: Double,
    val weatherCode: Int,
    val condition: WeatherCondition,
    /** % */
    val precipitationProbability: Int? = null,
    /** mm */
    val precipitationSum: Double? = null,
    /** km/h */
    val maxWindSpeed: Double? = null,
    /** km/h */
    val maxWindGusts: Double? = null,
    /** Derece */
    val dominantWindDirection: Int? = null,
    val maxUvIndex: Double? = null,
    /** Yerel saat, "yyyy-MM-ddTHH:mm" */
    val sunrise: String? = null,
    val sunset: String? = null,
    val moonrise: String? = null,
    val moonset: String? = null,
    /** 0 yeni ay, 0.5 dolunay */
    val moonPhase: Double? = null,
)

/** Şehir listesinde gösterilen kısa özet. */
data class CurrentConditions(
    val temperature: Double,
    val weatherCode: Int,
    val condition: WeatherCondition,
    val isDay: Boolean,
)

enum class WeatherCondition { CLEAR, PARTLY_CLOUDY, CLOUDY, FOG, RAIN, STORM, SNOW }

enum class DayPhase { DAY, NIGHT, SUNRISE, SUNSET }
