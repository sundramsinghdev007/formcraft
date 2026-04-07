package io.formcraft.compose

import io.formcraft.core.FieldState
import io.formcraft.core.FormState

/**
 * Receiver scope for form composables.
 * Provides convenient access to [FormState] field lookups inside a form block.
 *
 * Usage:
 *   FormScope(form) {
 *       FormTextField(fieldKey = "email", label = "Email")
 *   }
 */
class FormScope(val formState: FormState) {
    fun <T> field(key: String): FieldState<T> = formState.field(key)
}
