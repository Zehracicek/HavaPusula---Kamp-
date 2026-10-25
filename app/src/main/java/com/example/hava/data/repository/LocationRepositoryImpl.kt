package com.example.hava.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.hava.R
import com.example.hava.data.remote.dto.City
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

class LocationRepositoryImpl(context: Context) : LocationRepository {

    private val context = context.applicationContext
    private val locationManager = context.getSystemService(LocationManager::class.java)

    override suspend fun getCurrentCity(): Result<City> = resultOf {
        if (!context.hasLocationPermission()) throw SecurityException("Konum izni verilmedi")
        val location = findLocation() ?: throw LocationUnavailableException()
        val address = withContext(Dispatchers.IO) { reverseGeocode(location) }
        City(
            id = MY_LOCATION_CITY_ID,
            name = address?.let { it.locality ?: it.subAdminArea ?: it.adminArea }
                ?: context.getString(R.string.my_location),
            latitude = location.latitude,
            longitude = location.longitude,
            country = address?.countryName,
            countryCode = address?.countryCode,
            region = address?.adminArea,
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun findLocation(): Location? {
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) return null

        val providers = locationManager.getProviders(true)
        val lastKnown = providers
            .mapNotNull { runCatching { locationManager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.elapsedRealtimeNanos }
        if (lastKnown != null && lastKnown.ageMillis() < MAX_LOCATION_AGE_MS) return lastKnown

        val provider = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .firstOrNull { it in providers }
            ?: providers.firstOrNull()
            ?: return lastKnown

        val fresh = withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            suspendCancellableCoroutine<Location?> { cont ->
                val signal = CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                LocationManagerCompat.getCurrentLocation(
                    locationManager,
                    provider,
                    signal,
                    ContextCompat.getMainExecutor(context),
                ) { cont.resume(it) }
            }
        }
        return fresh ?: lastKnown
    }

    // Geocoder bazı cihazlarda (Google servisleri olmayan) çalışmaz; isim yoksa koordinat yine kullanılır
    @Suppress("DEPRECATION")
    private fun reverseGeocode(location: Location): Address? {
        if (!Geocoder.isPresent()) return null
        return runCatching {
            Geocoder(context, Locale.forLanguageTag("tr-TR"))
                .getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()
        }.getOrNull()
    }

    private fun Location.ageMillis(): Long =
        (SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos) / 1_000_000

    companion object {
        const val MY_LOCATION_CITY_ID = -1L
        private const val MAX_LOCATION_AGE_MS = 10 * 60 * 1000L
        private const val LOCATION_TIMEOUT_MS = 15_000L
    }
}

fun Context.hasLocationPermission(): Boolean =
    listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        .any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
