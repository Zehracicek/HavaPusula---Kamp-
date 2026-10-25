package com.example.hava.ui.background

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import com.example.hava.data.model.DayPhase
import com.example.hava.data.model.WeatherCondition

private const val TRANSITION_MILLIS = 1600

private fun <T> sceneTransition() = tween<T>(TRANSITION_MILLIS, easing = FastOutSlowInEasing)

/**
 * Hava durumuna ve günün saatine göre canlanan arka plan.
 * Sahne değiştiğinde renkler ve katmanların yoğunluğu yumuşakça geçiş yapar;
 * görünmeyen katmanlar kompozisyondan tamamen çıkarılır.
 */
@Composable
fun WeatherBackground(scene: WeatherScene, modifier: Modifier = Modifier) {
    val target = remember(scene) { scene.toSkySpec() }
    val animationsEnabled = rememberSystemAnimationsEnabled()
    val clock = rememberAnimationClock(enabled = animationsEnabled)
    val time = { clock.value }

    val skyTop = animateColorAsState(target.skyTop, sceneTransition(), label = "skyTop")
    val skyMiddle = animateColorAsState(target.skyMiddle, sceneTransition(), label = "skyMiddle")
    val skyBottom = animateColorAsState(target.skyBottom, sceneTransition(), label = "skyBottom")
    val sunAlpha = animateFloatAsState(target.sunAlpha, sceneTransition(), label = "sunAlpha")
    val sunColor = animateColorAsState(target.sunColor, sceneTransition(), label = "sunColor")
    val sunHeight = animateFloatAsState(target.sunHeight, sceneTransition(), label = "sunHeight")
    val moonAlpha = animateFloatAsState(target.moonAlpha, sceneTransition(), label = "moonAlpha")
    val starsAlpha = animateFloatAsState(target.starsAlpha, sceneTransition(), label = "starsAlpha")
    val cloudCover = animateFloatAsState(target.cloudCover, sceneTransition(), label = "cloudCover")
    val cloudColor = animateColorAsState(target.cloudColor, sceneTransition(), label = "cloudColor")
    val rain = animateFloatAsState(target.rainIntensity, sceneTransition(), label = "rain")
    val snow = animateFloatAsState(target.snowIntensity, sceneTransition(), label = "snow")
    val haze = animateFloatAsState(target.hazeAlpha, sceneTransition(), label = "haze")
    // Animasyonlar kapalıysa yanıp sönen efekt gösterilmez
    val lightning = animateFloatAsState(
        targetValue = if (target.lightning && animationsEnabled) 1f else 0f,
        animationSpec = sceneTransition(),
        label = "lightning",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(Brush.verticalGradient(listOf(skyTop.value, skyMiddle.value, skyBottom.value)))
            }
    ) {
        LayerIfVisible(starsAlpha) { StarLayer(time) { starsAlpha.value } }
        LayerIfVisible(moonAlpha) { MoonLayer(time) { moonAlpha.value } }
        LayerIfVisible(sunAlpha) {
            SunLayer(
                time = time,
                alpha = { sunAlpha.value },
                color = { sunColor.value },
                height = { sunHeight.value },
            )
        }
        LayerIfVisible(cloudCover) {
            CloudLayer(time = time, cover = { cloudCover.value }, color = { cloudColor.value })
        }
        LayerIfVisible(haze) { HazeLayer { haze.value } }
        LayerIfVisible(rain) { RainLayer(time) { rain.value } }
        LayerIfVisible(snow) { SnowLayer(time) { snow.value } }
        LayerIfVisible(lightning) { LightningLayer { lightning.value } }
        ReadabilityScrim()
    }
}

@Composable
private fun LayerIfVisible(alpha: State<Float>, content: @Composable () -> Unit) {
    val visible by remember(alpha) { derivedStateOf { alpha.value > 0.005f } }
    if (visible) content()
}

@Preview(name = "Güneşli", widthDp = 360, heightDp = 640)
@Composable
private fun SunnyBackgroundPreview() {
    WeatherBackground(WeatherScene(WeatherCondition.CLEAR, DayPhase.DAY))
}

@Preview(name = "Parçalı bulutlu", widthDp = 360, heightDp = 640)
@Composable
private fun PartlyCloudyBackgroundPreview() {
    WeatherBackground(WeatherScene(WeatherCondition.PARTLY_CLOUDY, DayPhase.DAY))
}

@Preview(name = "Fırtına", widthDp = 360, heightDp = 640)
@Composable
private fun StormBackgroundPreview() {
    WeatherBackground(WeatherScene(WeatherCondition.STORM, DayPhase.DAY, intensity = 1f))
}

@Preview(name = "Karlı", widthDp = 360, heightDp = 640)
@Composable
private fun SnowBackgroundPreview() {
    WeatherBackground(WeatherScene(WeatherCondition.SNOW, DayPhase.DAY, intensity = 0.8f))
}

@Preview(name = "Gece", widthDp = 360, heightDp = 640)
@Composable
private fun NightBackgroundPreview() {
    WeatherBackground(WeatherScene(WeatherCondition.CLEAR, DayPhase.NIGHT))
}

@Preview(name = "Gün batımı", widthDp = 360, heightDp = 640)
@Composable
private fun SunsetBackgroundPreview() {
    WeatherBackground(WeatherScene(WeatherCondition.PARTLY_CLOUDY, DayPhase.SUNSET))
}
