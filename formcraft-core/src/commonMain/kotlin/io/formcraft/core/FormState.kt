package io.formcraft.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * The central state holder for an entire form.
 *
 * Usage — in ViewModel:
 *   val form = FormState {
 *       field("email")    { rules(Required, Email) }
 *       field("password") { rules(Required, MinLength(8)) }
 *   }
 *
 * Usage — in Composable (via rememberFormState):
 *   val form = rememberFormState { ... }
 */
class FormState(
    private val globalStrategy: ValidationStrategy = ValidationStrategy.OnChange,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val builderBlock: FormBuilder.() -> Unit
) {
    // ── Internal ─────────────────────────────────────────────────────────────

    private val builder = FormBuilder(globalStrategy).apply(builderBlock)
    private val fieldStates: Map<String, FieldState<*>> = builder.buildFields(scope) {
        recomputeFormState()
    }

    // Inject cross-field reader into every FieldState
    init {
        fieldStates.values.forEach { fieldState ->
            fieldState.formFieldReader = { key -> fieldStates[key]?.currentValue }
        }
    }

    // ── Public observable state ───────────────────────────────────────────────

    private val _isValid = MutableStateFlow(false)
    private val _isDirty = MutableStateFlow(false)
    private val _isSubmitting = MutableStateFlow(false)

    /** True when ALL fields pass ALL their validation rules. */
    val isValid: StateFlow<Boolean> = _isValid.asStateFlow()

    /** True when ANY field's value has changed from its initial value. */
    val isDirty: StateFlow<Boolean> = _isDirty.asStateFlow()

    /** True while the form's submit coroutine is running. */
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    // ── Field Access ──────────────────────────────────────────────────────────

    /**
     * Get a typed FieldState by key.
     * Throws if key doesn't exist — forces correct key usage.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> field(key: String): FieldState<T> =
        fieldStates[key] as? FieldState<T>
            ?: error("FormState: No field registered with key '$key'")

    /** Operator shorthand: form["email"] */
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): FieldState<T> = field(key)

    // ── Form-Level Actions ────────────────────────────────────────────────────

    /**
     * Force-validate all fields. Marks all as touched.
     * Returns true if the form is currently valid (sync only).
     * Use [submit] for async-aware validation.
     */
    fun validate(): Boolean {
        val results = fieldStates.values.map { it.forceValidate() }
        recomputeFormState()
        return results.all { it }
    }

    /**
     * Reset ALL fields to their initial values and clear all errors.
     */
    fun reset() {
        fieldStates.values.forEach { it.reset() }
        recomputeFormState()
    }

    /**
     * Programmatically set field values (useful for pre-filling edit forms).
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> setFieldValue(key: String, value: T) {
        (fieldStates[key] as? FieldState<T>)?.setValue(value)
        recomputeFormState()
    }

    /**
     * Validates then executes [onValid] if the form passes.
     * Sets isSubmitting = true during execution.
     */
    fun submit(onValid: suspend (data: Map<String, Any?>) -> Unit) {
        scope.launch {
            if (!validate()) return@launch
            _isSubmitting.value = true
            try {
                onValid(collectData())
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Collect current values of all fields as a plain Map.
     */
    fun collectData(): Map<String, Any?> =
        fieldStates.mapValues { (_, fieldState) -> fieldState.currentValue }

    /**
     * Get all current errors as a Map<fieldKey, errorMessage>.
     */
    val errors: Map<String, String?>
        get() = fieldStates.mapValues { (_, s) -> s.currentError }

    val firstError: String?
        get() = errors.values.firstOrNull { it != null }

    // ── Internal ─────────────────────────────────────────────────────────────

    private fun recomputeFormState() {
        _isValid.value = fieldStates.values.all { it.isValid }
        _isDirty.value = fieldStates.values.any { it.isDirty }
    }
}

