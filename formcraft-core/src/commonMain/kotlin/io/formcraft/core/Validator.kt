package io.formcraft.core

/**
 * Base interface for all synchronous validation rules.
 *
 * Usage — built-in:
 *   rules(Required, Email, MinLength(8))
 *
 * Usage — custom:
 *   val StartsWithRef = Validator<String> { value ->
 *       if (value.startsWith("REF-")) ValidationResult.Success
 *       else ValidationResult.Error("Must start with REF-")
 *   }
 */
fun interface Validator<T> {
    fun validate(value: T): ValidationResult
}

/**
 * Base interface for async validation rules (e.g., API calls).
 * Has built-in debounce support at the FieldState level.
 *
 * Usage:
 *   asyncRule("unique_email") { value ->
 *       val taken = api.isEmailTaken(value)
 *       if (taken) ValidationResult.Error("Email already in use")
 *       else ValidationResult.Success
 *   }
 */
fun interface AsyncValidator<T> {
    suspend fun validate(value: T): ValidationResult
}

/**
 * Marker interface for validators that need to read other field values.
 * Implemented by cross-field validators like [Matches] and [NotMatches].
 * FieldState injects the reader automatically — no reflection required.
 */
interface ReaderAware {
    fun injectFieldReader(reader: (String) -> Any?)
}
