package com.example.hava.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hava.R
import com.example.hava.ui.theme.HavaTheme
import com.example.hava.ui.theme.Ink

private val ErrorTitleColor = Color(0xFFC23B4A)

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    CenteredColumn(modifier) {
        CircularProgressIndicator()
        Text(stringResource(R.string.loading))
    }
}

@Composable
fun EmptyView(modifier: Modifier = Modifier) {
    CenteredColumn(modifier) {
        Text(
            text = stringResource(R.string.city_not_found),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenteredColumn(modifier) {
        Text(
            text = stringResource(R.string.error_title),
            style = MaterialTheme.typography.titleMedium,
            color = ErrorTitleColor,
        )
        Text(
            text = message,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun CenteredColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            contentColor = Ink,
            shadowElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorViewPreview() {
    HavaTheme {
        ErrorView(message = "İnternet bağlantınızı kontrol edin.", onRetry = {})
    }
}
