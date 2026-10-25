package com.example.hava.data.local

import com.example.hava.data.model.DailyForecast
import com.example.hava.data.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoonPhaseTest {

    @Test
    fun `yeni ay ve dolunay aydınlatması`() {
        assertEquals(0, moonIlluminationPercent(0.0))
        assertEquals(100, moonIlluminationPercent(0.5))
        assertEquals(0, moonIlluminationPercent(1.0))
    }

    @Test
    fun `evre adı sırası`() {
        assertEquals(0, moonPhaseIndex(0.0))
        assertEquals(2, moonPhaseIndex(0.25))
        assertEquals(4, moonPhaseIndex(0.5))
        assertEquals(6, moonPhaseIndex(0.75))
    }

    @Test
    fun `dolunaya kalan gün`() {
        assertEquals(0, daysUntilFullMoon(0.5))
        assertEquals(1, daysUntilFullMoon(0.47))
    }

    @Test
    fun `geçmiş ay batışı atlanır`() {
        val days = listOf(
            day("2026-10-25", moonset = "2026-10-25T06:09", phase = 0.47),
            day("2026-10-26", moonset = "2026-10-26T07:24", phase = 0.51),
        )

        assertEquals("2026-10-26T07:24", nextMoonset("2026-10-25T14:15", days))
        assertEquals("2026-10-25T06:09", nextMoonset("2026-10-25T05:00", days))
        assertNull(nextMoonset("2026-10-26T18:00", days))
    }

    @Test
    fun `dolunay tahmin penceresindeyse gün sayısı oradan okunur`() {
        val days = listOf(
            day("2026-10-25", phase = 0.47),
            day("2026-10-26", phase = 0.507),
        )

        assertEquals(1, moonInfoOf("2026-10-25T14:15", days)?.daysUntilFullMoon)
        assertEquals(0.99, moonInfoOf("2026-10-25T14:15", days)!!.illumination, 0.01)
    }

    private fun day(date: String, moonset: String? = null, phase: Double) = DailyForecast(
        date = date,
        maxTemperature = 10.0,
        minTemperature = 1.0,
        weatherCode = 0,
        condition = WeatherCondition.CLEAR,
        moonset = moonset,
        moonPhase = phase,
    )
}
