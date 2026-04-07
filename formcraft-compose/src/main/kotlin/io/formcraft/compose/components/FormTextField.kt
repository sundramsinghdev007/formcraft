package io.formcraft.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import io.formcraft.core.FieldState

/**
 * A Material3 OutlinedTextField bound to a [FieldState].
 *
 * Handles:
 *   - Two-way value binding
 *   - Focus gained/lost events for validation strategy
 *   - Error display from FieldState
 *
 * Usage:
 *   FormTextField(
 *       fieldState = form.field("email"),
 *       label = "Email",
 *       modifier = Modifier.fillMaxWidth()
 *   )
 */
@Composable
fun FormTextField(
    fieldState: FieldState<String>,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
) {
    val value by fieldState.value.collectAsState()
    val error by fieldState.error.collectAsState()
    val isTouched by fieldState.isTouched.collectAsState()

    val showError = isTouched && error != null

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { fieldState.onValueChange(it) },
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            isError = showError,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) fieldState.onFocusGained()
                    else fieldState.onFocusLost()
                }
        )
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
