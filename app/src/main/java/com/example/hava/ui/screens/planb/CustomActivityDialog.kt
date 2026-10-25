package com.example.hava.ui.screens.planb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hava.R
import com.example.hava.planb.CustomActivity
import com.example.hava.planb.CustomFactor
import com.example.hava.planb.TemperaturePreference
import com.example.hava.ui.theme.Brand
import com.example.hava.ui.theme.BrandSoft
import com.example.hava.ui.theme.FieldBorder
import com.example.hava.ui.theme.Ink
import com.example.hava.ui.theme.Muted

private const val NAME_MAX_LENGTH = 30

private data class CustomExample(
    val nameRes: Int,
    val factors: Set<CustomFactor>,
    val temperature: TemperaturePreference = TemperaturePreference.MILD,
)

private val examples = listOf(
    CustomExample(R.string.planb_example_archery, setOf(CustomFactor.WIND, CustomFactor.PRECIPITATION)),
    CustomExample(R.string.planb_example_golf, setOf(CustomFactor.WIND, CustomFactor.PRECIPITATION, CustomFactor.TEMPERATURE)),
    CustomExample(R.string.planb_example_paragliding, setOf(CustomFactor.WIND, CustomFactor.PRECIPITATION)),
    CustomExample(R.string.planb_example_photo, setOf(CustomFactor.PRECIPITATION)),
    CustomExample(
        R.string.planb_example_garden,
        setOf(CustomFactor.TEMPERATURE, CustomFactor.PRECIPITATION, CustomFactor.UV),
    ),
)

@Composable
internal fun CustomActivityDialog(
    initial: CustomActivity?,
    onDismiss: () -> Unit,
    onSave: (CustomActivity) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var factorNames by rememberSaveable {
        mutableStateOf(
            (initial?.factors ?: setOf(CustomFactor.PRECIPITATION, CustomFactor.WIND)).joinToString(",") { it.name },
        )
    }
    var temperatureName by rememberSaveable {
        mutableStateOf((initial?.temperature ?: TemperaturePreference.MILD).name)
    }
    val factors = CustomFactor.entries.filter { it.name in factorNames.split(',') }.toSet()
    val temperature = TemperaturePreference.valueOf(temperatureName)
    val exampleNames = examples.map { stringResource(it.nameRes) }
    val canSave = name.isNotBlank() && factors.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(stringResource(R.string.planb_custom_title), color = Ink) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(NAME_MAX_LENGTH) },
                    label = { Text(stringResource(R.string.planb_custom_name)) },
                    placeholder = { Text(stringResource(R.string.planb_custom_name_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brand,
                        focusedLabelColor = Brand,
                        cursorColor = Brand,
                        unfocusedBorderColor = FieldBorder,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                DialogLabel(stringResource(R.string.planb_custom_examples))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    examples.forEachIndexed { index, example ->
                        SuggestionChip(
                            onClick = {
                                name = exampleNames[index]
                                factorNames = example.factors.joinToString(",") { it.name }
                                temperatureName = example.temperature.name
                            },
                            label = { Text(exampleNames[index]) },
                            shape = RoundedCornerShape(50),
                            colors = SuggestionChipDefaults.suggestionChipColors(labelColor = Ink),
                        )
                    }
                }
                DialogLabel(stringResource(R.string.planb_custom_factors))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (factor in CustomFactor.entries) {
                        val selected = factor in factors
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val next = if (selected) factors - factor else factors + factor
                                factorNames = next.joinToString(",") { it.name }
                            },
                            label = { Text(stringResource(factor.labelRes())) },
                            leadingIcon = {
                                Icon(factor.icon(), contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            shape = RoundedCornerShape(50),
                            colors = brandChipColors(),
                        )
                    }
                }
                if (CustomFactor.TEMPERATURE in factors) {
                    DialogLabel(stringResource(R.string.planb_custom_temperature))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (preference in TemperaturePreference.entries) {
                            FilterChip(
                                selected = preference == temperature,
                                onClick = { temperatureName = preference.name },
                                label = { Text(stringResource(preference.labelRes())) },
                                shape = RoundedCornerShape(50),
                                colors = brandChipColors(),
                            )
                        }
                    }
                }
                Text(stringResource(R.string.planb_custom_note), style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(CustomActivity(name.trim(), factors, temperature)) },
                enabled = canSave,
            ) {
                Text(stringResource(R.string.planb_custom_save), color = if (canSave) Brand else Muted)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = Muted) }
        },
    )
}

@Composable
private fun DialogLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Ink)
}

@Composable
private fun brandChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = BrandSoft,
    selectedLabelColor = Brand,
    selectedLeadingIconColor = Brand,
    labelColor = Ink,
    iconColor = Muted,
)

internal fun CustomFactor.labelRes(): Int = when (this) {
    CustomFactor.PRECIPITATION -> R.string.planb_custom_f_precip
    CustomFactor.WIND -> R.string.planb_custom_f_wind
    CustomFactor.TEMPERATURE -> R.string.planb_custom_f_temp
    CustomFactor.UV -> R.string.planb_custom_f_uv
}

internal fun CustomFactor.icon(): ImageVector = when (this) {
    CustomFactor.PRECIPITATION -> Icons.Filled.Umbrella
    CustomFactor.WIND -> Icons.Filled.Air
    CustomFactor.TEMPERATURE -> Icons.Filled.Thermostat
    CustomFactor.UV -> Icons.Filled.WbSunny
}

internal fun TemperaturePreference.labelRes(): Int = when (this) {
    TemperaturePreference.COOL -> R.string.planb_temp_cool
    TemperaturePreference.MILD -> R.string.planb_temp_mild
    TemperaturePreference.WARM -> R.string.planb_temp_warm
}
