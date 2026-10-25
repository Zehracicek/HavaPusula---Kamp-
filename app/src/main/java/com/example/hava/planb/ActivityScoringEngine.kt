package com.example.hava.planb

import kotlin.math.roundToInt

class ActivityScoringEngine(
    private val thresholds: ActivityThresholds = ActivityThresholds,
) {

    fun scoreHour(activity: ActivityType, hour: HourConditions): HourAssessment =
        scoreHour(thresholds.profile(activity), hour)

    fun scoreWindow(activity: ActivityType, hours: List<HourConditions>): WindowAssessment =
        scoreWindow(thresholds.profile(activity), hours)

    fun scoreHour(profile: ActivityProfile, hour: HourConditions): HourAssessment {
        val snapshot = hour.snapshot()
        val safety = safetyOf(hour)
        val scored = mutableListOf<CriterionAssessment>()
        val missingCritical = mutableListOf<FactorKind>()
        val missingOptional = mutableListOf<FactorKind>()

        for (spec in profile.specs) {
            val assessment = assess(profile, spec, hour)
            if (assessment == null) {
                if (spec.critical) missingCritical += spec.kind else missingOptional += spec.kind
            } else {
                scored += assessment
            }
        }
        if (profile.tracksWindDirection && hour.windDirectionDegrees == null) {
            missingOptional += FactorKind.WIND_DIRECTION
        }

        if (missingCritical.isNotEmpty()) {
            if (safety == null) {
                return HourAssessment.Insufficient(hour.time, missingCritical.distinct(), snapshot)
            }
            return HourAssessment.Scored(
                time = hour.time,
                score = 0,
                level = SuitabilityLevel.UNSUITABLE,
                factors = scored,
                missingOptional = missingOptional.distinct(),
                safetyOverride = safety,
                snapshot = snapshot,
            )
        }

        val weightSum = scored.sumOf { it.weight }
        val raw = if (weightSum == 0.0) 0 else (scored.sumOf { it.score * it.weight } / weightSum).roundToInt()
        val score = if (safety != null) minOf(raw, ActivityThresholds.SAFETY_SCORE_CAP) else raw.coerceIn(0, 100)
        val level = if (safety != null) SuitabilityLevel.UNSUITABLE else suitabilityLevel(score)
        return HourAssessment.Scored(
            time = hour.time,
            score = score,
            level = level,
            factors = scored,
            missingOptional = missingOptional.distinct(),
            safetyOverride = safety,
            snapshot = snapshot,
        )
    }

    fun scoreWindow(profile: ActivityProfile, hours: List<HourConditions>): WindowAssessment {
        val unavailable = profile.unavailable
        if (hours.isEmpty()) {
            return WindowAssessment(
                hours = emptyList(),
                overall = OverallAssessment.Insufficient(emptyList(), InsufficientReason.NO_HOURLY_DATA),
                unavailable = unavailable,
            )
        }
        val assessed = hours.map { scoreHour(profile, it) }
        val scored = assessed.filterIsInstance<HourAssessment.Scored>()
        if (scored.isEmpty()) {
            val missing = assessed.filterIsInstance<HourAssessment.Insufficient>()
                .flatMap { it.missing }
                .distinct()
            return WindowAssessment(
                hours = assessed,
                overall = OverallAssessment.Insufficient(missing, InsufficientReason.MISSING_CRITICAL),
                unavailable = unavailable,
            )
        }

        val safety = scored.any { it.safetyOverride != null }
        val score = if (safety) {
            scored.minOf { it.score }
        } else {
            scored.map { it.score }.average().roundToInt().coerceIn(0, 100)
        }
        val level = if (safety) SuitabilityLevel.UNSUITABLE else suitabilityLevel(score)
        val byKind = scored.flatMap { it.factors }.groupBy { it.kind }
        val positive = mutableListOf<FactorKind>()
        val negative = mutableListOf<FactorKind>()
        var weakest: CriterionAssessment? = null
        byKind.forEach { (kind, factors) ->
            val average = factors.map { it.score }.average()
            when {
                average >= ActivityThresholds.SUITABLE_AT -> positive += kind
                average < 50 -> {
                    negative += kind
                    val worst = factors.minBy { it.score }
                    val currentWeakest = weakest
                    if (currentWeakest == null || worst.score < currentWeakest.score) weakest = worst
                }
            }
        }
        val dominant = scored.mapNotNull { it.safetyOverride?.reason }.firstOrNull() ?: weakest?.reason
        return WindowAssessment(
            hours = assessed,
            overall = OverallAssessment.Scored(
                score = score,
                level = level,
                positive = positive,
                negative = negative,
                missingOptional = scored.flatMap { it.missingOptional }.distinct(),
                safetyOverride = safety,
                dominantReason = if (level == SuitabilityLevel.SUITABLE) null else dominant,
            ),
            unavailable = unavailable,
        )
    }

    private fun assess(profile: ActivityProfile, spec: CriterionSpec, hour: HourConditions): CriterionAssessment? {
        val value = measured(profile, spec.kind, hour) ?: return null
        return CriterionAssessment(
            kind = spec.kind,
            score = value.score,
            weight = spec.weight,
            measured = value.measured,
            unit = value.unit,
            polarity = polarity(value.score),
            reason = value.reason,
            usedApparentTemperature = value.usedApparent,
        )
    }

    private fun measured(profile: ActivityProfile, kind: FactorKind, hour: HourConditions): Measured? = when (kind) {
        FactorKind.PRECIPITATION_PROBABILITY -> hour.precipitationProbabilityPercent?.let { probability ->
            val badAt = profile.precipitationProbabilityBadAt
            Measured(
                score = decliningScore(probability.toDouble(), goodUntil = 0.0, badAt = badAt.toDouble()),
                measured = probability.toDouble(),
                unit = MeasurementUnit.PERCENT,
                reason = ReasonCode.PRECIPITATION,
            )
        }
        FactorKind.PRECIPITATION_AMOUNT -> hour.precipitationMm?.let { mm ->
            Measured(
                score = decliningScore(mm, goodUntil = 0.0, badAt = profile.precipitationAmountBadAt),
                measured = mm,
                unit = MeasurementUnit.MILLIMETER,
                reason = ReasonCode.PRECIPITATION,
            )
        }
        FactorKind.TEMPERATURE -> {
            val apparent = hour.apparentTemperatureC
            val actual = hour.temperatureC
            val used = apparent ?: actual ?: return null
            val band = profile.comfort
            val reason = when {
                used < band.idealLow -> ReasonCode.COLD
                used > band.idealHigh -> ReasonCode.HEAT
                else -> ReasonCode.COMFORTABLE
            }
            Measured(
                score = bandScore(used, band),
                measured = used,
                unit = MeasurementUnit.CELSIUS,
                reason = reason,
                usedApparent = apparent != null,
            )
        }
        FactorKind.WIND_SPEED -> hour.windSpeedKmh?.let { speed ->
            val band = profile.wind
            Measured(
                score = decliningScore(speed, band.goodUntil, band.badAt),
                measured = speed,
                unit = MeasurementUnit.KILOMETERS_PER_HOUR,
                reason = ReasonCode.WIND,
            )
        }
        FactorKind.WIND_GUSTS -> hour.windGustsKmh?.let { speed ->
            val band = profile.gust
            Measured(
                score = decliningScore(speed, band.goodUntil, band.badAt),
                measured = speed,
                unit = MeasurementUnit.KILOMETERS_PER_HOUR,
                reason = ReasonCode.WIND,
            )
        }
        FactorKind.UV_INDEX -> {
            val band = profile.uv ?: return null
            hour.uvIndex?.let { uv ->
                Measured(
                    score = decliningScore(uv, band.goodUntil, band.badAt),
                    measured = uv,
                    unit = MeasurementUnit.UV_INDEX,
                    reason = ReasonCode.UV,
                )
            }
        }
        FactorKind.WEATHER_CODE -> hour.weatherCode?.let { code ->
            Measured(
                score = weatherCodeScore(code),
                measured = code.toDouble(),
                unit = MeasurementUnit.NONE,
                reason = if (code in thresholds.severeWeatherCodes) ReasonCode.SEVERE_WEATHER else ReasonCode.WEATHER,
            )
        }
        FactorKind.WIND_DIRECTION, FactorKind.AIR_QUALITY, FactorKind.WAVE_HEIGHT -> null
    }

    private fun safetyOf(hour: HourConditions): SafetyOverride? {
        val code = hour.weatherCode
        if (code != null && code in thresholds.severeWeatherCodes) {
            return SafetyOverride(ReasonCode.SEVERE_WEATHER, code, hour.windSpeedKmh, hour.windGustsKmh)
        }
        if (hour.windSpeedKmh != null && hour.windSpeedKmh >= ActivityThresholds.SEVERE_WIND_KMH) {
            return SafetyOverride(ReasonCode.WIND, code, hour.windSpeedKmh, hour.windGustsKmh)
        }
        if (hour.windGustsKmh != null && hour.windGustsKmh >= ActivityThresholds.SEVERE_GUST_KMH) {
            return SafetyOverride(ReasonCode.WIND, code, hour.windSpeedKmh, hour.windGustsKmh)
        }
        return null
    }

    private fun polarity(score: Int): FactorPolarity = when {
        score >= ActivityThresholds.SUITABLE_AT -> FactorPolarity.POSITIVE
        score < 50 -> FactorPolarity.NEGATIVE
        else -> FactorPolarity.NEUTRAL
    }

    private data class Measured(
        val score: Int,
        val measured: Double,
        val unit: MeasurementUnit,
        val reason: ReasonCode,
        val usedApparent: Boolean = false,
    )
}

