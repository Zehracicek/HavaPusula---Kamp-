package com.example.hava.guard

import com.example.hava.data.remote.dto.City
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskNoticeTest {

    private val city = City(id = 6, name = "Ankara", latitude = 39.9, longitude = 32.8, countryCode = "TR")

    @Test
    fun `aynı saat ve risk ikinci kez seçilmez`() {
        val notice = notice(validAt = "2026-10-25T16:40")
        val again = notice(validAt = "2026-10-25T16:10")
        val first = unseenNotices(listOf(notice), emptySet())
        val second = unseenNotices(listOf(again), first.map { it.key() }.toSet())
        assertEquals(1, first.size)
        assertTrue(second.isEmpty())
        assertEquals(notice.key(), again.key())
    }

    @Test
    fun `saat veya seviye değişince yeni bildirim olur`() {
        val caution = notice(level = RiskLevel.CAUTION, validAt = "2026-10-25T16:00")
        val later = notice(level = RiskLevel.CAUTION, validAt = "2026-10-25T18:00")
        val higher = notice(level = RiskLevel.HIGH, validAt = "2026-10-25T16:00")
        val sent = setOf(caution.key())
        val unseen = unseenNotices(listOf(later, higher, caution), sent)
        assertEquals(listOf(later.key(), higher.key()), unseen.map { it.key() })
    }

    @Test
    fun `eksik veri ve kapalı risk bildirim olmaz`() {
        val report = GuardReport(
            active = listOf(
                finding(GuardRisk.WIND, RiskStatus.ACTIVE, RiskLevel.HIGH),
                finding(GuardRisk.UV, RiskStatus.INSUFFICIENT, null),
            ),
            insufficient = listOf(finding(GuardRisk.UV, RiskStatus.INSUFFICIENT, null)),
            clearRisks = listOf(GuardRisk.PRECIPITATION),
            thunderstorm = finding(GuardRisk.THUNDERSTORM, RiskStatus.ACTIVE, RiskLevel.HIGH),
        )
        val notices = report.toNotices(city)
        assertEquals(listOf(GuardRisk.WIND, GuardRisk.THUNDERSTORM), notices.map { it.risk })
    }

    @Test
    fun `seçilmemiş risk motor sonucuna girmez`() {
        val hours = listOf(
            com.example.hava.planb.HourConditions(
                time = "2026-10-25T15:00",
                temperatureC = 20.0,
                windSpeedKmh = 80.0,
                uvIndex = 11.0,
                precipitationProbabilityPercent = 5,
                precipitationMm = 0.0,
                weatherCode = 1,
            ),
        )
        val report = RiskEngine().evaluate(hours, RiskSettings(enabled = setOf(UserRisk.WIND)))
        val risks = report.toNotices(city).map { it.risk }
        assertTrue(GuardRisk.WIND in risks)
        assertTrue(GuardRisk.UV !in risks)
    }

    private fun notice(
        level: RiskLevel = RiskLevel.CAUTION,
        validAt: String? = "2026-10-25T16:00",
        risk: GuardRisk = GuardRisk.WIND,
    ) = RiskNotice(
        cityId = city.id,
        cityName = city.name,
        latitude = city.latitude,
        longitude = city.longitude,
        countryCode = city.countryCode,
        risk = risk,
        level = level,
        validAt = validAt,
        reason = RiskReason.WIND_SPEED,
    )

    private fun finding(risk: GuardRisk, status: RiskStatus, level: RiskLevel?) = RiskFinding(
        risk = risk,
        status = status,
        level = level,
        validAt = "2026-10-25T16:00",
        measurements = emptyList(),
        thresholds = emptyList(),
        reason = if (risk == GuardRisk.THUNDERSTORM) RiskReason.THUNDERSTORM else RiskReason.WIND_SPEED,
        missing = if (status == RiskStatus.INSUFFICIENT) listOf(com.example.hava.planb.FactorKind.UV_INDEX) else emptyList(),
    )
}
