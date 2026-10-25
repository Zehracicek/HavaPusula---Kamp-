package com.example.hava.planb

import kotlin.math.roundToInt

class BestWindowFinder {

    /** Yüksek puan önce; eşit puanda daha erken saat. */
    fun rank(hours: List<HourAssessment>): List<HourAssessment.Scored> =
        hours.filterIsInstance<HourAssessment.Scored>()
            .sortedWith(compareByDescending<HourAssessment.Scored> { it.score }.thenBy { it.time })

    /**
     * Kesintisiz Uygun saatler varsa en uzununu, eşitlikte daha yüksek ortalamayı, onda da daha erken başı seçer.
     * Uygun blok yoksa en yüksek puanlı tek saati döner.
     */
    fun bestWindow(hours: List<HourAssessment>): SuggestedWindow? {
        val scored = hours.filterIsInstance<HourAssessment.Scored>().sortedBy { it.time }
        if (scored.isEmpty()) return null
        val suitable = scored.filter { it.level == SuitabilityLevel.SUITABLE && it.safetyOverride == null }
        val bestRun = consecutiveRuns(suitable).maxWithOrNull(runComparator)
        if (bestRun != null) return toWindow(bestRun, fullySuitable = true)
        return toWindow(listOf(rank(scored).first()), fullySuitable = false)
    }

    private val runComparator = compareByDescending<List<HourAssessment.Scored>> { it.size }
        .thenByDescending { run -> run.map { it.score }.average() }
        .thenByDescending { it.first().time }

    private fun toWindow(hours: List<HourAssessment.Scored>, fullySuitable: Boolean): SuggestedWindow {
        val score = hours.map { it.score }.average().roundToInt().coerceIn(0, 100)
        val level = if (hours.any { it.safetyOverride != null }) {
            SuitabilityLevel.UNSUITABLE
        } else if (fullySuitable) {
            SuitabilityLevel.SUITABLE
        } else {
            suitabilityLevel(score)
        }
        return SuggestedWindow(
            startTime = hours.first().time,
            endTime = hours.last().time,
            score = if (hours.any { it.safetyOverride != null }) hours.minOf { it.score } else score,
            level = level,
            fullySuitable = fullySuitable,
        )
    }
}

internal fun consecutiveRuns(hours: List<HourAssessment.Scored>): List<List<HourAssessment.Scored>> {
    if (hours.isEmpty()) return emptyList()
    val sorted = hours.sortedBy { it.time }
    val runs = mutableListOf<MutableList<HourAssessment.Scored>>()
    for (hour in sorted) {
        val current = runs.lastOrNull()
        if (current == null || !isNextHour(current.last().time, hour.time)) {
            runs += mutableListOf(hour)
        } else {
            current += hour
        }
    }
    return runs
}
