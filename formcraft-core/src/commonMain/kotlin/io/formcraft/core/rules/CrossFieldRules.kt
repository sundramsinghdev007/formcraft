package io.formcraft.core.rules

import io.formcraft.core.ReaderAware
import io.formcraft.core.ValidationResult
import io.formcraft.core.Validator

/**
 * Cross-field validator: this field's value must equal another field's value.
 *
 * Classic use case: password confirmation.
 *
 * Usage:
 *   field("confirmPassword") {
 *       rules(Required, Matches("password"))
 *   }
 */
class Matches(
    private val otherFieldKey: String,
    private val message: String? = null
) : Validator<String>, ReaderAware {

    private var reader: ((String) -> Any?)? = null

    override fun injectFieldReader(reader: (String) -> Any?) {
        this.reader = reader
    }

    override fun validate(value: String): ValidationResult {
        val otherValue = reader?.invoke(otherFieldKey)?.toString() ?: ""
        return if (value == otherValue) ValidationResult.Success
        else ValidationResult.Error(message ?: "Fields do not match")
    }
}

/**
 * Cross-field validator: this field's value must NOT equal another field's value.
 *
 * Usage:
 *   field("newPassword") {
 *       rules(Required, NotMatches("currentPassword", message = "New password must differ"))
 *   }
 */
class NotMatches(
    private val otherFieldKey: String,
    private val message: String? = null
) : Validator<String>, ReaderAware {

    private var reader: ((String) -> Any?)? = null

    override fun injectFieldReader(reader: (String) -> Any?) {
        this.reader = reader
    }

    override fun validate(value: String): ValidationResult {
        val otherValue = reader?.invoke(otherFieldKey)?.toString() ?: ""
        return if (value != otherValue) ValidationResult.Success
        else ValidationResult.Error(message ?: "Fields must not match")
    }
}
