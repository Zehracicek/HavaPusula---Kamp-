package com.example.hava

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.hava.data.local.AppPreferencesStore
import com.example.hava.guard.GuardLaunchRequest
import com.example.hava.guard.WeatherGuardScheduler
import com.example.hava.guard.toGuardLaunchRequest
import com.example.hava.ui.HavaApp
import com.example.hava.ui.theme.HavaTheme

class MainActivity : ComponentActivity() {

    private var guardLaunch by mutableStateOf<GuardLaunchRequest?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        guardLaunch = intent.toGuardLaunchRequest()
        if (AppPreferencesStore(this).notificationsEnabled()) {
            WeatherGuardScheduler.ensureScheduled(this)
        }
        // Arka plan her zaman koyu/doygun tonlarda olduğu için sistem çubuğu ikonları açık renk
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
        )
        setContent {
            HavaTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                ) { innerPadding ->
                    HavaApp(
                        contentPadding = innerPadding,
                        guardLaunch = guardLaunch,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        guardLaunch = intent.toGuardLaunchRequest()
    }
}
