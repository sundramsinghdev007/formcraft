package io.formcraft.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Manages a multi-step form — a sequence of [FormState]s validated step by step.
 *
 * Usage:
 *   val wizard = MultiStepFormState {
 *       step {                                    // Step 1 — Personal Info
 *           field("name")  { rules(Required) }
 *           field("email") { rules(Required, Email) }
 *       }
 *       step {                                    // Step 2 — Account
 *           field("password")        { rules(Required, MinLength(8)) }
 *           field("confirmPassword") { rules(Required, Matches("password")) }
 *       }
 *       step {                                    // Step 3 — Address
 *           field("city") { rules(Required) }
 *           field("zip")  { rules(Required, ExactLength(5)) }
 *       }
 *   }
 *
 *   wizard.nextStep()      // validate current step → advance if valid
 *   wizard.previousStep()  // go back (no validation)
 *   wizard.submit { data -> ... }
 */
class MultiStepFormState(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    stepBlocks: List<FormBuilder.() -> Unit>
) {
    private val stepForms: List<FormState> = stepBlocks.map { block ->
        FormState(scope = scope, builderBlock = block)
    }

    private val _currentStepIndex = MutableStateFlow(0)
    private val _isSubmitting = MutableStateFlow(false)

    /** Index of the currently active step (0-based). */
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    /** True while submit coroutine is running. */
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    /** Number of steps in this form. */
    val totalSteps: Int get() = stepForms.size

    /** The [FormState] for the currently active step. */
    val currentStep: FormState get() = stepForms[_currentStepIndex.value]

    /** True when the user is on the first step. */
    val isFirstStep: Boolean get() = _currentStepIndex.value == 0

    /** True when the user is on the last step. */
    val isLastStep: Boolean get() = _currentStepIndex.value == stepForms.size - 1

    /** Access a specific step's [FormState] by index. */
    fun step(index: Int): FormState = stepForms[index]

    /**
     * Validates the current step.
     * If valid, advances to the next step and returns true.
     * If on the last step or validation fails, returns false.
     */
    fun nextStep(): Boolean {
        if (isLastStep) return false
        if (!currentStep.validate()) return false
        _currentStepIndex.value++
        return true
    }

    /**
     * Goes back to the previous step without validation.
     * Does nothing if already on the first step.
     */
    fun previousStep() {
        if (!isFirstStep) _currentStepIndex.value--
    }

    /**
     * Jump directly to a step by index (0-based).
     * Validates all steps before the target if moving forward.
     */
    fun goToStep(index: Int): Boolean {
        require(index in stepForms.indices) { "Step index $index out of range [0, ${stepForms.size - 1}]" }
        if (index > _currentStepIndex.value) {
            for (i in _currentStepIndex.value until index) {
                if (!stepForms[i].validate()) return false
            }
        }
        _currentStepIndex.value = index
        return true
    }

    /**
     * Validates ALL steps (not just current).
     * Returns true if every step passes.
     */
    fun validateAll(): Boolean = stepForms.all { it.validate() }

    /**
     * Resets all steps to their initial values and returns to step 0.
     */
    fun reset() {
        stepForms.forEach { it.reset() }
        _currentStepIndex.value = 0
    }

    /**
     * Collects field values from all steps merged into one map.
     * Keys must be unique across steps.
     */
    fun collectAllData(): Map<String, Any?> =
        stepForms.fold(mutableMapOf()) { acc, form ->
            acc.putAll(form.collectData())
            acc
        }

    /**
     * Validates all steps then executes [onValid] with the combined data.
     * Sets [isSubmitting] = true during execution.
     */
    fun submit(onValid: suspend (data: Map<String, Any?>) -> Unit) {
        scope.launch {
            if (!validateAll()) return@launch
            _isSubmitting.value = true
            try {
                onValid(collectAllData())
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /** Convenience: collect all errors from all steps. */
    val allErrors: Map<String, String?>
        get() = stepForms.fold(mutableMapOf()) { acc, form ->
            acc.putAll(form.errors)
            acc
        }
}

// ── DSL ───────────────────────────────────────────────────────────────────────

class MultiStepFormBuilder {
    private val steps = mutableListOf<FormBuilder.() -> Unit>()

    /** Add a step to the wizard. */
    fun step(block: FormBuilder.() -> Unit) {
        steps.add(block)
    }

    internal fun buildSteps(): List<FormBuilder.() -> Unit> = steps.toList()
}

/**
 * Creates a [MultiStepFormState] using the DSL builder.
 */
fun MultiStepFormState(
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    block: MultiStepFormBuilder.() -> Unit
): MultiStepFormState {
    val builder = MultiStepFormBuilder().apply(block)
    return MultiStepFormState(scope = scope, stepBlocks = builder.buildSteps())
}
