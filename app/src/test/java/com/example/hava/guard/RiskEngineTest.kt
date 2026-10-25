package com.example.hava.guard

import com.example.hava.planb.FactorKind
import com.example.hava.planb.HourConditions
import com.example.hava.planb.MeasurementUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskEngineTest {

    private val engine = RiskEngine()
    private val settings = RiskSettings()

    @Test
    fun `olasılık tek başına şiddetli yağış sayılmaz`() {
        val finding = engine.evaluate(
            listOf(hour(precipProbability = 95, precipMm = null)),
            settings,
        ).active.single { it.risk == GuardRisk.PRECIPITATION }

        assertEquals(RiskLevel.CAUTION, finding.level)
        assertEquals(RiskReason.PROBABILITY_ONLY, finding.reason)
        assertEquals(MeasurementUnit.PERCENT, finding.measurements.single().unit)
    }

    @Test
    fun `yüksek olasılık ve yüksek miktar birlikte yüksek risktir`() {
        val finding = engine.evaluate(
            listOf(hour(precipProbability = 90, precipMm = 6.0)),
            settings,
        ).active.single { it.risk == GuardRisk.PRECIPITATION }

        assertEquals(RiskLevel.HIGH, finding.level)
        assertEquals(RiskReason.PROBABILITY_AND_AMOUNT, finding.reason)
        assertTrue(finding.measurements.any { it.unit == MeasurementUnit.MILLIMETER })
    }

    @Test
    fun `eşik aşılmayınca risk kartı oluşmaz`() {
        val report = engine.evaluate(listOf(hour(wind = 10.0, precipProbability = 10, precipMm = 0.0)), settings)
        assertTrue(report.active.none { it.risk == GuardRisk.WIND || it.risk == GuardRisk.PRECIPITATION })
        assertTrue(report.clearRisks.contains(GuardRisk.WIND))
        assertTrue(report.clearRisks.contains(GuardRisk.PRECIPITATION))
    }

    @Test
    fun `eksik rüzgâr risksiz sayılmaz`() {
        val report = engine.evaluate(listOf(hour(wind = null)), settings)
        val wind = report.insufficient.single { it.risk == GuardRisk.WIND }
        assertEquals(RiskStatus.INSUFFICIENT, wind.status)
        assertTrue(report.clearRisks.none { it == GuardRisk.WIND })
        assertTrue(report.active.none { it.risk == GuardRisk.WIND })
    }

    @Test
    fun `kapalı risk tercihi dikkate alınmaz`() {
        val report = engine.evaluate(
            listOf(hour(uv = 11.0, wind = 70.0)),
            settings.copy(enabled = setOf(UserRisk.WIND)),
        )
        assertTrue(report.active.any { it.risk == GuardRisk.WIND })
        assertTrue(report.active.none { it.risk == GuardRisk.UV })
        assertTrue(report.insufficient.none { it.risk == GuardRisk.UV })
        assertTrue(report.clearRisks.none { it == GuardRisk.UV })
    }

    @Test
    fun `sıcaklık ve rüzgâr birimleri korunur`() {
        val report = engine.evaluate(
            listOf(hour(temperature = 37.0, apparent = 38.0, wind = 52.0)),
            settings,
        )
        val heat = report.active.single { it.risk == GuardRisk.HEAT }
        val wind = report.active.single { it.risk == GuardRisk.WIND }
        assertEquals(MeasurementUnit.CELSIUS, heat.measurements.single().unit)
        assertEquals(38.0, heat.measurements.single().value, 0.0)
        assertEquals(MeasurementUnit.KILOMETERS_PER_HOUR, wind.measurements.single().unit)
        assertEquals(RiskLevel.HIGH, wind.level)
    }

    @Test
    fun `don eşiği hava sıcaklığına bakar`() {
        val report = engine.evaluate(listOf(hour(temperature = -1.0, apparent = 3.0)), settings)
        val frost = report.active.single { it.risk == GuardRisk.FROST }
        assertEquals(RiskLevel.HIGH, frost.level)
        assertEquals(-1.0, frost.measurements.single().value, 0.0)
        assertEquals(FactorKind.TEMPERATURE, frost.measurements.single().kind)
    }

    @Test
    fun `gök gürültüsü kodu kart üretir ve resmî eşik taşımaz`() {
        val report = engine.evaluate(listOf(hour(code = 95, time = "2026-10-25T18:00")), settings)
        val storm = requireNotNull(report.thunderstorm)
        assertEquals(RiskLevel.HIGH, storm.level)
        assertEquals("2026-10-25T18:00", storm.validAt)
        assertTrue(storm.thresholds.isEmpty())
        assertNull(engine.evaluate(listOf(hour(code = 2)), settings).thunderstorm)
    }

    @Test
    fun `aynı seviyede daha erken saat seçilir`() {
        val report = engine.evaluate(
            listOf(
                hour(time = "2026-10-25T16:00", wind = 45.0),
                hour(time = "2026-10-25T11:00", wind = 45.0),
            ),
            settings,
        )
        assertEquals("2026-10-25T11:00", report.active.single { it.risk == GuardRisk.WIND }.validAt)
    }

    private fun hour(
        time: String = "2026-10-25T14:00",
        temperature: Double? = 20.0,
        apparent: Double? = 20.0,
        precipProbability: Int? = 5,
        precipMm: Double? = 0.0,
        wind: Double? = 8.0,
        gust: Double? = 12.0,
        uv: Double? = 2.0,
        code: Int? = 1,
    ) = HourConditions(
        time = time,
        temperatureC = temperature,
        apparentTemperatureC = apparent,
        precipitationProbabilityPercent = precipProbability,
        precipitationMm = precipMm,
        windSpeedKmh = wind,
        windGustsKmh = gust,
        uvIndex = uv,
        weatherCode = code,
    )
}
