package com.example.hava.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.hava.R
import java.util.Locale
import kotlin.math.roundToInt

private val MapHeight = 220.dp

@Composable
fun PrecipitationMapCard(
    latitude: Double,
    longitude: Double,
    temperature: Double,
    modifier: Modifier = Modifier,
) {
    val html = precipitationMapHtml(latitude, longitude, temperature.roundToInt())
    WeatherCard(modifier = modifier, title = stringResource(R.string.precipitation_map)) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(MapHeight)
                .clip(RoundedCornerShape(16.dp)),
            factory = { context ->
                mapWebView(context).also {
                    it.tag = mapKey(latitude, longitude, temperature)
                    it.loadMap(html)
                }
            },
            update = { webView ->
                val key = mapKey(latitude, longitude, temperature)
                if (webView.tag != key) {
                    webView.tag = key
                    webView.loadMap(html)
                }
            },
            onRelease = { it.stopLoading() },
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun mapWebView(context: android.content.Context): WebView =
    WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setSupportZoom(false)
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        overScrollMode = android.view.View.OVER_SCROLL_NEVER
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        webViewClient = WebViewClient()
    }

private fun mapKey(latitude: Double, longitude: Double, temperature: Double): String =
    "${latitude},${longitude},${temperature.roundToInt()}"

private fun WebView.loadMap(html: String) {
    loadDataWithBaseURL("https://localhost/", html, "text/html", Charsets.UTF_8.name(), null)
}

private fun precipitationMapHtml(latitude: Double, longitude: Double, temperature: Int): String {
    val lat = String.format(Locale.US, "%.5f", latitude)
    val lon = String.format(Locale.US, "%.5f", longitude)
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"/>
          <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
          <style>
            html, body, #map { height: 100%; margin: 0; background: #e8eef3; }
            .leaflet-control-attribution { font-size: 8px; }
            .temp-pin {
              background: #2b2f36; color: #fff; border-radius: 50%;
              width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;
              font: 600 13px/1 -apple-system, sans-serif; border: 2px solid #fff;
              box-shadow: 0 2px 6px rgba(0,0,0,.25);
            }
          </style>
        </head>
        <body>
        <div id="map"></div>
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <script>
          const map = L.map('map', {
            zoomControl: false, dragging: false, scrollWheelZoom: false,
            doubleClickZoom: false, boxZoom: false, keyboard: false, touchZoom: false
          }).setView([$lat, $lon], 7);
          const base = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}', {
            attribution: 'Esri',
            maxZoom: 18
          });
          base.on('tileerror', function (event) { event.tile.style.display = 'none'; });
          base.addTo(map);
          setTimeout(function () { map.invalidateSize(); }, 200);
          L.marker([$lat, $lon], {
            interactive: false,
            icon: L.divIcon({
              className: '',
              html: '<div class="temp-pin">${temperature}°</div>',
              iconSize: [36, 36], iconAnchor: [18, 18]
            })
          }).addTo(map);
          fetch('https://api.rainviewer.com/public/weather-maps.json')
            .then(r => r.json())
            .then(data => {
              const frames = (data.radar && data.radar.past) || [];
              const last = frames[frames.length - 1];
              if (!last) return;
              const radar = L.tileLayer(data.host + last.path + '/256/{z}/{x}/{y}/2/1_1.png', { opacity: 0.65 });
              radar.on('tileerror', function (event) { event.tile.style.display = 'none'; });
              radar.addTo(map);
            });
        </script>
        </body>
        </html>
    """.trimIndent()
}
