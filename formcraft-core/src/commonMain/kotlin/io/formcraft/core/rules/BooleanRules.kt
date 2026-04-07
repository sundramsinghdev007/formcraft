package io.formcraft.core.rules

import io.formcraft.core.ValidationResult
import io.formcraft.core.Validator

/** Boolean field must be true (e.g., "I agree to Terms and Conditions"). */
val MustBeTrue = Validator<Boolean> { value ->
    if (value) ValidationResult.Success
    else ValidationResult.Error("You must accept to continue")
}

/** Boolean field must be false. */
val MustBeFalse = Validator<Boolean> { value ->
    if (!value) ValidationResult.Success
    else ValidationResult.Error("This must be unchecked")
}
