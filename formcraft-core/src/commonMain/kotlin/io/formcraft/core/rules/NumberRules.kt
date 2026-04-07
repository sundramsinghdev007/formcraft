package io.formcraft.core.rules

import io.formcraft.core.ValidationResult
import io.formcraft.core.Validator

/** Integer must be >= [min]. */
fun Min(min: Int, message: String? = null) = Validator<Int> { value ->
    if (value >= min) ValidationResult.Success
    else ValidationResult.Error(message ?: "Must be at least $min")
}

/** Integer must be <= [max]. */
fun Max(max: Int, message: String? = null) = Validator<Int> { value ->
    if (value <= max) ValidationResult.Success
    else ValidationResult.Error(message ?: "Must be at most $max")
}

/** Integer must be between [min] and [max] inclusive. */
fun Between(min: Int, max: Int) = Validator<Int> { value ->
    if (value in min..max) ValidationResult.Success
    else ValidationResult.Error("Must be between $min and $max")
}

/** Integer must be > 0. */
val Positive = Validator<Int> { value ->
    if (value > 0) ValidationResult.Success
    else ValidationResult.Error("Must be a positive number")
}

/** Integer must be >= 0. */
val NonNegative = Validator<Int> { value ->
    if (value >= 0) ValidationResult.Success
    else ValidationResult.Error("Must be zero or positive")
}

/** String field that must be a valid integer. */
val IsNumeric = Validator<String> { value ->
    if (value.toIntOrNull() != null) ValidationResult.Success
    else ValidationResult.Error("Must be a number")
}
