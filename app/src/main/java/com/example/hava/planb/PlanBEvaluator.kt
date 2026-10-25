package com.example.hava.planb

class PlanBEvaluator(
    private val engine: ActivityScoringEngine = ActivityScoringEngine(),
    private val finder: BestWindowFinder = BestWindowFinder(),
    private val alternatives: PlanBAlternatives = PlanBAlternatives(engine, finder),
    private val thresholds: ActivityThresholds = ActivityThresholds,
) {

    /** [custom] yalnızca [ActivityType.CUSTOM] için kullanılır. */
    fun evaluate(
        activity: ActivityType,
        date: String,
        startHour: Int,
        endHour: Int,
        dayHours: List<HourConditions>,
        custom: CustomActivity? = null,
    ): ActivityEvaluation {
        val customActivity = custom?.takeIf { activity == ActivityType.CUSTOM }
        val profile = customActivity?.let { thresholds.profile(it) } ?: thresholds.profile(activity)
        val requested = (endHour - startHour + 1).coerceAtLeast(0)
        val selected = dayHours
            .filter { it.time.substringBefore('T') == date }
            .filter { hour ->
                val clock = hour.time.forecastHour()
                clock != null && clock in startHour..endHour
            }
            .sortedBy { it.time }
        val window = engine.scoreWindow(profile, selected)
        val ranked = finder.rank(window.hours)
        val best = finder.bestWindow(window.hours)
        val suggestions = alternatives.suggest(profile, dayHours, selected, window)
        return ActivityEvaluation(
            activity = activity,
            custom = customActivity,
            date = date,
            startHour = startHour,
            endHour = endHour,
            requestedHourCount = requested,
            window = window,
            rankedHours = ranked,
            bestWindow = best,
            suggestions = suggestions,
            measurements = measurementsOf(selected),
            windDirectionKnown = profile.tracksWindDirection && selected.any { it.windDirectionDegrees != null },
        )
    }
}

private fun measurementsOf(hours: List<HourConditions>): List<Measurement> {
    fun range(kind: FactorKind, unit: MeasurementUnit, values: List<Double>): Measurement? {
        if (values.isEmpty()) return null
        return Measurement(kind, values.min(), values.max(), unit)
    }
    return listOfNotNull(
        range(FactorKind.TEMPERATURE, MeasurementUnit.CELSIUS, hours.mapNotNull { it.apparentTemperatureC ?: it.temperatureC }),
        range(FactorKind.PRECIPITATION_PROBABILITY, MeasurementUnit.PERCENT, hours.mapNotNull { it.precipitationProbabilityPercent?.toDouble() }),
        range(FactorKind.PRECIPITATION_AMOUNT, MeasurementUnit.MILLIMETER, hours.mapNotNull { it.precipitationMm }),
        range(FactorKind.WIND_SPEED, MeasurementUnit.KILOMETERS_PER_HOUR, hours.mapNotNull { it.windSpeedKmh }),
        range(FactorKind.WIND_GUSTS, MeasurementUnit.KILOMETERS_PER_HOUR, hours.mapNotNull { it.windGustsKmh }),
        range(FactorKind.UV_INDEX, MeasurementUnit.UV_INDEX, hours.mapNotNull { it.uvIndex }),
    )
}
