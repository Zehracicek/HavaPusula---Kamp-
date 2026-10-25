package com.example.hava.guard

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Periyodik kontrol. WorkManager işi tam istenen saatte çalıştırmaz;
 * cihaz uyurken ve ağ yokken erteleyebilir.
 */
object WeatherGuardScheduler {

    private const val WORK_NAME = "weatherguard_favorites"
    private const val INTERVAL_HOURS = 6L

    fun ensureScheduled(context: Context) {
        val request = PeriodicWorkRequestBuilder<WeatherGuardWorker>(INTERVAL_HOURS, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(WORK_NAME)
    }
}
