package com.example.hava.planb

/**
 * İlk sürüm eşikleri başlangıç varsayımıdır.
 * Bilimsel olarak doğrulanmış sınırlar değildir; aktiviteleri birbiriyle karşılaştırmak içindir.
 *
 * Birimler: sıcaklık °C, rüzgâr ve hamle km/h (Open-Meteo varsayılanı), yağış mm, olasılık %, UV indeksi.
 * Ağırlıklar her aktivitede 1.0'a toplanır; eksik isteğe bağlı kriterde kalanlar yeniden normalize edilir.
 */
object ActivityThresholds {

    const val SUITABLE_AT = 75
    const val CAUTION_AT = 45

    /** Güvenlik kuralı puanı bu tavanın üstüne çıkamaz. */
    const val SAFETY_SCORE_CAP = 20

    /** Şiddetli yağmur, yoğun kar, kuvvetli sağanak, gök gürültüsü ve dolu. */
    val severeWeatherCodes = setOf(65, 67, 75, 82, 95, 96, 99)

    const val SEVERE_WIND_KMH = 50.0
    const val SEVERE_GUST_KMH = 70.0

    /** Alternatif aktivitenin önerilmesi için gereken puan farkı. */
    const val ALTERNATIVE_MARGIN = 10

    fun specs(activity: ActivityType): List<CriterionSpec> = when (activity) {
        ActivityType.PICNIC -> listOf(
            CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.35, critical = true),
            CriterionSpec(FactorKind.TEMPERATURE, 0.22, critical = true),
            CriterionSpec(FactorKind.WIND_SPEED, 0.18, critical = true),
            CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.10, critical = false),
            CriterionSpec(FactorKind.WIND_GUSTS, 0.10, critical = false),
            CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
        )
        ActivityType.WALK -> listOf(
            CriterionSpec(FactorKind.TEMPERATURE, 0.25, critical = true),
            CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.30, critical = true),
            CriterionSpec(FactorKind.WIND_SPEED, 0.20, critical = true),
            CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.10, critical = false),
            CriterionSpec(FactorKind.WIND_GUSTS, 0.10, critical = false),
            CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
        )
        ActivityType.RUN -> listOf(
            CriterionSpec(FactorKind.TEMPERATURE, 0.35, critical = true),
            CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.25, critical = true),
            CriterionSpec(FactorKind.WIND_SPEED, 0.20, critical = true),
            CriterionSpec(FactorKind.UV_INDEX, 0.10, critical = false),
            CriterionSpec(FactorKind.WIND_GUSTS, 0.05, critical = false),
            CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
        )
        ActivityType.BIKE -> listOf(
            CriterionSpec(FactorKind.WIND_SPEED, 0.25, critical = true),
            CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.25, critical = true),
            CriterionSpec(FactorKind.TEMPERATURE, 0.20, critical = true),
            CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.15, critical = false),
            CriterionSpec(FactorKind.WIND_GUSTS, 0.10, critical = false),
            CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
        )
        ActivityType.BEACH -> listOf(
            CriterionSpec(FactorKind.TEMPERATURE, 0.25, critical = true),
            CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.20, critical = true),
            CriterionSpec(FactorKind.WIND_SPEED, 0.20, critical = true),
            CriterionSpec(FactorKind.UV_INDEX, 0.20, critical = false),
            CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.10, critical = false),
            CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
        )
    }

    /** API'de olmayan ölçümler. Puanı etkilemez; sonuçta açıkça yazılır. */
    fun unavailable(activity: ActivityType): List<FactorKind> = when (activity) {
        ActivityType.WALK, ActivityType.RUN -> listOf(FactorKind.AIR_QUALITY)
        ActivityType.BEACH -> listOf(FactorKind.WAVE_HEIGHT)
        ActivityType.PICNIC, ActivityType.BIKE -> emptyList()
    }

    fun comfort(activity: ActivityType): ComfortBand = when (activity) {
        ActivityType.PICNIC -> ComfortBand(hardLow = 8.0, idealLow = 18.0, idealHigh = 26.0, hardHigh = 34.0)
        ActivityType.WALK -> ComfortBand(hardLow = -2.0, idealLow = 10.0, idealHigh = 22.0, hardHigh = 32.0)
        ActivityType.RUN -> ComfortBand(hardLow = -5.0, idealLow = 8.0, idealHigh = 16.0, hardHigh = 28.0)
        ActivityType.BIKE -> ComfortBand(hardLow = 0.0, idealLow = 12.0, idealHigh = 24.0, hardHigh = 34.0)
        ActivityType.BEACH -> ComfortBand(hardLow = 18.0, idealLow = 26.0, idealHigh = 32.0, hardHigh = 38.0)
    }

    fun wind(activity: ActivityType): DeclineBand = when (activity) {
        ActivityType.PICNIC -> DeclineBand(goodUntil = 12.0, badAt = 35.0)
        ActivityType.WALK -> DeclineBand(goodUntil = 25.0, badAt = 50.0)
        ActivityType.RUN -> DeclineBand(goodUntil = 18.0, badAt = 40.0)
        ActivityType.BIKE -> DeclineBand(goodUntil = 12.0, badAt = 28.0)
        ActivityType.BEACH -> DeclineBand(goodUntil = 18.0, badAt = 35.0)
    }

    fun gust(activity: ActivityType): DeclineBand = when (activity) {
        ActivityType.PICNIC -> DeclineBand(goodUntil = 20.0, badAt = 45.0)
        ActivityType.WALK -> DeclineBand(goodUntil = 30.0, badAt = 55.0)
        ActivityType.RUN -> DeclineBand(goodUntil = 25.0, badAt = 50.0)
        ActivityType.BIKE -> DeclineBand(goodUntil = 20.0, badAt = 40.0)
        ActivityType.BEACH -> DeclineBand(goodUntil = 25.0, badAt = 45.0)
    }

    /** Olasılığın 0 puana indiği yüzde. */
    fun precipitationProbabilityBadAt(activity: ActivityType): Int = when (activity) {
        ActivityType.PICNIC -> 60
        ActivityType.WALK -> 80
        ActivityType.RUN -> 70
        ActivityType.BIKE -> 50
        ActivityType.BEACH -> 50
    }

    /** Yağış miktarının 0 puana indiği mm. */
    fun precipitationAmountBadAt(activity: ActivityType): Double = when (activity) {
        ActivityType.PICNIC -> 1.5
        ActivityType.WALK -> 3.0
        ActivityType.RUN -> 2.0
        ActivityType.BIKE -> 1.0
        ActivityType.BEACH -> 1.0
    }

    fun uv(activity: ActivityType): DeclineBand? = when (activity) {
        ActivityType.RUN -> DeclineBand(goodUntil = 5.0, badAt = 10.0)
        ActivityType.BEACH -> DeclineBand(goodUntil = 7.0, badAt = 11.0)
        else -> null
    }

    fun indoorFor(activity: ActivityType): IndoorSuggestion = when (activity) {
        ActivityType.PICNIC -> IndoorSuggestion.CAFE
        ActivityType.WALK -> IndoorSuggestion.INDOOR_WALK
        ActivityType.RUN -> IndoorSuggestion.GYM_RUN
        ActivityType.BIKE -> IndoorSuggestion.INDOOR_BIKE
        ActivityType.BEACH -> IndoorSuggestion.INDOOR_POOL
    }

    fun alternativesFor(activity: ActivityType): List<ActivityType> = when (activity) {
        ActivityType.PICNIC -> listOf(ActivityType.WALK)
        ActivityType.WALK -> listOf(ActivityType.PICNIC)
        ActivityType.RUN -> listOf(ActivityType.WALK)
        ActivityType.BIKE -> listOf(ActivityType.WALK)
        ActivityType.BEACH -> listOf(ActivityType.WALK, ActivityType.PICNIC)
    }
}

data class CriterionSpec(
    val kind: FactorKind,
    val weight: Double,
    val critical: Boolean,
)

data class ComfortBand(
    val hardLow: Double,
    val idealLow: Double,
    val idealHigh: Double,
    val hardHigh: Double,
)

data class DeclineBand(
    val goodUntil: Double,
    val badAt: Double,
)

fun suitabilityLevel(score: Int): SuitabilityLevel = when {
    score >= ActivityThresholds.SUITABLE_AT -> SuitabilityLevel.SUITABLE
    score >= ActivityThresholds.CAUTION_AT -> SuitabilityLevel.CAUTION
    else -> SuitabilityLevel.UNSUITABLE
}
