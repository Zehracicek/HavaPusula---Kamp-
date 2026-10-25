package com.example.hava.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hava.data.local.CountryCatalog
import com.example.hava.data.model.Country
import com.example.hava.ui.theme.HavaTheme

@Composable
fun CountrySelector(
    countries: List<Country>,
    selected: Country,
    onSelect: (Country) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val listState = rememberLazyListState()
    // Ekran yeniden açıldığında seçili ülke görünür olsun
    LaunchedEffect(Unit) {
        val index = countries.indexOf(selected)
        if (index > 0) listState.scrollToItem(index)
    }

    LazyRow(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = countries, key = { it.code }) { country ->
            FilterChip(
                selected = country == selected,
                onClick = { onSelect(country) },
                label = { Text("${flagEmoji(country.code)}  ${country.name}") },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = country == selected,
                    borderColor = Color.Transparent,
                ),
            )
        }
    }
}

/** "TR" → 🇹🇷; bayraklar iki bölgesel gösterge harfinden oluşur. */
internal fun flagEmoji(countryCode: String): String =
    countryCode.uppercase().map { char -> String(Character.toChars(0x1F1E6 + (char - 'A'))) }
        .joinToString("")

@Preview(showBackground = true, backgroundColor = 0xFF1A73E8)
@Composable
private fun CountrySelectorPreview() {
    HavaTheme {
        CountrySelector(
            countries = CountryCatalog.countries,
            selected = CountryCatalog.countries.first(),
            onSelect = {},
        )
    }
}
