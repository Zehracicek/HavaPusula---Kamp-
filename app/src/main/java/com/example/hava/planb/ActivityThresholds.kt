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

    fun specs(activity: ActivityType): List<CriterionSpec> = profile(activity).specs

    fun profile(activity: ActivityType): ActivityProfile = when (activity) {
        ActivityType.PICNIC -> ActivityProfile(
            activity = activity,
            specs = listOf(
                CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.35, critical = true),
                CriterionSpec(FactorKind.TEMPERATURE, 0.22, critical = true),
                CriterionSpec(FactorKind.WIND_SPEED, 0.18, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.10, critical = false),
                CriterionSpec(FactorKind.WIND_GUSTS, 0.10, critical = false),
                CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
            ),
            comfort = ComfortBand(hardLow = 8.0, idealLow = 18.0, idealHigh = 26.0, hardHigh = 34.0),
            wind = DeclineBand(goodUntil = 12.0, badAt = 35.0),
            gust = DeclineBand(goodUntil = 20.0, badAt = 45.0),
            precipitationProbabilityBadAt = 60,
            precipitationAmountBadAt = 1.5,
            indoor = IndoorSuggestion.CAFE,
            alternatives = listOf(ActivityType.WALK),
        )
        ActivityType.WALK -> ActivityProfile(
            activity = activity,
            specs = listOf(
                CriterionSpec(FactorKind.TEMPERATURE, 0.25, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.30, critical = true),
                CriterionSpec(FactorKind.WIND_SPEED, 0.20, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.10, critical = false),
                CriterionSpec(FactorKind.WIND_GUSTS, 0.10, critical = false),
                CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
            ),
            comfort = ComfortBand(hardLow = -2.0, idealLow = 10.0, idealHigh = 22.0, hardHigh = 32.0),
            wind = DeclineBand(goodUntil = 25.0, badAt = 50.0),
            gust = DeclineBand(goodUntil = 30.0, badAt = 55.0),
            precipitationProbabilityBadAt = 80,
            precipitationAmountBadAt = 3.0,
            unavailable = listOf(FactorKind.AIR_QUALITY),
            indoor = IndoorSuggestion.INDOOR_WALK,
            alternatives = listOf(ActivityType.PICNIC),
        )
        ActivityType.RUN -> ActivityProfile(
            activity = activity,
            specs = listOf(
                CriterionSpec(FactorKind.TEMPERATURE, 0.35, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.25, critical = true),
                CriterionSpec(FactorKind.WIND_SPEED, 0.20, critical = true),
                CriterionSpec(FactorKind.UV_INDEX, 0.10, critical = false),
                CriterionSpec(FactorKind.WIND_GUSTS, 0.05, critical = false),
                CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
            ),
            comfort = ComfortBand(hardLow = -5.0, idealLow = 8.0, idealHigh = 16.0, hardHigh = 28.0),
            wind = DeclineBand(goodUntil = 18.0, badAt = 40.0),
            gust = DeclineBand(goodUntil = 25.0, badAt = 50.0),
            precipitationProbabilityBadAt = 70,
            precipitationAmountBadAt = 2.0,
            uv = DeclineBand(goodUntil = 5.0, badAt = 10.0),
            unavailable = listOf(FactorKind.AIR_QUALITY),
            indoor = IndoorSuggestion.GYM_RUN,
            alternatives = listOf(ActivityType.WALK),
        )
        ActivityType.BIKE -> ActivityProfile(
            activity = activity,
            specs = listOf(
                CriterionSpec(FactorKind.WIND_SPEED, 0.25, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.25, critical = true),
                CriterionSpec(FactorKind.TEMPERATURE, 0.20, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.15, critical = false),
                CriterionSpec(FactorKind.WIND_GUSTS, 0.10, critical = false),
                CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
            ),
            comfort = ComfortBand(hardLow = 0.0, idealLow = 12.0, idealHigh = 24.0, hardHigh = 34.0),
            wind = DeclineBand(goodUntil = 12.0, badAt = 28.0),
            gust = DeclineBand(goodUntil = 20.0, badAt = 40.0),
            precipitationProbabilityBadAt = 50,
            precipitationAmountBadAt = 1.0,
            tracksWindDirection = true,
            indoor = IndoorSuggestion.INDOOR_BIKE,
            alternatives = listOf(ActivityType.WALK),
        )
        ActivityType.BEACH -> ActivityProfile(
            activity = activity,
            specs = listOf(
                CriterionSpec(FactorKind.TEMPERATURE, 0.25, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.20, critical = true),
                CriterionSpec(FactorKind.WIND_SPEED, 0.20, critical = true),
                CriterionSpec(FactorKind.UV_INDEX, 0.20, critical = false),
                CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.10, critical = false),
                CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
            ),
            comfort = ComfortBand(hardLow = 18.0, idealLow = 26.0, idealHigh = 32.0, hardHigh = 38.0),
            wind = DeclineBand(goodUntil = 18.0, badAt = 35.0),
            gust = DeclineBand(goodUntil = 25.0, badAt = 45.0),
            precipitationProbabilityBadAt = 50,
            precipitationAmountBadAt = 1.0,
            uv = DeclineBand(goodUntil = 7.0, badAt = 11.0),
            unavailable = listOf(FactorKind.WAVE_HEIGHT),
            indoor = IndoorSuggestion.INDOOR_POOL,
            alternatives = listOf(ActivityType.WALK, ActivityType.PICNIC),
        )
        ActivityType.CAMPING -> ActivityProfile(
            activity = activity,
            specs = listOf(
                CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.30, critical = true),
                CriterionSpec(FactorKind.TEMPERATURE, 0.22, critical = true),
                CriterionSpec(FactorKind.WIND_SPEED, 0.20, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.13, critical = false),
                CriterionSpec(FactorKind.WIND_GUSTS, 0.10, critical = false),
                CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
            ),
            comfort = ComfortBand(hardLow = 2.0, idealLow = 14.0, idealHigh = 26.0, hardHigh = 34.0),
            wind = DeclineBand(goodUntil = 10.0, badAt = 35.0),
            gust = DeclineBand(goodUntil = 20.0, badAt = 45.0),
            precipitationProbabilityBadAt = 50,
            precipitationAmountBadAt = 1.5,
            indoor = IndoorSuggestion.CABIN,
            alternatives = listOf(ActivityType.PICNIC, ActivityType.HIKING),
        )
        ActivityType.HIKING -> ActivityProfile(
            activity = activity,
            specs = listOf(
                CriterionSpec(FactorKind.TEMPERATURE, 0.25, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.25, critical = true),
                CriterionSpec(FactorKind.WIND_SPEED, 0.20, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.10, critical = false),
                CriterionSpec(FactorKind.WIND_GUSTS, 0.10, critical = false),
                CriterionSpec(FactorKind.UV_INDEX, 0.05, critical = false),
                CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
            ),
            comfort = ComfortBand(hardLow = -2.0, idealLow = 8.0, idealHigh = 20.0, hardHigh = 30.0),
            wind = DeclineBand(goodUntil = 20.0, badAt = 45.0),
            gust = DeclineBand(goodUntil = 30.0, badAt = 55.0),
            precipitationProbabilityBadAt = 70,
            precipitationAmountBadAt = 2.5,
            uv = DeclineBand(goodUntil = 6.0, badAt = 10.0),
            indoor = IndoorSuggestion.INDOOR_WALK,
            alternatives = listOf(ActivityType.WALK),
        )
        ActivityType.YOGA -> ActivityProfile(
            activity = activity,
            specs = listOf(
                CriterionSpec(FactorKind.TEMPERATURE, 0.30, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.30, critical = true),
                CriterionSpec(FactorKind.WIND_SPEED, 0.20, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.10, critical = false),
                CriterionSpec(FactorKind.UV_INDEX, 0.05, critical = false),
                CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
            ),
            comfort = ComfortBand(hardLow = 10.0, idealLow = 18.0, idealHigh = 26.0, hardHigh = 32.0),
            wind = DeclineBand(goodUntil = 8.0, badAt = 25.0),
            gust = DeclineBand(goodUntil = 15.0, badAt = 35.0),
            precipitationProbabilityBadAt = 50,
            precipitationAmountBadAt = 1.0,
            uv = DeclineBand(goodUntil = 5.0, badAt = 9.0),
            indoor = IndoorSuggestion.YOGA_STUDIO,
            alternatives = listOf(ActivityType.WALK, ActivityType.PICNIC),
        )
        ActivityType.TENNIS -> ActivityProfile(
            activity = activity,
            specs = listOf(
                CriterionSpec(FactorKind.WIND_SPEED, 0.30, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.25, critical = true),
                CriterionSpec(FactorKind.TEMPERATURE, 0.20, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.10, critical = false),
                CriterionSpec(FactorKind.WIND_GUSTS, 0.10, critical = false),
                CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
            ),
            comfort = ComfortBand(hardLow = 4.0, idealLow = 14.0, idealHigh = 25.0, hardHigh = 33.0),
            wind = DeclineBand(goodUntil = 8.0, badAt = 25.0),
            gust = DeclineBand(goodUntil = 15.0, badAt = 35.0),
            precipitationProbabilityBadAt = 40,
            precipitationAmountBadAt = 0.5,
            indoor = IndoorSuggestion.INDOOR_COURT,
            alternatives = listOf(ActivityType.RUN, ActivityType.WALK),
        )
        ActivityType.BASKETBALL -> ActivityProfile(
            activity = activity,
            specs = listOf(
                CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.30, critical = true),
                CriterionSpec(FactorKind.TEMPERATURE, 0.25, critical = true),
                CriterionSpec(FactorKind.WIND_SPEED, 0.15, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.15, critical = false),
                CriterionSpec(FactorKind.WIND_GUSTS, 0.10, critical = false),
                CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
            ),
            comfort = ComfortBand(hardLow = 2.0, idealLow = 12.0, idealHigh = 26.0, hardHigh = 34.0),
            wind = DeclineBand(goodUntil = 15.0, badAt = 40.0),
            gust = DeclineBand(goodUntil = 25.0, badAt = 50.0),
            precipitationProbabilityBadAt = 50,
            precipitationAmountBadAt = 1.0,
            indoor = IndoorSuggestion.INDOOR_COURT,
            alternatives = listOf(ActivityType.RUN, ActivityType.WALK),
        )
        ActivityType.VOLLEYBALL -> ActivityProfile(
            activity = activity,
            specs = listOf(
                CriterionSpec(FactorKind.TEMPERATURE, 0.25, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.25, critical = true),
                CriterionSpec(FactorKind.WIND_SPEED, 0.20, critical = true),
                CriterionSpec(FactorKind.UV_INDEX, 0.15, critical = false),
                CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.10, critical = false),
                CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
            ),
            comfort = ComfortBand(hardLow = 14.0, idealLow = 20.0, idealHigh = 30.0, hardHigh = 36.0),
            wind = DeclineBand(goodUntil = 10.0, badAt = 30.0),
            gust = DeclineBand(goodUntil = 20.0, badAt = 40.0),
            precipitationProbabilityBadAt = 50,
            precipitationAmountBadAt = 1.0,
            uv = DeclineBand(goodUntil = 6.0, badAt = 10.0),
            indoor = IndoorSuggestion.INDOOR_COURT,
            alternatives = listOf(ActivityType.BEACH, ActivityType.WALK),
        )
        ActivityType.FOOTBALL -> ActivityProfile(
            activity = activity,
            specs = listOf(
                CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.25, critical = true),
                CriterionSpec(FactorKind.TEMPERATURE, 0.25, critical = true),
                CriterionSpec(FactorKind.WIND_SPEED, 0.15, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.15, critical = false),
                CriterionSpec(FactorKind.WIND_GUSTS, 0.10, critical = false),
                CriterionSpec(FactorKind.WEATHER_CODE, 0.10, critical = false),
            ),
            comfort = ComfortBand(hardLow = 0.0, idealLow = 10.0, idealHigh = 24.0, hardHigh = 32.0),
            wind = DeclineBand(goodUntil = 20.0, badAt = 45.0),
            gust = DeclineBand(goodUntil = 30.0, badAt = 55.0),
            precipitationProbabilityBadAt = 70,
            precipitationAmountBadAt = 2.0,
            indoor = IndoorSuggestion.INDOOR_FOOTBALL,
            alternatives = listOf(ActivityType.RUN),
        )
        ActivityType.FISHING -> ActivityProfile(
            activity = activity,
            specs = listOf(
                CriterionSpec(FactorKind.WIND_SPEED, 0.30, critical = true),
                CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.25, critical = true),
                CriterionSpec(FactorKind.TEMPERATURE, 0.20, critical = true),
                CriterionSpec(FactorKind.WIND_GUSTS, 0.10, critical = false),
                CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.10, critical = false),
                CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false),
            ),
            comfort = ComfortBand(hardLow = 0.0, idealLow = 10.0, idealHigh = 24.0, hardHigh = 32.0),
            wind = DeclineBand(goodUntil = 10.0, badAt = 30.0),
            gust = DeclineBand(goodUntil = 20.0, badAt = 40.0),
            precipitationProbabilityBadAt = 60,
            precipitationAmountBadAt = 2.0,
            unavailable = listOf(FactorKind.WAVE_HEIGHT),
            indoor = IndoorSuggestion.CAFE,
            alternatives = listOf(ActivityType.WALK, ActivityType.PICNIC),
        )
        ActivityType.CUSTOM -> profile(
            CustomActivity(name = "", factors = CustomFactor.entries.toSet()),
        )
    }

    /**
     * Seçilen ölçütler sıkı eşiklerle puanlanır; seçilmeyenler puana katılmaz.
     * Şiddetli hava ve fırtına güvenlik kuralı ölçüt seçiminden bağımsız olarak uygulanır.
     */
    fun profile(custom: CustomActivity): ActivityProfile {
        val factors = custom.factors.ifEmpty { CustomFactor.entries.toSet() }
        val raw = buildList {
            if (CustomFactor.PRECIPITATION in factors) {
                add(CriterionSpec(FactorKind.PRECIPITATION_PROBABILITY, 0.30, critical = true))
                add(CriterionSpec(FactorKind.PRECIPITATION_AMOUNT, 0.10, critical = false))
            }
            if (CustomFactor.WIND in factors) {
                add(CriterionSpec(FactorKind.WIND_SPEED, 0.25, critical = true))
                add(CriterionSpec(FactorKind.WIND_GUSTS, 0.10, critical = false))
            }
            if (CustomFactor.TEMPERATURE in factors) {
                add(CriterionSpec(FactorKind.TEMPERATURE, 0.25, critical = true))
            }
            if (CustomFactor.UV in factors) {
                add(CriterionSpec(FactorKind.UV_INDEX, 0.15, critical = true))
            }
            add(CriterionSpec(FactorKind.WEATHER_CODE, 0.05, critical = false))
        }
        val total = raw.sumOf { it.weight }
        return ActivityProfile(
            activity = ActivityType.CUSTOM,
            specs = raw.map { it.copy(weight = it.weight / total) },
            comfort = when (custom.temperature) {
                TemperaturePreference.COOL -> ComfortBand(hardLow = -5.0, idealLow = 5.0, idealHigh = 16.0, hardHigh = 26.0)
                TemperaturePreference.MILD -> ComfortBand(hardLow = 4.0, idealLow = 14.0, idealHigh = 25.0, hardHigh = 33.0)
                TemperaturePreference.WARM -> ComfortBand(hardLow = 14.0, idealLow = 22.0, idealHigh = 30.0, hardHigh = 37.0)
            },
            wind = DeclineBand(goodUntil = 8.0, badAt = 25.0),
            gust = DeclineBand(goodUntil = 15.0, badAt = 35.0),
            precipitationProbabilityBadAt = 50,
            precipitationAmountBadAt = 1.0,
            uv = DeclineBand(goodUntil = 4.0, badAt = 8.0),
            indoor = IndoorSuggestion.GENERAL,
            alternatives = listOf(ActivityType.WALK),
        )
    }
}

/** Bir aktivitenin puanlanmasında kullanılan bütün eşikler. */
data class ActivityProfile(
    val activity: ActivityType,
    val specs: List<CriterionSpec>,
    val comfort: ComfortBand,
    val wind: DeclineBand,
    val gust: DeclineBand,
    /** Olasılığın 0 puana indiği yüzde. */
    val precipitationProbabilityBadAt: Int,
    /** Yağış miktarının 0 puana indiği mm. */
    val precipitationAmountBadAt: Double,
    val uv: DeclineBand? = null,
    /** API'de olmayan ölçümler. Puanı etkilemez; sonuçta açıkça yazılır. */
    val unavailable: List<FactorKind> = emptyList(),
    /** Rüzgâr yönü gelmezse eksik isteğe bağlı veri olarak bildirilir. */
    val tracksWindDirection: Boolean = false,
    val indoor: IndoorSuggestion,
    val alternatives: List<ActivityType>,
)

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
