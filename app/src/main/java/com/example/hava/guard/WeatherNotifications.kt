package com.example.hava.guard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.hava.MainActivity
import com.example.hava.R

object WeatherNotifications {

    const val CHANNEL_ID = "weatherguard_risks"
    const val EXTRA_TOKEN = "guard_token"
    const val EXTRA_CITY_ID = "guard_city_id"
    const val EXTRA_CITY_NAME = "guard_city_name"
    const val EXTRA_LATITUDE = "guard_latitude"
    const val EXTRA_LONGITUDE = "guard_longitude"
    const val EXTRA_COUNTRY_CODE = "guard_country_code"

    fun canPost(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
            if (granted != android.content.pm.PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    fun show(context: Context, notices: List<RiskNotice>) {
        if (notices.isEmpty() || !canPost(context)) return
        ensureChannel(context)
        val manager = NotificationManagerCompat.from(context)
        for (notice in notices) {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.guard_notification_title, notice.cityName, context.getString(notice.risk.titleRes())))
                .setContentText(body(context, notice))
                .setStyle(NotificationCompat.BigTextStyle().bigText(body(context, notice)))
                .setContentIntent(openGuard(context, notice))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            manager.notify(notice.key().hashCode(), notification)
        }
    }

    private fun body(context: Context, notice: RiskNotice): String {
        val whenText = notice.validAt?.substringAfter('T')?.take(5)
            ?.let { context.getString(R.string.guard_valid_at, it) }
            ?: context.getString(R.string.guard_observed_unknown)
        val action = context.getString(notice.reason.actionRes())
        val level = context.getString(notice.level.labelRes())
        return context.getString(R.string.guard_notification_body, level, whenText, action)
    }

    private fun openGuard(context: Context, notice: RiskNotice): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TOKEN, System.currentTimeMillis())
            putExtra(EXTRA_CITY_ID, notice.cityId)
            putExtra(EXTRA_CITY_NAME, notice.cityName)
            putExtra(EXTRA_LATITUDE, notice.latitude)
            putExtra(EXTRA_LONGITUDE, notice.longitude)
            putExtra(EXTRA_COUNTRY_CODE, notice.countryCode)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, notice.key().hashCode(), intent, flags)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.guard_notification_channel),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.guard_notification_channel_desc)
        }
        manager.createNotificationChannel(channel)
    }
}

private fun GuardRisk.titleRes(): Int = when (this) {
    GuardRisk.PRECIPITATION -> R.string.guard_risk_precip
    GuardRisk.WIND -> R.string.guard_risk_wind
    GuardRisk.GUST -> R.string.guard_risk_gust
    GuardRisk.HEAT -> R.string.guard_risk_heat
    GuardRisk.FROST -> R.string.guard_risk_frost
    GuardRisk.UV -> R.string.guard_risk_uv
    GuardRisk.THUNDERSTORM -> R.string.guard_risk_storm
}

private fun RiskLevel.labelRes(): Int = when (this) {
    RiskLevel.CAUTION -> R.string.guard_level_caution
    RiskLevel.HIGH -> R.string.guard_level_high
}

private fun RiskReason.actionRes(): Int = when (this) {
    RiskReason.PROBABILITY_ONLY -> R.string.guard_action_probability
    RiskReason.AMOUNT_ONLY -> R.string.guard_action_amount
    RiskReason.PROBABILITY_AND_AMOUNT -> R.string.guard_action_both
    RiskReason.WIND_SPEED -> R.string.guard_action_wind
    RiskReason.GUST -> R.string.guard_action_gust
    RiskReason.HEAT -> R.string.guard_action_heat
    RiskReason.FROST -> R.string.guard_action_frost
    RiskReason.UV -> R.string.guard_action_uv
    RiskReason.THUNDERSTORM -> R.string.guard_action_storm
    RiskReason.MISSING_DATA, RiskReason.BELOW_THRESHOLD -> R.string.guard_no_exceedance
}
