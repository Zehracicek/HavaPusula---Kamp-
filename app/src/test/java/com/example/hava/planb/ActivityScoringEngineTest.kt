package com.example.hava.planb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityScoringEngineTest {

    private val engine = ActivityScoringEngine()
    private val finder = BestWindowFinder()
    private val evaluator = PlanBEvaluator()

    @Test
    fun `aktivite ağırlıkları bire toplanır`() {
        ActivityType.entries.forEach { activity ->
            val specs = ActivityThresholds.specs(activity)
            assertEquals(1.0, specs.sumOf { it.weight }, 0.001)
            assertTrue(specs.all { it.weight > 0 })
        }
    }

    @Test
    fun `yağış olasılığı piknik puanını düşürür`() {
        val dry = engine.scoreHour(ActivityType.PICNIC, hour(precipProbability = 5, precipMm = 0.0))
        val wet = engine.scoreHour(ActivityType.PICNIC, hour(precipProbability = 90, precipMm = 3.0, code = 63))
        val dryScore = (dry as HourAssessment.Scored).score
        val wetScore = (wet as HourAssessment.Scored).score
        assertTrue(wetScore < dryScore)
        assertTrue((wet as HourAssessment.Scored).factors.any {
            it.kind == FactorKind.PRECIPITATION_PROBABILITY && it.polarity == FactorPolarity.NEGATIVE
        })
    }

    @Test
    fun `yüksek sıcaklık koşu puanını düşürür`() {
        val cool = engine.scoreHour(ActivityType.RUN, hour(temperature = 12.0, apparent = 12.0, uv = 3.0))
        val hot = engine.scoreHour(ActivityType.RUN, hour(temperature = 32.0, apparent = 34.0, uv = 3.0))
        assertTrue((hot as HourAssessment.Scored).score < (cool as HourAssessment.Scored).score)
        assertTrue(hot.factors.any { it.kind == FactorKind.TEMPERATURE && it.reason == ReasonCode.HEAT })
    }

    @Test
    fun `eşit puanlı saatler erken saate göre sıralanır`() {
        val later = scored("2026-10-25T15:00", 80)
        val earlier = scored("2026-10-25T11:00", 80)
        val weaker = scored("2026-10-25T09:00", 40)
        val ranked = finder.rank(listOf(later, weaker, earlier))
        assertEquals(listOf("2026-10-25T11:00", "2026-10-25T15:00", "2026-10-25T09:00"), ranked.map { it.time })
    }

    @Test
    fun `aralarında boşluk olan uygun saatler tek aralığa birleşmez`() {
        val window = finder.bestWindow(
            listOf(scored("2026-10-25T10:00", 90), scored("2026-10-25T12:00", 90)),
        )
        assertEquals("2026-10-25T10:00", window?.startTime)
        assertEquals("2026-10-25T10:00", window?.endTime)
    }

    @Test
    fun `kritik veri eksikse puan üretilmez`() {
        val result = engine.scoreHour(
            ActivityType.PICNIC,
            hour(precipProbability = null),
        )
        assertTrue(result is HourAssessment.Insufficient)
        assertTrue((result as HourAssessment.Insufficient).missing.contains(FactorKind.PRECIPITATION_PROBABILITY))
        val window = engine.scoreWindow(ActivityType.PICNIC, listOf(hour(precipProbability = null)))
        assertTrue(window.overall is OverallAssessment.Insufficient)
    }

    @Test
    fun `saatlik veri yoksa puan üretilmez`() {
        val evaluation = evaluator.evaluate(ActivityType.PICNIC, "2026-10-25", 10, 12, emptyList())
        val overall = evaluation.window.overall
        assertTrue(overall is OverallAssessment.Insufficient)
        assertEquals(InsufficientReason.NO_HOURLY_DATA, (overall as OverallAssessment.Insufficient).reason)
        assertNull(evaluation.bestWindow)
    }

    @Test
    fun `şiddetli hava güvenlik kuralını tetikler`() {
        val storm = engine.scoreHour(ActivityType.PICNIC, hour(code = 95)) as HourAssessment.Scored
        assertEquals(SuitabilityLevel.UNSUITABLE, storm.level)
        assertEquals(ReasonCode.SEVERE_WEATHER, storm.safetyOverride?.reason)
        assertTrue(storm.score <= ActivityThresholds.SAFETY_SCORE_CAP)

        val gale = engine.scoreHour(ActivityType.WALK, hour(wind = 55.0)) as HourAssessment.Scored
        assertEquals(SuitabilityLevel.UNSUITABLE, gale.level)
        assertEquals(ReasonCode.WIND, gale.safetyOverride?.reason)
    }

    @Test
    fun `elverişsiz saatte aynı günün daha uygun saati önerilir`() {
        val morning = hour(time = "2026-10-25T10:00")
        val evening = hour(
            time = "2026-10-25T16:00",
            precipProbability = 100,
            precipMm = 4.0,
            code = 63,
            wind = 20.0,
        )
        val evaluation = evaluator.evaluate(
            ActivityType.PICNIC,
            date = "2026-10-25",
            startHour = 16,
            endHour = 16,
            dayHours = listOf(morning, evening),
        )
        val overall = evaluation.window.overall as OverallAssessment.Scored
        assertTrue(overall.level != SuitabilityLevel.SUITABLE)
        assertEquals("2026-10-25T10:00", evaluation.suggestions.betterWindow?.startTime)
        assertTrue(evaluation.suggestions.reason == ReasonCode.PRECIPITATION || overall.dominantReason == ReasonCode.PRECIPITATION)
    }

    @Test
    fun `deniz alternatifinde dalga verisi olmadığı belirtilir`() {
        val evaluation = evaluator.evaluate(
            ActivityType.BEACH,
            "2026-10-25",
            12,
            12,
            listOf(hour(time = "2026-10-25T12:00", temperature = 28.0, apparent = 28.0)),
        )
        assertTrue(evaluation.window.unavailable.contains(FactorKind.WAVE_HEIGHT))
    }

    @Test
    fun `yürüyüşte hava kalitesi olmadığı belirtilir`() {
        val window = engine.scoreWindow(ActivityType.WALK, listOf(hour()))
        assertTrue(window.unavailable.contains(FactorKind.AIR_QUALITY))
    }

    @Test
    fun `soğuk ve yağışlı plaj yerine yürüyüş önerilir`() {
        val conditions = hour(
            time = "2026-10-25T14:00",
            temperature = 16.0,
            apparent = 16.0,
            precipProbability = 40,
            precipMm = 0.2,
            wind = 30.0,
            gust = 15.0,
            uv = 9.0,
            code = 2,
        )
        val evaluation = evaluator.evaluate(ActivityType.BEACH, "2026-10-25", 14, 14, listOf(conditions))
        assertEquals(ActivityType.WALK, evaluation.suggestions.alternativeActivity?.activity)
        assertTrue(
            (evaluation.suggestions.alternativeActivity?.score ?: 0) >
                ((evaluation.window.overall as OverallAssessment.Scored).score),
        )
    }

    @Test
    fun `özel aktivitede yalnızca seçilen ölçütler puanlanır`() {
        val archery = CustomActivity("Okçuluk", setOf(CustomFactor.WIND, CustomFactor.PRECIPITATION))
        val profile = ActivityThresholds.profile(archery)
        assertEquals(1.0, profile.specs.sumOf { it.weight }, 0.001)
        val kinds = profile.specs.map { it.kind }.toSet()
        assertTrue(FactorKind.WIND_SPEED in kinds)
        assertTrue(FactorKind.TEMPERATURE !in kinds)
        assertTrue(FactorKind.UV_INDEX !in kinds)

        val freezing = engine.scoreHour(profile, hour(temperature = -3.0, apparent = -6.0)) as HourAssessment.Scored
        assertTrue(freezing.factors.none { it.kind == FactorKind.TEMPERATURE })
        assertTrue(freezing.score >= ActivityThresholds.SUITABLE_AT)
    }

    @Test
    fun `rüzgâra hassas özel aktivite rüzgârda yürüyüşten düşük puan alır`() {
        val archery = ActivityThresholds.profile(CustomActivity("Okçuluk", setOf(CustomFactor.WIND)))
        val breezy = hour(wind = 22.0, gust = 30.0)
        val archeryScore = (engine.scoreHour(archery, breezy) as HourAssessment.Scored).score
        val walkScore = (engine.scoreHour(ActivityType.WALK, breezy) as HourAssessment.Scored).score
        assertTrue(archeryScore < walkScore)
    }

    @Test
    fun `özel aktivite değerlendirmesi adı taşır`() {
        val custom = CustomActivity("Okçuluk", setOf(CustomFactor.WIND))
        val evaluation = evaluator.evaluate(ActivityType.CUSTOM, "2026-10-25", 14, 14, listOf(hour()), custom)
        assertEquals("Okçuluk", evaluation.custom?.name)
        assertEquals(IndoorSuggestion.GENERAL, ActivityThresholds.profile(custom).indoor)
        val preset = evaluator.evaluate(ActivityType.PICNIC, "2026-10-25", 14, 14, listOf(hour()), custom)
        assertNull(preset.custom)
    }

    private fun hour(
        time: String = "2026-10-25T14:00",
        temperature: Double = 22.0,
        apparent: Double? = 22.0,
        precipProbability: Int? = 5,
        precipMm: Double? = 0.0,
        wind: Double? = 8.0,
        gust: Double? = 12.0,
        uv: Double? = 3.0,
        code: Int? = 1,
        direction: Int? = null,
    ) = HourConditions(
        time = time,
        temperatureC = temperature,
        apparentTemperatureC = apparent,
        precipitationProbabilityPercent = precipProbability,
        precipitationMm = precipMm,
        windSpeedKmh = wind,
        windDirectionDegrees = direction,
        windGustsKmh = gust,
        uvIndex = uv,
        weatherCode = code,
    )

    private fun scored(time: String, score: Int) = HourAssessment.Scored(
        time = time,
        score = score,
        level = suitabilityLevel(score),
        factors = emptyList(),
        missingOptional = emptyList(),
        safetyOverride = null,
        snapshot = HourSnapshot(20.0, 0, 10.0),
    )
}
