package com.example.hava.ui.screens.city

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hava.R
import com.example.hava.data.local.CountryCatalog
import com.example.hava.data.model.Country
import com.example.hava.data.model.CurrentConditions
import com.example.hava.data.model.DayPhase
import com.example.hava.data.model.WeatherCondition
import com.example.hava.data.remote.dto.City
import com.example.hava.ui.background.WeatherBackground
import com.example.hava.ui.background.WeatherScene
import com.example.hava.ui.components.CityCard
import com.example.hava.ui.components.CitySearchField
import com.example.hava.ui.components.CountrySelector
import com.example.hava.ui.components.EmptyView
import com.example.hava.ui.components.MyLocationButton
import com.example.hava.ui.components.PreviewData
import com.example.hava.ui.theme.HavaTheme
import kotlinx.coroutines.launch
import java.util.Locale

private val TurkishLocale = Locale.forLanguageTag("tr-TR")

// Kısa listelerde her harfe tek şehir düştüğü için başlıklar gürültü olur
private const val MIN_CITIES_FOR_LETTER_HEADERS = 20

@Composable
fun CityRoute(
    selectedCityId: Long?,
    onCitySelected: (City) -> Unit,
    onMyLocationClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: CityViewModel = viewModel(factory = CityViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshIfStale() }

    CityScreen(
        uiState = uiState,
        query = viewModel.query,
        onQueryChange = viewModel::onQueryChange,
        onCountrySelected = viewModel::onCountrySelected,
        selectedCityId = selectedCityId,
        onCityClick = { city ->
            // Ekrana bir sonraki girişte tam liste görünsün
            viewModel.onQueryChange("")
            onCitySelected(city)
        },
        onMyLocationClick = onMyLocationClick,
        onBack = onBack,
        onRetrySearch = viewModel::retrySearch,
        onRetryConditions = viewModel::retryConditions,
        favoriteIds = favoriteIds,
        onFavoriteClick = viewModel::toggleFavorite,
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

/** Arka plan çağıran tarafta çizilir; ekran saydamdır. */
@Composable
fun CityScreen(
    uiState: CityUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onCountrySelected: (Country) -> Unit,
    selectedCityId: Long?,
    onCityClick: (City) -> Unit,
    onMyLocationClick: () -> Unit,
    onBack: () -> Unit,
    onRetrySearch: () -> Unit,
    onRetryConditions: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    favoriteIds: Set<Long> = emptySet(),
    onFavoriteClick: (City) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val isTurkey = uiState.selectedCountry.code == CountryCatalog.TURKEY_CODE
    val scrollToTop: () -> Unit = { scope.launch { listState.scrollToItem(0) } }

    CompositionLocalProvider(LocalContentColor provides com.example.hava.ui.theme.Ink) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Text(
                    text = stringResource(R.string.choose_city),
                    style = MaterialTheme.typography.titleLarge.copy(),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() },
                )
            }

            CountrySelector(
                countries = uiState.countries,
                selected = uiState.selectedCountry,
                onSelect = { country ->
                    onCountrySelected(country)
                    scrollToTop()
                },
                contentPadding = PaddingValues(horizontal = 16.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CitySearchField(
                    query = query,
                    onQueryChange = { newQuery ->
                        onQueryChange(newQuery)
                        scrollToTop()
                    },
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.search_in_country, uiState.selectedCountry.name),
                    placeholder = stringResource(
                        if (isTurkey) R.string.search_placeholder_turkey else R.string.search_placeholder_country
                    ),
                )
                MyLocationButton(
                    onClick = {
                        focusManager.clearFocus()
                        onMyLocationClick()
                    },
                    // OutlinedTextField'ın üstündeki etiket boşluğuyla hizalamak için
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            // Sabit yükseklik: gösterge kaybolunca liste zıplamasın
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(2.dp),
            ) {
                if (uiState.conditions is ConditionsState.Loading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = LocalContentColor.current,
                        trackColor = LocalContentColor.current.copy(alpha = 0.2f),
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val conditions = uiState.conditions
                if (conditions is ConditionsState.Error) {
                    item(key = "conditionsError") {
                        InlineError(
                            message = stringResource(R.string.conditions_failed),
                            onRetry = onRetryConditions,
                        )
                    }
                }

                listedCityItems(
                    cities = uiState.cities,
                    showLetterHeaders = query.isBlank() && uiState.cities.size >= MIN_CITIES_FOR_LETTER_HEADERS,
                    conditions = (conditions as? ConditionsState.Success)?.byCityId.orEmpty(),
                    query = query,
                    selectedCityId = selectedCityId,
                    favoriteIds = favoriteIds,
                    onFavoriteClick = onFavoriteClick,
                    onCityClick = { city ->
                        focusManager.clearFocus()
                        onCityClick(city)
                    },
                )

                searchResultItems(
                    results = uiState.searchResults,
                    query = query,
                    selectedCityId = selectedCityId,
                    favoriteIds = favoriteIds,
                    onFavoriteClick = onFavoriteClick,
                    onCityClick = { city ->
                        focusManager.clearFocus()
                        onCityClick(city)
                    },
                    onRetry = onRetrySearch,
                )

                if (uiState.isEmpty) {
                    item(key = "empty") { EmptyView() }
                }
            }
        }
    }
}

