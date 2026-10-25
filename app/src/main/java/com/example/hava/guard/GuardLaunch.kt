package com.example.hava.guard

import android.content.Intent
import com.example.hava.data.remote.dto.City

data class GuardLaunchRequest(
    val city: City,
    val token: Long,
)

fun Intent.toGuardLaunchRequest(): GuardLaunchRequest? {
    if (!hasExtra(WeatherNotifications.EXTRA_TOKEN)) return null
    val id = getLongExtra(WeatherNotifications.EXTRA_CITY_ID, Long.MIN_VALUE)
    val name = getStringExtra(WeatherNotifications.EXTRA_CITY_NAME)
    if (id == Long.MIN_VALUE || name.isNullOrBlank()) return null
    if (!hasExtra(WeatherNotifications.EXTRA_LATITUDE) || !hasExtra(WeatherNotifications.EXTRA_LONGITUDE)) return null
    return GuardLaunchRequest(
        city = City(
            id = id,
            name = name,
            latitude = getDoubleExtra(WeatherNotifications.EXTRA_LATITUDE, 0.0),
            longitude = getDoubleExtra(WeatherNotifications.EXTRA_LONGITUDE, 0.0),
            countryCode = getStringExtra(WeatherNotifications.EXTRA_COUNTRY_CODE),
        ),
        token = getLongExtra(WeatherNotifications.EXTRA_TOKEN, 0L),
    )
}
