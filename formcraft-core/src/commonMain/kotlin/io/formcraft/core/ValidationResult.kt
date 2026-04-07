package io.formcraft.core

/**
 * The result of running a validation rule against a field value.
 * Using a sealed class ensures exhaustive when-expressions everywhere.
 */
sealed class ValidationResult {

    /** Field passed validation — no error to show. */
    object Success : ValidationResult()

    /**
     * Field failed validation.
     * @param message Human-readable error shown to the user.
     */
    data class Error(val message: String) : ValidationResult()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val errorMessage: String? get() = (this as? Error)?.message
}
