package com.example.hava.guard

import com.example.hava.planb.FactorKind
import com.example.hava.planb.HourConditions
import com.example.hava.planb.MeasurementUnit

/**
 * Eşikler ilk sürüm varsayımıdır; bilimsel uyarı eşiği değildir.
 * Birimler: sıcaklık °C, rüzgâr km/h, yağış mm, olasılık %, UV indeksi.
 * Günlük özet saatlik veri gibi kullanılmaz. Olasılık tek başına şiddetli yağış sayılmaz.
 */
class RiskEngine {

    fun evaluate(hours: List<HourConditions>, settings: RiskSettings): GuardReport {
        val ordered = hours.sortedBy { it.time }
        val active = mutableListOf<RiskFinding>()
        val insufficient = mutableListOf<RiskFinding>()
        val clear = mutableListOf<GuardRisk>()

        fun keep(finding: RiskFinding) {
            when (finding.status) {
                RiskStatus.ACTIVE -> active += finding
                RiskStatus.INSUFFICIENT -> insufficient += finding
                RiskStatus.CLEAR -> clear += finding.risk
            }
        }

        if (UserRisk.PRECIPITATION in settings.enabled) keep(precipitation(ordered, settings))
        if (UserRisk.WIND in settings.enabled) {
            keep(wind(ordered, settings))
            keep(gust(ordered, settings))
        }
        if (UserRisk.HEAT in settings.enabled) keep(heat(ordered, settings))
        if (UserRisk.FROST in settings.enabled) keep(frost(ordered, settings))
        if (UserRisk.UV in settings.enabled) keep(uv(ordered, settings))

        return GuardReport(
            active = active.sortedWith(compareByDescending<RiskFinding> { it.level == RiskLevel.HIGH }.thenBy { it.validAt }),
            insufficient = insufficient,
            clearRisks = clear,
            thunderstorm = thunderstorm(ordered),
        )
    }

    private fun precipitation(hours: List<HourConditions>, settings: RiskSettings): RiskFinding {
        val usable = hours.filter { it.precipitationProbabilityPercent != null || it.precipitationMm != null }
        if (usable.isEmpty()) {
            return insufficient(
                GuardRisk.PRECIPITATION,
                listOf(FactorKind.PRECIPITATION_PROBABILITY, FactorKind.PRECIPITATION_AMOUNT),
            )
        }
        var worst: Ranked? = null
        for (hour in usable) {
            val ranked = rankPrecipitation(hour, settings) ?: continue
            if (worst == null || ranked.outranks(worst)) worst = ranked
        }
        return worst?.toFinding() ?: clear(
            GuardRisk.PRECIPITATION,
            listOf(
                threshold(FactorKind.PRECIPITATION_PROBABILITY, settings.precipProbabilityCautionPercent.toDouble(), MeasurementUnit.PERCENT),
                threshold(FactorKind.PRECIPITATION_AMOUNT, settings.precipAmountCautionMm, MeasurementUnit.MILLIMETER),
            ),
        )
    }

