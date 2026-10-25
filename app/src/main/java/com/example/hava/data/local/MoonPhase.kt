package com.example.hava.data.local

import com.example.hava.data.model.DailyForecast
import com.example.hava.data.model.MoonInfo
import com.example.hava.data.model.MoonPhase
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt

private const val SYNODIC_MONTH_DAYS = 29.530588
private const val FULL_MOON_WINDOW = 0.03
private const val TIME_PREFIX_LENGTH = 13

/** 0 yeni ayda %0, 0.5 dolunayda %100. */
fun moonIlluminationPercent(phase: Double): Int {
    val cycle = phase.asCycle()
    val fraction = (1 - cos(2 * PI * cycle)) / 2
    return (fraction * 100).roundToInt().coerceIn(0, 100)
}

/** moon_phases dizisindeki sıra: Yeni ay..Küçülen hilal */
fun moonPhaseIndex(phase: Double): Int = ((phase.asCycle() * 8.0) + 0.5).toInt() % 8

fun daysUntilFullMoon(phase: Double): Int {
    val cycle = phase.asCycle()
    if (abs(cycle - 0.5) < FULL_MOON_WINDOW) return 0
    val toFull = (0.5 - cycle + 1.0) % 1.0
    return (toFull * SYNODIC_MONTH_DAYS).roundToInt().coerceAtLeast(1)
}

fun nextMoonset(now: String?, daily: List<DailyForecast>): String? =
    nextMoonEvent(now, daily) { it.moonset }

fun nextMoonrise(now: String?, daily: List<DailyForecast>): String? =
    nextMoonEvent(now, daily) { it.moonrise }

fun daysUntilNewMoon(phase: Double): Int {
    val cycle = phase.asCycle()
    if (cycle < FULL_MOON_WINDOW || cycle > 1 - FULL_MOON_WINDOW) return 0
    return ((1.0 - cycle) * SYNODIC_MONTH_DAYS).roundToInt().coerceAtLeast(1)
}

fun moonInfoOf(now: String?, daily: List<DailyForecast>): MoonInfo? {
    val phase = daily.firstOrNull()?.moonPhase ?: return null
    val fullMoonIndex = daily.indexOfFirst { day ->
        day.moonPhase != null && abs(day.moonPhase - 0.5) < FULL_MOON_WINDOW
    }
    return MoonInfo(
        phase = phase,
        illumination = moonIlluminationPercent(phase) / 100.0,
        phaseName = MoonPhase.entries[moonPhaseIndex(phase)],
        nextRise = nextMoonrise(now, daily),
        nextSet = nextMoonset(now, daily),
        daysUntilFullMoon = if (fullMoonIndex >= 0) fullMoonIndex else daysUntilFullMoon(phase),
        daysUntilNewMoon = daysUntilNewMoon(phase),
    )
}

private fun nextMoonEvent(
    now: String?,
    daily: List<DailyForecast>,
    event: (DailyForecast) -> String?,
): String? {
    val start = now?.take(TIME_PREFIX_LENGTH)
    return daily.asSequence()
        .mapNotNull(event)
        .firstOrNull { start == null || it.take(TIME_PREFIX_LENGTH) >= start }
}

private fun Double.asCycle(): Double = ((this % 1.0) + 1.0) % 1.0
