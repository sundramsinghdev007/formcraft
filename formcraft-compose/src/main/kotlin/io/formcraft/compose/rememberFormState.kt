package io.formcraft.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.formcraft.core.FormBuilder
import io.formcraft.core.FormState
import io.formcraft.core.ValidationStrategy

/**
 * Creates and remembers a [FormState] across recompositions.
 *
 * Usage:
 *   val form = rememberFormState {
 *       field("email") { rules(Required, Email) }
 *       field("password") { rules(Required, MinLength(8)) }
 *   }
 */
@Composable
fun rememberFormState(
    globalStrategy: ValidationStrategy = ValidationStrategy.OnChange,
    block: FormBuilder.() -> Unit
): FormState {
    val scope = rememberCoroutineScope()
    return remember { FormState(globalStrategy, scope, block) }
}
