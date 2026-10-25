package com.example.hava.ui.background

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

/**
 * Tüm arka plan katmanlarının paylaştığı tek zaman kaynağı (saniye).
 * Değer yalnızca çizim aşamasında okunmalı; böylece her karede recomposition değil
 * sadece yeniden çizim tetiklenir. Uygulama arka plandayken saat durur.
 */
@Composable
internal fun rememberAnimationClock(enabled: Boolean): State<Float> {
    val time = remember { mutableFloatStateOf(0f) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(enabled, lifecycleOwner) {
        if (!enabled) return@LaunchedEffect
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            val base = time.floatValue
            val start = withFrameNanos { it }
            while (true) {
                withFrameNanos { frame ->
                    time.floatValue = base + (frame - start) / 1_000_000_000f
                }
            }
        }
    }
    return time
}

/** Geliştirici seçeneklerinde/erişilebilirlikte animasyonlar kapatıldıysa false döner. */
@Composable
internal fun rememberSystemAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) > 0f
    }
}
