package io.formcraft.core.rules

import io.formcraft.core.ValidationResult
import io.formcraft.core.Validator

// ── Required ─────────────────────────────────────────────────────────────────

/** Field must not be blank (empty or whitespace only). */
val Required = Validator<String> { value ->
    if (value.isNotBlank()) ValidationResult.Success
    else ValidationResult.Error("This field is required")
}

/** Required with a custom message. */
fun Required(message: String) = Validator<String> { value ->
    if (value.isNotBlank()) ValidationResult.Success
    else ValidationResult.Error(message)
}

// ── Length ────────────────────────────────────────────────────────────────────

/** Minimum character count. */
fun MinLength(min: Int, message: String? = null) = Validator<String> { value ->
    if (value.length >= min) ValidationResult.Success
    else ValidationResult.Error(message ?: "Must be at least $min characters")
}

/** Maximum character count. */
fun MaxLength(max: Int, message: String? = null) = Validator<String> { value ->
    if (value.length <= max) ValidationResult.Success
    else ValidationResult.Error(message ?: "Must be at most $max characters")
}

/** Exact character count. */
fun ExactLength(length: Int, message: String? = null) = Validator<String> { value ->
    if (value.length == length) ValidationResult.Success
    else ValidationResult.Error(message ?: "Must be exactly $length characters")
}

// ── Format ────────────────────────────────────────────────────────────────────

/** Standard email address format. */
val Email = Validator<String> { value ->
    val emailRegex = Regex("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")
    if (value.matches(emailRegex)) ValidationResult.Success
    else ValidationResult.Error("Enter a valid email address")
}

/** HTTP/HTTPS URL format. */
val Url = Validator<String> { value ->
    val urlRegex = Regex("^https?://[^\\s/$.?#].[^\\s]*$")
    if (value.matches(urlRegex)) ValidationResult.Success
    else ValidationResult.Error("Enter a valid URL (e.g. https://example.com)")
}

/** Custom regex pattern. */
fun Pattern(pattern: String, message: String = "Invalid format") = Validator<String> { value ->
    if (value.matches(Regex(pattern))) ValidationResult.Success
    else ValidationResult.Error(message)
}

/** Phone number — international format. */
val PhoneNumber = Validator<String> { value ->
    val stripped = value.replace(Regex("[\\s\\-()]"), "")
    val phoneRegex = Regex("^\\+?[0-9]{7,15}$")
    if (stripped.matches(phoneRegex)) ValidationResult.Success
    else ValidationResult.Error("Enter a valid phone number")
}

// ── Content Rules ─────────────────────────────────────────────────────────────

/** No whitespace characters allowed. */
val NoWhitespace = Validator<String> { value ->
    if (!value.contains(' ')) ValidationResult.Success
    else ValidationResult.Error("No spaces allowed")
}

/** Letters only (a-z, A-Z). */
val AlphaOnly = Validator<String> { value ->
    if (value.all { it.isLetter() }) ValidationResult.Success
    else ValidationResult.Error("Letters only")
}

/** Letters and digits only. */
val AlphaNumeric = Validator<String> { value ->
    if (value.all { it.isLetterOrDigit() }) ValidationResult.Success
    else ValidationResult.Error("Letters and numbers only")
}

/** At least one uppercase letter. */
val HasUppercase = Validator<String> { value ->
    if (value.any { it.isUpperCase() }) ValidationResult.Success
    else ValidationResult.Error("Must contain at least one uppercase letter")
}

/** At least one digit. */
val HasDigit = Validator<String> { value ->
    if (value.any { it.isDigit() }) ValidationResult.Success
    else ValidationResult.Error("Must contain at least one number")
}

/** At least one special character. */
val HasSpecialChar = Validator<String> { value ->
    val specialChars = "!@#\$%^&*()_+-=[]{}|;':\",./<>?"
    if (value.any { it in specialChars }) ValidationResult.Success
    else ValidationResult.Error("Must contain at least one special character")
}

/** Strong password: min 8 chars, uppercase, digit, special char. */
val StrongPassword = Validator<String> { value ->
    when {
        value.length < 8       -> ValidationResult.Error("Password must be at least 8 characters")
        !value.any { it.isUpperCase() } -> ValidationResult.Error("Must contain an uppercase letter")
        !value.any { it.isDigit() }     -> ValidationResult.Error("Must contain a number")
        !value.any { "!@#\$%^&*".contains(it) } -> ValidationResult.Error("Must contain a special character (!@#\$%^&*)")
        else -> ValidationResult.Success
    }
}