    private fun rankPrecipitation(hour: HourConditions, settings: RiskSettings): Ranked? {
        val probability = hour.precipitationProbabilityPercent
        val amount = hour.precipitationMm
        val measurements = measurementsOf(
            probability?.let { RiskMeasurement(FactorKind.PRECIPITATION_PROBABILITY, it.toDouble(), MeasurementUnit.PERCENT) },
            amount?.let { RiskMeasurement(FactorKind.PRECIPITATION_AMOUNT, it, MeasurementUnit.MILLIMETER) },
        )
        val probabilityHigh = probability != null && probability >= settings.precipProbabilityHighPercent
        val probabilityCaution = probability != null && probability >= settings.precipProbabilityCautionPercent
        val amountHigh = amount != null && amount >= settings.precipAmountHighMm
        val amountCaution = amount != null && amount >= settings.precipAmountCautionMm
        val (level, reason) = when {
            amountHigh && (probabilityHigh || probabilityCaution) -> RiskLevel.HIGH to RiskReason.PROBABILITY_AND_AMOUNT
            amountHigh -> RiskLevel.HIGH to RiskReason.AMOUNT_ONLY
            amountCaution && probability == null -> RiskLevel.CAUTION to RiskReason.AMOUNT_ONLY
            probabilityCaution && amount == null -> RiskLevel.CAUTION to RiskReason.PROBABILITY_ONLY
            probabilityCaution && !amountCaution -> RiskLevel.CAUTION to RiskReason.PROBABILITY_ONLY
            amountCaution -> RiskLevel.CAUTION to RiskReason.AMOUNT_ONLY
            else -> return null
        }
        val crossed = when (reason) {
            RiskReason.PROBABILITY_ONLY -> listOf(
                threshold(FactorKind.PRECIPITATION_PROBABILITY, settings.precipProbabilityCautionPercent.toDouble(), MeasurementUnit.PERCENT),
            )
            RiskReason.AMOUNT_ONLY -> listOf(
                threshold(
                    FactorKind.PRECIPITATION_AMOUNT,
                    if (level == RiskLevel.HIGH) settings.precipAmountHighMm else settings.precipAmountCautionMm,
                    MeasurementUnit.MILLIMETER,
                ),
            )
            else -> listOf(
                threshold(FactorKind.PRECIPITATION_PROBABILITY, settings.precipProbabilityCautionPercent.toDouble(), MeasurementUnit.PERCENT),
                threshold(
                    FactorKind.PRECIPITATION_AMOUNT,
                    if (level == RiskLevel.HIGH) settings.precipAmountHighMm else settings.precipAmountCautionMm,
                    MeasurementUnit.MILLIMETER,
                ),
            )
        }
        return Ranked(GuardRisk.PRECIPITATION, level, hour.time, measurements, crossed, reason)
    }

    private fun wind(hours: List<HourConditions>, settings: RiskSettings): RiskFinding =
        atLeast(
            hours = hours,
            risk = GuardRisk.WIND,
            kind = FactorKind.WIND_SPEED,
            unit = MeasurementUnit.KILOMETERS_PER_HOUR,
            caution = settings.windCautionKmh,
            high = settings.windHighKmh,
            reason = RiskReason.WIND_SPEED,
            value = { it.windSpeedKmh },
        )

    private fun gust(hours: List<HourConditions>, settings: RiskSettings): RiskFinding =
        atLeast(
            hours = hours,
            risk = GuardRisk.GUST,
            kind = FactorKind.WIND_GUSTS,
            unit = MeasurementUnit.KILOMETERS_PER_HOUR,
            caution = settings.gustCautionKmh,
            high = settings.gustHighKmh,
            reason = RiskReason.GUST,
            value = { it.windGustsKmh },
        )

    private fun heat(hours: List<HourConditions>, settings: RiskSettings): RiskFinding =
        atLeast(
            hours = hours,
            risk = GuardRisk.HEAT,
            kind = FactorKind.TEMPERATURE,
            unit = MeasurementUnit.CELSIUS,
            caution = settings.heatCautionC,
            high = settings.heatHighC,
            reason = RiskReason.HEAT,
            value = { it.apparentTemperatureC ?: it.temperatureC },
        )

    private fun frost(hours: List<HourConditions>, settings: RiskSettings): RiskFinding {
        val usable = hours.mapNotNull { hour -> hour.temperatureC?.let { hour to it } }
        if (usable.isEmpty()) return insufficient(GuardRisk.FROST, listOf(FactorKind.TEMPERATURE))
        var worst: Ranked? = null
        for ((hour, temperature) in usable) {
            val level = when {
                temperature <= settings.frostHighC -> RiskLevel.HIGH
                temperature <= settings.frostCautionC -> RiskLevel.CAUTION
                else -> null
            } ?: continue
            val ranked = Ranked(
                risk = GuardRisk.FROST,
                level = level,
                time = hour.time,
                measurements = listOf(RiskMeasurement(FactorKind.TEMPERATURE, temperature, MeasurementUnit.CELSIUS)),
                thresholds = listOf(
                    threshold(
                        FactorKind.TEMPERATURE,
                        if (level == RiskLevel.HIGH) settings.frostHighC else settings.frostCautionC,
                        MeasurementUnit.CELSIUS,
                    ),
                ),
                reason = RiskReason.FROST,
            )
            if (worst == null || ranked.outranks(worst)) worst = ranked
        }
        return worst?.toFinding() ?: clear(
            GuardRisk.FROST,
            listOf(threshold(FactorKind.TEMPERATURE, settings.frostCautionC, MeasurementUnit.CELSIUS)),
        )
    }

