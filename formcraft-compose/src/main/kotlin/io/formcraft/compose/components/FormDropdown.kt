package io.formcraft.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.formcraft.core.FieldState

/**
 * A dropdown (exposed dropdown menu) bound to a String [FieldState].
 *
 * Usage:
 *   FormDropdown(
 *       fieldState = form.field("country"),
 *       label = "Country",
 *       options = listOf("US", "UK", "CA")
 *   )
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDropdown(
    fieldState: FieldState<String>,
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    val value by fieldState.value.collectAsState()
    val error by fieldState.error.collectAsState()
    val isTouched by fieldState.isTouched.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    val showError = isTouched && error != null

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                isError = showError,
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    fieldState.onFocusLost()
                }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            fieldState.onFocusGained()
                            fieldState.onValueChange(option)
                            fieldState.onFocusLost()
                            expanded = false
                        }
                    )
                }
            }
        }
        if (showError && error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
