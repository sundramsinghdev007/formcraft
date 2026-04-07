package io.formcraft.core

/**
 * Immutable configuration for a single field.
 * Built by [FieldBuilder] and stored inside [FormState].
 */
data class FieldConfig<T>(
    val key: String,
    val initialValue: T,
    val validators: List<Validator<T>>,
    val asyncValidators: List<Pair<String, AsyncValidator<T>>>,
    val strategy: ValidationStrategy,
    val isRequired: Boolean,
    val debounceMs: Long
)
