package com.example.hava.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hava.R
import com.example.hava.ui.theme.CardBorder
import com.example.hava.ui.theme.Ink

/** Animasyonlu arka plan üzerinde okunabilir, yarı saydam bilgi kartı. */
@Composable
fun WeatherCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    containerColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        contentColor = Ink,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, CardBorder),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
            }
            content()
        }
    }
}

@Composable
internal fun ForecastUnavailable() {
    Text(
        text = stringResource(R.string.forecast_unavailable),
        style = MaterialTheme.typography.bodyMedium,
        color = LocalContentColor.current.copy(alpha = 0.8f),
    )
}
