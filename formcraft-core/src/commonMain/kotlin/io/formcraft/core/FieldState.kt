package io.formcraft.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Live, observable state for a single form field.
 *
 * Exposes:
 *   - value          → current field value
 *   - error          → first failing rule's message (null = valid)
 *   - isTouched      → has the user interacted with this field?
 *   - isDirty        → has value changed from initialValue?
 *   - isValidating   → async rule in progress?
 *   - isValid        → all rules pass AND no async errors?
 */
class FieldState<T>(
    private val config: FieldConfig<T>,
    private val scope: CoroutineScope,
    private val onFieldChanged: () -> Unit   // notifies parent FormState
) {
    // ── Internal mutable state ───────────────────────────────────────────────

    private val _value = MutableStateFlow(config.initialValue)
    private val _error = MutableStateFlow<String?>(null)
    private val _isTouched = MutableStateFlow(false)
    private val _isValidating = MutableStateFlow(false)

    private var asyncDebounceJob: Job? = null

    // ── Public state (read-only) ─────────────────────────────────────────────

    val value: StateFlow<T> = _value.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()
    val isTouched: StateFlow<Boolean> = _isTouched.asStateFlow()
    val isValidating: StateFlow<Boolean> = _isValidating.asStateFlow()

    val isDirty: Boolean get() = _value.value != config.initialValue
    val isValid: Boolean get() = _error.value == null && !_isValidating.value
    val currentValue: T get() = _value.value
    val currentError: String? get() = _error.value

    // ── Actions ──────────────────────────────────────────────────────────────

    /**
     * Called by the UI whenever the field value changes (e.g., onValueChange in TextField).
     */
    fun onValueChange(newValue: T) {
        _value.value = newValue
        if (config.strategy == ValidationStrategy.OnChange) {
            validateSync(newValue)
            triggerAsyncValidation(newValue)
        }
        onFieldChanged()
    }

    /**
     * Called when the field gains focus (user starts interacting).
     */
    fun onFocusGained() {
        _isTouched.value = true
    }

    /**
     * Called when the field loses focus (user moves to next field).
     */
    fun onFocusLost() {
        _isTouched.value = true
        if (config.strategy == ValidationStrategy.OnFocusLost) {
            validateSync(_value.value)
            triggerAsyncValidation(_value.value)
        }
        onFieldChanged()
    }

    /**
     * Force validation — called by FormState.validate() on submit.
     */
    fun forceValidate(): Boolean {
        _isTouched.value = true
        val syncResult = validateSync(_value.value)
        if (syncResult) triggerAsyncValidation(_value.value)
        return syncResult && _error.value == null
    }

    /**
     * Reset field to its initial value and clear all state.
     */
    fun reset() {
        _value.value = config.initialValue
        _error.value = null
        _isTouched.value = false
        _isValidating.value = false
        asyncDebounceJob?.cancel()
        onFieldChanged()
    }

    /**
     * Programmatically set value (for edit-mode forms with pre-filled data).
     */
    fun setValue(value: T) {
        _value.value = value
        onFieldChanged()
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    /**
     * Runs all synchronous validators in order.
     * Stops at the first failure and sets the error message.
     * Returns true if all sync validators pass.
     */
    internal fun validateSync(value: T): Boolean {
        for (validator in config.validators) {
            // Inject cross-field reader into validators that need it (e.g. Matches, NotMatches)
            if (validator is ReaderAware) {
                formFieldReader?.let { validator.injectFieldReader(it) }
            }
            when (val result = validator.validate(value)) {
                is ValidationResult.Error -> {
                    _error.value = result.message
                    return false
                }
                is ValidationResult.Success -> { /* continue */ }
            }
        }
        // All sync validators passed — clear sync errors
        if (config.asyncValidators.isEmpty()) {
            _error.value = null
        }
        return true
    }

    /**
     * Debounced async validation — cancels previous job on rapid typing.
     */
    private fun triggerAsyncValidation(value: T) {
        if (config.asyncValidators.isEmpty()) return

        asyncDebounceJob?.cancel()
        asyncDebounceJob = scope.launch {
            delay(config.debounceMs)
            _isValidating.value = true
            try {
                for ((_, asyncValidator) in config.asyncValidators) {
                    when (val result = asyncValidator.validate(value)) {
                        is ValidationResult.Error -> {
                            _error.value = result.message
                            _isValidating.value = false
                            onFieldChanged()
                            return@launch
                        }
                        is ValidationResult.Success -> { /* continue */ }
                    }
                }
                _error.value = null
            } finally {
                _isValidating.value = false
                onFieldChanged()
            }
        }
    }

    /**
     * Reads the current value of another field — used by cross-field validators.
     */
    internal var formFieldReader: ((String) -> Any?)? = null
}
