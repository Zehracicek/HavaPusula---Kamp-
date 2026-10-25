package com.example.hava.guard

import com.example.hava.data.remote.dto.City

/**
 * Bildirim kimliği: şehir, risk, seviye ve saat.
 * Aynı saatte aynı risk tekrarlanmaz. Seviye yükselirse yeni kimlik oluşur.
 * Saat, "yyyy-MM-ddTHH" biçiminde kesilir; dakikadaki küçük fark yeni bildirim açmaz.
 */
data class RiskNotice(
    val cityId: Long,
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val countryCode: String?,
    val risk: GuardRisk,
    val level: RiskLevel,
    val validAt: String?,
    val reason: RiskReason,
) {
    fun key(): String {
        val hour = validAt?.take(HOUR_PREFIX) ?: "unknown"
        return "$cityId|${risk.name}|${level.name}|$hour"
    }

    fun toCity(): City = City(
        id = cityId,
        name = cityName,
        latitude = latitude,
        longitude = longitude,
        countryCode = countryCode,
    )
}

fun GuardReport.toNotices(city: City): List<RiskNotice> {
    val findings = active + listOfNotNull(thunderstorm)
    return findings.mapNotNull { finding ->
        val level = finding.level ?: return@mapNotNull null
        if (finding.status != RiskStatus.ACTIVE) return@mapNotNull null
        RiskNotice(
            cityId = city.id,
            cityName = city.name,
            latitude = city.latitude,
            longitude = city.longitude,
            countryCode = city.countryCode,
            risk = finding.risk,
            level = level,
            validAt = finding.validAt,
            reason = finding.reason,
        )
    }
}

/** Daha önce gönderilmiş anahtarları eler. Aynı çalışmadaki kopyaları da teke indirir. */
fun unseenNotices(notices: List<RiskNotice>, alreadySent: Set<String>): List<RiskNotice> {
    val seen = HashSet<String>(alreadySent)
    return notices.filter { notice -> seen.add(notice.key()) }
}

private const val HOUR_PREFIX = 13