    private fun uv(hours: List<HourConditions>, settings: RiskSettings): RiskFinding =
        atLeast(
            hours = hours,
            risk = GuardRisk.UV,
            kind = FactorKind.UV_INDEX,
            unit = MeasurementUnit.UV_INDEX,
            caution = settings.uvCaution,
            high = settings.uvHigh,
            reason = RiskReason.UV,
            value = { it.uvIndex },
        )

    private fun thunderstorm(hours: List<HourConditions>): RiskFinding? {
        val storm = hours.firstOrNull { it.weatherCode in STORM_CODES } ?: return null
        return RiskFinding(
            risk = GuardRisk.THUNDERSTORM,
            status = RiskStatus.ACTIVE,
            level = RiskLevel.HIGH,
            validAt = storm.time,
            measurements = listOf(RiskMeasurement(FactorKind.WEATHER_CODE, storm.weatherCode!!.toDouble(), MeasurementUnit.NONE)),
            thresholds = emptyList(),
            reason = RiskReason.THUNDERSTORM,
            missing = emptyList(),
        )
    }

    private fun atLeast(
        hours: List<HourConditions>,
        risk: GuardRisk,
        kind: FactorKind,
        unit: MeasurementUnit,
        caution: Double,
        high: Double,
        reason: RiskReason,
        value: (HourConditions) -> Double?,
    ): RiskFinding {
        val usable = hours.mapNotNull { hour -> value(hour)?.let { hour to it } }
        if (usable.isEmpty()) return insufficient(risk, listOf(kind))
        var worst: Ranked? = null
        for ((hour, measured) in usable) {
            val level = when {
                measured >= high -> RiskLevel.HIGH
                measured >= caution -> RiskLevel.CAUTION
                else -> null
            } ?: continue
            val ranked = Ranked(
                risk = risk,
                level = level,
                time = hour.time,
                measurements = listOf(RiskMeasurement(kind, measured, unit)),
                thresholds = listOf(threshold(kind, if (level == RiskLevel.HIGH) high else caution, unit)),
                reason = reason,
            )
            if (worst == null || ranked.outranks(worst)) worst = ranked
        }
        return worst?.toFinding() ?: clear(risk, listOf(threshold(kind, caution, unit)))
    }

    private fun insufficient(risk: GuardRisk, missing: List<FactorKind>) = RiskFinding(
        risk = risk,
        status = RiskStatus.INSUFFICIENT,
        level = null,
        validAt = null,
        measurements = emptyList(),
        thresholds = emptyList(),
        reason = RiskReason.MISSING_DATA,
        missing = missing,
    )

    private fun clear(risk: GuardRisk, thresholds: List<RiskThreshold>) = RiskFinding(
        risk = risk,
        status = RiskStatus.CLEAR,
        level = null,
        validAt = null,
        measurements = emptyList(),
        thresholds = thresholds,
        reason = RiskReason.BELOW_THRESHOLD,
        missing = emptyList(),
    )

    private fun threshold(kind: FactorKind, value: Double, unit: MeasurementUnit) = RiskThreshold(kind, value, unit)

    private fun measurementsOf(vararg values: RiskMeasurement?) = values.filterNotNull()

    private class Ranked(
        val risk: GuardRisk,
        val level: RiskLevel,
        val time: String,
        val measurements: List<RiskMeasurement>,
        val thresholds: List<RiskThreshold>,
        val reason: RiskReason,
    ) {
        fun outranks(other: Ranked): Boolean {
            if (level != other.level) return level == RiskLevel.HIGH
            return time < other.time
        }

        fun toFinding() = RiskFinding(
            risk = risk,
            status = RiskStatus.ACTIVE,
            level = level,
            validAt = time,
            measurements = measurements,
            thresholds = thresholds,
            reason = reason,
            missing = emptyList(),
        )
    }

    private companion object {
        val STORM_CODES = setOf(95, 96, 99)
    }
}
