package io.formcraft.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.formcraft.core.FieldState

/**
 * A checkbox bound to a Boolean [FieldState].
 *
 * Usage:
 *   FormCheckbox(
 *       fieldState = form.field("acceptTerms"),
 *       label = "I accept the Terms and Conditions"
 *   )
 */
@Composable
fun FormCheckbox(
    fieldState: FieldState<Boolean>,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    val checked by fieldState.value.collectAsState()
    val error by fieldState.error.collectAsState()
    val isTouched by fieldState.isTouched.collectAsState()

    val showError = isTouched && error != null

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { newValue ->
                    fieldState.onFocusGained()
                    fieldState.onValueChange(newValue)
                    fieldState.onFocusLost()
                }
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
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
