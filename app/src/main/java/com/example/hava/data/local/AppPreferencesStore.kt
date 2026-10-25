package com.example.hava.data.local

import android.content.Context
import com.example.hava.data.remote.dto.City
import com.example.hava.guard.RiskSettings
import com.example.hava.guard.UserRisk
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Risk tercihleri ve favori şehirler. Yeni bir veritabanı yok.
 * Sıcaklık °C, rüzgâr km/h, yağış mm, olasılık %, UV indeksi olarak saklanır.
 */
class AppPreferencesStore(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun riskSettings(): RiskSettings = RiskSettings(
        enabled = UserRisk.entries.filter { preferences.getBoolean(it.prefKey(), it.defaultEnabled()) }.toSet(),
        precipProbabilityCautionPercent = preferences.getInt(KEY_PRECIP_PROB_CAUTION, 60),
        precipProbabilityHighPercent = preferences.getInt(KEY_PRECIP_PROB_HIGH, 80),
        precipAmountCautionMm = preferences.getFloat(KEY_PRECIP_AMOUNT_CAUTION, 1f).toDouble(),
        precipAmountHighMm = preferences.getFloat(KEY_PRECIP_AMOUNT_HIGH, 4f).toDouble(),
        windCautionKmh = preferences.getFloat(KEY_WIND_CAUTION, 40f).toDouble(),
        windHighKmh = preferences.getFloat(KEY_WIND_HIGH, 50f).toDouble(),
        gustCautionKmh = preferences.getFloat(KEY_GUST_CAUTION, 55f).toDouble(),
        gustHighKmh = preferences.getFloat(KEY_GUST_HIGH, 70f).toDouble(),
        heatCautionC = preferences.getFloat(KEY_HEAT_CAUTION, 32f).toDouble(),
        heatHighC = preferences.getFloat(KEY_HEAT_HIGH, 36f).toDouble(),
        frostCautionC = preferences.getFloat(KEY_FROST_CAUTION, 2f).toDouble(),
        frostHighC = preferences.getFloat(KEY_FROST_HIGH, 0f).toDouble(),
        uvCaution = preferences.getFloat(KEY_UV_CAUTION, 6f).toDouble(),
        uvHigh = preferences.getFloat(KEY_UV_HIGH, 8f).toDouble(),
    )

    fun saveRiskSettings(settings: RiskSettings) {
        preferences.edit()
            .apply {
                UserRisk.entries.forEach { putBoolean(it.prefKey(), it in settings.enabled) }
            }
            .putInt(KEY_PRECIP_PROB_CAUTION, settings.precipProbabilityCautionPercent)
            .putInt(KEY_PRECIP_PROB_HIGH, settings.precipProbabilityHighPercent)
            .putFloat(KEY_PRECIP_AMOUNT_CAUTION, settings.precipAmountCautionMm.toFloat())
            .putFloat(KEY_PRECIP_AMOUNT_HIGH, settings.precipAmountHighMm.toFloat())
            .putFloat(KEY_WIND_CAUTION, settings.windCautionKmh.toFloat())
            .putFloat(KEY_WIND_HIGH, settings.windHighKmh.toFloat())
            .putFloat(KEY_GUST_CAUTION, settings.gustCautionKmh.toFloat())
            .putFloat(KEY_GUST_HIGH, settings.gustHighKmh.toFloat())
            .putFloat(KEY_HEAT_CAUTION, settings.heatCautionC.toFloat())
            .putFloat(KEY_HEAT_HIGH, settings.heatHighC.toFloat())
            .putFloat(KEY_FROST_CAUTION, settings.frostCautionC.toFloat())
            .putFloat(KEY_FROST_HIGH, settings.frostHighC.toFloat())
            .putFloat(KEY_UV_CAUTION, settings.uvCaution.toFloat())
            .putFloat(KEY_UV_HIGH, settings.uvHigh.toFloat())
            .apply()
    }

    fun favoriteCities(): List<City> = decodeFavorites(preferences.getString(KEY_FAVORITES, null))

    fun isFavorite(cityId: Long): Boolean = favoriteCities().any { it.id == cityId }

    fun setFavorite(city: City, favorite: Boolean) {
        val current = favoriteCities().filterNot { it.id == city.id }
        val updated = if (favorite) current + city else current
        preferences.edit().putString(KEY_FAVORITES, encodeFavorites(updated)).apply()
    }

    fun notificationsEnabled(): Boolean = preferences.getBoolean(KEY_NOTIFICATIONS, false)

    fun setNotificationsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
    }

    fun sentNoticeKeys(): Set<String> = decodeNoticeKeys(preferences.getString(KEY_SENT_NOTICES, null)).toSet()

    fun rememberNoticeKeys(keys: Collection<String>) {
        if (keys.isEmpty()) return
        val merged = (decodeNoticeKeys(preferences.getString(KEY_SENT_NOTICES, null)) + keys)
            .distinct()
            .takeLast(MAX_SENT_NOTICES)
        preferences.edit().putString(KEY_SENT_NOTICES, gson.toJson(merged)).apply()
    }

    private fun decodeFavorites(json: String?): List<City> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<City>>(json, object : TypeToken<List<City>>() {}.type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    private fun decodeNoticeKeys(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<String>>(json, object : TypeToken<List<String>>() {}.type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    private fun encodeFavorites(cities: List<City>): String = gson.toJson(cities)

    /** Hamle eskiden rüzgâr tercihine bağlıydı; ayrı anahtar yoksa o tercih devralınır. */
    private fun UserRisk.defaultEnabled(): Boolean =
        if (this == UserRisk.GUST) preferences.getBoolean(KEY_ENABLE_WIND, true) else true

    private fun UserRisk.prefKey(): String = when (this) {
        UserRisk.PRECIPITATION -> KEY_ENABLE_PRECIP
        UserRisk.WIND -> KEY_ENABLE_WIND
        UserRisk.GUST -> KEY_ENABLE_GUST
        UserRisk.HEAT -> KEY_ENABLE_HEAT
        UserRisk.FROST -> KEY_ENABLE_FROST
        UserRisk.UV -> KEY_ENABLE_UV
    }

    private companion object {
        const val FILE = "hava_prefs"
        const val KEY_FAVORITES = "favorite_cities"
        const val KEY_ENABLE_PRECIP = "risk_precipitation"
        const val KEY_ENABLE_WIND = "risk_wind"
        const val KEY_ENABLE_GUST = "risk_gust"
        const val KEY_ENABLE_HEAT = "risk_heat"
        const val KEY_ENABLE_FROST = "risk_frost"
        const val KEY_ENABLE_UV = "risk_uv"
        const val KEY_PRECIP_PROB_CAUTION = "precip_probability_caution_percent"
        const val KEY_PRECIP_PROB_HIGH = "precip_probability_high_percent"
        const val KEY_PRECIP_AMOUNT_CAUTION = "precip_amount_caution_mm"
        const val KEY_PRECIP_AMOUNT_HIGH = "precip_amount_high_mm"
        const val KEY_WIND_CAUTION = "wind_caution_kmh"
        const val KEY_WIND_HIGH = "wind_high_kmh"
        const val KEY_GUST_CAUTION = "gust_caution_kmh"
        const val KEY_GUST_HIGH = "gust_high_kmh"
        const val KEY_HEAT_CAUTION = "heat_caution_c"
        const val KEY_HEAT_HIGH = "heat_high_c"
        const val KEY_FROST_CAUTION = "frost_caution_c"
        const val KEY_FROST_HIGH = "frost_high_c"
        const val KEY_UV_CAUTION = "uv_caution"
        const val KEY_UV_HIGH = "uv_high"
        const val KEY_NOTIFICATIONS = "notifications_enabled"
        const val KEY_SENT_NOTICES = "sent_notice_keys"
        const val MAX_SENT_NOTICES = 200
    }
}
