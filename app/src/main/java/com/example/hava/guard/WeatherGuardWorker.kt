package com.example.hava.guard

import android.content.Context
import androidx.annotation.Keep
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result as WorkResult
import androidx.work.WorkerParameters
import com.example.hava.data.local.AppPreferencesStore
import com.example.hava.data.repository.WeatherRepositoryImpl

/**
 * Favori şehirleri kontrol eder. Tüm şehir kataloğunu veya konumu taramaz.
 * Seçilmemiş risk türleri kişisel bildirime girmez. Gök gürültüsü kodu genel güvenlik olarak kalır.
 * Resmî uyarı dağıtımı bu işin kapsamında değildir; o ayrı bir sunucu ister.
 */
@Keep
class WeatherGuardWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): WorkResult {
        val preferences = AppPreferencesStore(applicationContext)
        if (!preferences.notificationsEnabled()) return WorkResult.success()
        if (!WeatherNotifications.canPost(applicationContext)) return WorkResult.success()

        val favorites = preferences.favoriteCities()
        if (favorites.isEmpty()) return WorkResult.success()

        val settings = preferences.riskSettings()
        val repository = WeatherRepositoryImpl()
        val engine = RiskEngine()
        val notices = mutableListOf<RiskNotice>()
        var anySuccess = false
        var anyFailure = false

        for (city in favorites) {
            val result = repository.getWeather(city)
            if (result.isFailure) {
                anyFailure = true
                continue
            }
            anySuccess = true
            val report = result.getOrThrow().toGuardHours()
            notices += engine.evaluate(report, settings).toNotices(city)
        }

        if (!anySuccess && anyFailure) return WorkResult.retry()

        val unseen = unseenNotices(notices, preferences.sentNoticeKeys()).take(MAX_PER_RUN)
        if (unseen.isNotEmpty()) {
            WeatherNotifications.show(applicationContext, unseen)
            preferences.rememberNoticeKeys(unseen.map { it.key() })
        }
        return WorkResult.success()
    }

    private companion object {
        const val MAX_PER_RUN = 6
    }
}