private fun LazyListScope.listedCityItems(
    cities: List<City>,
    showLetterHeaders: Boolean,
    conditions: Map<Long, CurrentConditions>,
    query: String,
    selectedCityId: Long?,
    favoriteIds: Set<Long>,
    onFavoriteClick: (City) -> Unit,
    onCityClick: (City) -> Unit,
) {
    val groups = if (showLetterHeaders) {
        // Liste zaten Türkçe alfabeye göre sıralı; groupBy bu sırayı korur
        cities.groupBy { it.name.first().uppercase(TurkishLocale) }
    } else {
        mapOf("" to cities)
    }
    groups.forEach { (letter, citiesInGroup) ->
        if (letter.isNotEmpty()) {
            item(key = "letter-$letter") { LetterHeader(letter) }
        }
        items(items = citiesInGroup, key = { "city-${it.id}" }) { city ->
            CityCard(
                city = city,
                onClick = { onCityClick(city) },
                isSelected = city.id == selectedCityId,
                highlightQuery = query,
                conditions = conditions[city.id],
                isFavorite = city.id in favoriteIds,
                onFavoriteClick = { onFavoriteClick(city) },
            )
        }
    }
}

private fun LazyListScope.searchResultItems(
    results: SearchResults,
    query: String,
    selectedCityId: Long?,
    favoriteIds: Set<Long>,
    onFavoriteClick: (City) -> Unit,
    onCityClick: (City) -> Unit,
    onRetry: () -> Unit,
) {
    when (results) {
        SearchResults.Idle -> Unit
        SearchResults.Loading -> item(key = "searchLoading") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = LocalContentColor.current,
                    strokeWidth = 2.dp,
                )
                Text(
                    text = stringResource(R.string.searching_places),
                    style = MaterialTheme.typography.bodyMedium.copy(),
                )
            }
        }

        is SearchResults.Error -> item(key = "searchError") {
            InlineError(
                message = "${stringResource(R.string.search_failed)} ${stringResource(results.type.messageRes())}",
                onRetry = onRetry,
            )
        }

        is SearchResults.Success -> if (results.cities.isNotEmpty()) {
            item(key = "otherPlaces") {
                SectionTitle(
                    text = stringResource(R.string.other_places),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(items = results.cities, key = { "remote-${it.id}" }) { city ->
                CityCard(
                    city = city,
                    onClick = { onCityClick(city) },
                    isSelected = city.id == selectedCityId,
                    highlightQuery = query,
                    isFavorite = city.id in favoriteIds,
                    onFavoriteClick = { onFavoriteClick(city) },
                )
            }
        }
    }
}

@Composable
private fun LetterHeader(letter: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 4.dp)
            .semantics { heading() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.titleLarge.copy(),
            fontWeight = FontWeight.Bold,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = LocalContentColor.current.copy(alpha = 0.35f),
        )
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.semantics { heading() },
        style = MaterialTheme.typography.labelLarge.copy(),
        color = LocalContentColor.current.copy(alpha = 0.9f),
    )
}

@Composable
private fun InlineError(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(),
            modifier = Modifier.weight(1f),
        )
        // TextButton içinde LocalContentColor butonun kendi rengine döner; dış renk burada aktarılır
        TextButton(
            onClick = onRetry,
            colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current),
        ) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun CityScreenPreviewContainer(content: @Composable () -> Unit) {
    HavaTheme {
        Box {
            WeatherBackground(scene = WeatherScene(WeatherCondition.PARTLY_CLOUDY, DayPhase.DAY))
            content()
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun CityScreenTurkeyPreview() {
    CityScreenPreviewContainer {
        CityScreen(
            uiState = CityUiState(
                countries = CountryCatalog.countries,
                selectedCountry = CountryCatalog.countries.first(),
                cities = CountryCatalog.citiesOf(CountryCatalog.TURKEY_CODE),
                conditions = ConditionsState.Success(PreviewData.conditions),
            ),
            query = "",
            onQueryChange = {},
            onCountrySelected = {},
            selectedCityId = 6,
            onCityClick = {},
            onMyLocationClick = {},
            onBack = {},
            onRetrySearch = {},
            onRetryConditions = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CityScreenSearchPreview() {
    CityScreenPreviewContainer {
        val ankara = CountryCatalog.citiesOf(CountryCatalog.TURKEY_CODE).first { it.id == 6L }
        CityScreen(
            uiState = CityUiState(
                countries = CountryCatalog.countries,
                selectedCountry = CountryCatalog.countries.first(),
                cities = listOf(ankara),
                searchResults = SearchResults.Success(
                    listOf(City(id = 1, name = "Çankaya", latitude = 39.9, longitude = 32.86, countryCode = "TR", region = "Ankara"))
                ),
                conditions = ConditionsState.Error(ErrorType.NETWORK),
            ),
            query = "an",
            onQueryChange = {},
            onCountrySelected = {},
            selectedCityId = null,
            onCityClick = {},
            onMyLocationClick = {},
            onBack = {},
            onRetrySearch = {},
            onRetryConditions = {},
        )
    }
}
