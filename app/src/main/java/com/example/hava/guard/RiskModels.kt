package com.example.hava.guard

import com.example.hava.planb.FactorKind
import com.example.hava.planb.HourConditions
import com.example.hava.planb.MeasurementUnit

/**
 * Kullanıcının açıp kapatabildiği riskler.
 * Gök gürültüsü genel güvenlik kontrolüdür; kapatılamaz.
 */
enum class UserRisk {
    PRECIPITATION,
    WIND,
    GUST,
    HEAT,
    FROST,
    UV,
}

enum class GuardRisk {
    PRECIPITATION,
    WIND,
    GUST,
    HEAT,
    FROST,
    UV,
    THUNDERSTORM,
}

enum class RiskLevel {
    CAUTION,
    HIGH,
}

enum class RiskStatus {
    ACTIVE,
    CLEAR,
    INSUFFICIENT,
}

enum class RiskReason {
    PROBABILITY_ONLY,
    AMOUNT_ONLY,
    PROBABILITY_AND_AMOUNT,
    WIND_SPEED,
    GUST,
    HEAT,
    FROST,
    UV,
    THUNDERSTORM,
    MISSING_DATA,
    BELOW_THRESHOLD,
}

enum class DataSource {
    OPEN_METEO,
}

data class RiskSettings(
    val enabled: Set<UserRisk> = UserRisk.entries.toSet(),
    val precipProbabilityCautionPercent: Int = 60,
    val precipProbabilityHighPercent: Int = 80,
    val precipAmountCautionMm: Double = 1.0,
    val precipAmountHighMm: Double = 4.0,
    val windCautionKmh: Double = 40.0,
    val windHighKmh: Double = 50.0,
    val gustCautionKmh: Double = 55.0,
    val gustHighKmh: Double = 70.0,
    val heatCautionC: Double = 32.0,
    val heatHighC: Double = 36.0,
    val frostCautionC: Double = 2.0,
    val frostHighC: Double = 0.0,
    val uvCaution: Double = 6.0,
    val uvHigh: Double = 8.0,
)

data class RiskMeasurement(
    val kind: FactorKind,
    val value: Double,
    val unit: MeasurementUnit,
)

data class RiskThreshold(
    val kind: FactorKind,
    val value: Double,
    val unit: MeasurementUnit,
)

data class RiskFinding(
    val risk: GuardRisk,
    val status: RiskStatus,
    val level: RiskLevel?,
    /** "yyyy-MM-ddTHH:mm"; eşik bu saatin tahminine aittir. */
    val validAt: String?,
    val measurements: List<RiskMeasurement>,
    val thresholds: List<RiskThreshold>,
    val reason: RiskReason,
    val missing: List<FactorKind>,
    val source: DataSource = DataSource.OPEN_METEO,
)

data class GuardReport(
    val active: List<RiskFinding>,
    val insufficient: List<RiskFinding>,
    val clearRisks: List<GuardRisk>,
    /** Yalnızca kod tespit edilirse dolu. Resmî uyarı değildir. */
    val thunderstorm: RiskFinding?,
)