private fun HourConditions.snapshot(): HourSnapshot = HourSnapshot(
    temperatureC = apparentTemperatureC ?: temperatureC,
    precipitationProbabilityPercent = precipitationProbabilityPercent,
    windSpeedKmh = windSpeedKmh,
)

private fun bandScore(value: Double, band: ComfortBand): Int {
    if (value in band.idealLow..band.idealHigh) return 100
    if (value <= band.hardLow || value >= band.hardHigh) return 0
    val fraction = if (value < band.idealLow) {
        (value - band.hardLow) / (band.idealLow - band.hardLow)
    } else {
        (band.hardHigh - value) / (band.hardHigh - band.idealHigh)
    }
    return (fraction * 100).roundToInt().coerceIn(0, 100)
}

private fun decliningScore(value: Double, goodUntil: Double, badAt: Double): Int {
    if (badAt <= goodUntil) return if (value <= goodUntil) 100 else 0
    if (value <= goodUntil) return 100
    if (value >= badAt) return 0
    return ((badAt - value) / (badAt - goodUntil) * 100).roundToInt().coerceIn(0, 100)
}

private fun weatherCodeScore(code: Int): Int = when (code) {
    0, 1, 2, 3 -> 100
    45, 48 -> 45
    51, 53, 55, 56, 57 -> 55
    61, 63, 66, 80, 81 -> 35
    65, 67, 82 -> 0
    71, 73, 75, 77, 85, 86 -> 15
    95, 96, 99 -> 0
    else -> 60
}
