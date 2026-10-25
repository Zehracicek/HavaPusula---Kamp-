package com.example.hava.ui.components

import java.text.SimpleDateFormat
import java.util.Locale

private val TurkishLocale = Locale.forLanguageTag("tr-TR")

/** "yyyy-MM-ddTHH:mm" → "HH:mm" */
internal fun hourLabel(isoTime: String): String = isoTime.substringAfter('T').take(5)

/** "yyyy-MM-dd" → "Pzt"; minSdk 24'te java.time olmadığı için SimpleDateFormat kullanılıyor. */
internal fun shortDayName(isoDate: String): String = runCatching {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate)
    SimpleDateFormat("EEE", TurkishLocale).format(requireNotNull(date))
}.getOrDefault(isoDate)

/** "yyyy-MM-dd" → "Pazartesi, 26 Ekim" */
internal fun fullDayTitle(isoDate: String): String = runCatching {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate)
    SimpleDateFormat("EEEE, d MMMM", TurkishLocale).format(requireNotNull(date))
}.getOrDefault(isoDate)

/** Türkçe ondalık ayırıcıyla tek basamaklı sayı: 0.4 → "0,4" */
internal fun oneDecimal(value: Double): String = String.format(TurkishLocale, "%.1f", value)

/** Metre → km; uzak mesafelerde ondalık gösterilmez. */
internal fun visibilityKm(meters: Double): String {
    val km = meters / 1000
    return if (km >= 10) km.toInt().toString() else oneDecimal(km)
}

/** 8 ana yönden hangisine düştüğü; compass_directions dizisindeki sıra. */
internal fun compassIndex(degrees: Int): Int = (((degrees % 360 + 360) % 360 + 22) / 45) % 8

private val BeaufortUpperBoundsKmh = intArrayOf(1, 6, 12, 20, 29, 39, 50, 62, 75, 89, 103, 118)

/** km/h → Beaufort ölçeği (0..12) */
internal fun beaufortScale(speedKmh: Double): Int = BeaufortUpperBoundsKmh.count { speedKmh >= it }

/** UV indeksi → uv_levels dizisindeki sıra (Düşük..Aşırı) */
internal fun uvLevelIndex(uvIndex: Double): Int = when {
    uvIndex < 3 -> 0
    uvIndex < 6 -> 1
    uvIndex < 8 -> 2
    uvIndex < 11 -> 3
    else -> 4
}
