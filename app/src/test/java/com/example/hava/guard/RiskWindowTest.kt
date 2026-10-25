package com.example.hava.guard

import com.example.hava.planb.HourConditions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RiskWindowTest {

    private val engine = RiskEngine()

    @Test
    fun `eşiği kesintisiz aşan saatler aralık olur`() {
        val hours = listOf(
            hour("2026-10-25T14:00", probability = 20),
            hour("2026-10-25T15:00", probability = 75),
            hour("2026-10-25T16:00", probability = 70),
            hour("2026-10-25T17:00", probability = 65),
            hour("2026-10-25T18:00", probability = 30),
        )
        val finding = engine.evaluate(hours, RiskSettings()).active.single { it.risk == GuardRisk.PRECIPITATION }
        assertEquals(RiskWindow("2026-10-25T15:00", "2026-10-25T17:00"), finding.window(hours))
    }

    @Test
    fun `don aralığı eşiğin altındaki saatlerden oluşur`() {
        val hours = listOf(
            hour("2026-10-25T03:00", temperature = -0.5),
            hour("2026-10-25T04:00", temperature = -1.0),
            hour("2026-10-25T05:00", temperature = 4.0),
        )
        val finding = engine.evaluate(hours, RiskSettings()).active.single { it.risk == GuardRisk.FROST }
        assertEquals(RiskWindow("2026-10-25T03:00", "2026-10-25T04:00"), finding.window(hours))
    }

    @Test
    fun `geçerli saati olmayan bulgu aralık üretmez`() {
        val finding = RiskFinding(
            risk = GuardRisk.UV,
            status = RiskStatus.INSUFFICIENT,
            level = null,
            validAt = null,
            measurements = emptyList(),
            thresholds = emptyList(),
            reason = RiskReason.MISSING_DATA,
            missing = emptyList(),
        )
        assertNull(finding.window(emptyList()))
    }

    private fun hour(time: String, probability: Int = 5, temperature: Double = 20.0) = HourConditions(
        time = time,
        temperatureC = temperature,
        apparentTemperatureC = temperature,
        precipitationProbabilityPercent = probability,
        precipitationMm = 0.0,
        windSpeedKmh = 8.0,
        windGustsKmh = 12.0,
        uvIndex = 1.0,
        weatherCode = 1,
    )
}
