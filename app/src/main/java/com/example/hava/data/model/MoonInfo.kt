package com.example.hava.data.model

data class MoonInfo(
    /** 0 yeni ay, 0.25 ilk dördün, 0.5 dolunay, 0.75 son dördün */
    val phase: Double,
    /** Aydınlanan yüzey oranı, 0..1 */
    val illumination: Double,
    val phaseName: MoonPhase,
    /** Şehrin yerel saatiyle "yyyy-MM-ddTHH:mm"; birkaç gün içinde doğmuyorsa null */
    val nextRise: String?,
    val nextSet: String?,
    /** Şehrin takvimine göre bugünden kaç gün sonra; 0 bugün */
    val daysUntilFullMoon: Int,
    val daysUntilNewMoon: Int,
)

enum class MoonPhase {
    NEW_MOON,
    WAXING_CRESCENT,
    FIRST_QUARTER,
    WAXING_GIBBOUS,
    FULL_MOON,
    WANING_GIBBOUS,
    LAST_QUARTER,
    WANING_CRESCENT,
}
