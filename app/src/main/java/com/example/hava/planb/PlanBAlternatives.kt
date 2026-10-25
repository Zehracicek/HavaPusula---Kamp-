package com.example.hava.planb

/**
 * Elverişsiz pencerede daha iyi saat, başka bir açık hava aktivitesi veya kapalı alan önerir.
 * Mekân adı, etkinlik takvimi veya olmayan hava verisi üretmez.
 */
class PlanBAlternatives(
    private val engine: ActivityScoringEngine = ActivityScoringEngine(),
    private val finder: BestWindowFinder = BestWindowFinder(),
    private val thresholds: ActivityThresholds = ActivityThresholds,
) {

    fun suggest(
        activity: ActivityType,
        dayHours: List<HourConditions>,
        selectedHours: List<HourConditions>,
        selected: WindowAssessment,
    ): PlanSuggestions {
        val overall = selected.overall
        if (overall is OverallAssessment.Scored && overall.level == SuitabilityLevel.SUITABLE) {
            return PlanSuggestions()
        }
        val reason = (overall as? OverallAssessment.Scored)?.dominantReason
            ?: ReasonCode.WEATHER
        val selectedScore = (overall as? OverallAssessment.Scored)?.score
        val selectedWindow = finder.bestWindow(selected.hours)

        val dayWindow = engine.scoreWindow(activity, dayHours.sortedBy { it.time })
        val better = finder.bestWindow(dayWindow.hours)
            ?.takeIf { it.fullySuitable && !sameWindow(it, selectedWindow) }
            ?.takeIf { selectedScore == null || it.score >= selectedScore + thresholds.ALTERNATIVE_MARGIN }

        val alternative = bestAlternative(activity, selectedHours, selectedScore)
        val indoor = if (alternative == null || alternative.level != SuitabilityLevel.SUITABLE) {
            thresholds.indoorFor(activity)
        } else {
            null
        }
        return PlanSuggestions(
            betterWindow = better,
            alternativeActivity = alternative,
            indoor = indoor,
            reason = reason,
        )
    }

    private fun bestAlternative(
        activity: ActivityType,
        selectedHours: List<HourConditions>,
        selectedScore: Int?,
    ): AlternativeActivity? {
        if (selectedHours.isEmpty()) return null
        return thresholds.alternativesFor(activity)
            .mapNotNull { candidate ->
                val overall = engine.scoreWindow(candidate, selectedHours).overall as? OverallAssessment.Scored
                    ?: return@mapNotNull null
                AlternativeActivity(candidate, overall.score, overall.level, overall.dominantReason ?: ReasonCode.WEATHER)
            }
            .filter { candidate ->
                selectedScore == null || candidate.score >= selectedScore + thresholds.ALTERNATIVE_MARGIN
            }
            .maxWithOrNull(compareBy<AlternativeActivity> { it.score }.thenBy { it.activity.ordinal })
    }

    private fun sameWindow(candidate: SuggestedWindow, selected: SuggestedWindow?): Boolean {
        if (selected == null) return false
        return candidate.startTime == selected.startTime && candidate.endTime == selected.endTime
    }
}
