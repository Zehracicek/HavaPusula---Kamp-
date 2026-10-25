package com.example.hava.planb

data class SafetyOverride(
    val reason: ReasonCode,
    val weatherCode: Int?,
    val windSpeedKmh: Double?,
    val windGustsKmh: Double?,
)

data class CriterionAssessment(
    val kind: FactorKind,
    val score: Int,
    val weight: Double,
    val measured: Double?,
    val unit: MeasurementUnit,
    val polarity: FactorPolarity,
    val reason: ReasonCode,
    val usedApparentTemperature: Boolean = false,
)

data class HourSnapshot(
    val temperatureC: Double?,
    val precipitationProbabilityPercent: Int?,
    val windSpeedKmh: Double?,
)

sealed interface HourAssessment {
    val time: String
    val snapshot: HourSnapshot

    data class Scored(
        override val time: String,
        val score: Int,
        val level: SuitabilityLevel,
        val factors: List<CriterionAssessment>,
        val missingOptional: List<FactorKind>,
        val safetyOverride: SafetyOverride?,
        override val snapshot: HourSnapshot,
    ) : HourAssessment

    data class Insufficient(
        override val time: String,
        val missing: List<FactorKind>,
        override val snapshot: HourSnapshot,
    ) : HourAssessment
}

sealed interface OverallAssessment {
    data class Scored(
        val score: Int,
        val level: SuitabilityLevel,
        val positive: List<FactorKind>,
        val negative: List<FactorKind>,
        val missingOptional: List<FactorKind>,
        val safetyOverride: Boolean,
        /** Seçilen penceredeki en zayıf olumsuz gerekçe; öneriler bunu kullanır. */
        val dominantReason: ReasonCode?,
    ) : OverallAssessment

    data class Insufficient(
        val missing: List<FactorKind>,
        val reason: InsufficientReason,
    ) : OverallAssessment
}

data class WindowAssessment(
    val hours: List<HourAssessment>,
    val overall: OverallAssessment,
    val unavailable: List<FactorKind>,
)

data class SuggestedWindow(
    val startTime: String,
    val endTime: String,
    val score: Int,
    val level: SuitabilityLevel,
    /** Aralık boyunca her saat Uygun ise true. */
    val fullySuitable: Boolean,
)

data class AlternativeActivity(
    val activity: ActivityType,
    val score: Int,
    val level: SuitabilityLevel,
    val reason: ReasonCode,
)

data class PlanSuggestions(
    val betterWindow: SuggestedWindow? = null,
    val alternativeActivity: AlternativeActivity? = null,
    val indoor: IndoorSuggestion? = null,
    val reason: ReasonCode? = null,
) {
    val isEmpty: Boolean
        get() = betterWindow == null && alternativeActivity == null && indoor == null
}

data class Measurement(
    val kind: FactorKind,
    val min: Double,
    val max: Double,
    val unit: MeasurementUnit,
)

data class ActivityEvaluation(
    val activity: ActivityType,
    val date: String,
    val startHour: Int,
    val endHour: Int,
    val requestedHourCount: Int,
    val window: WindowAssessment,
    val rankedHours: List<HourAssessment.Scored>,
    val bestWindow: SuggestedWindow?,
    val suggestions: PlanSuggestions,
    val measurements: List<Measurement>,
    /** Bisiklette yön geldi ama rota olmadığı için puana katılmadı. */
    val windDirectionKnown: Boolean = false,
)
