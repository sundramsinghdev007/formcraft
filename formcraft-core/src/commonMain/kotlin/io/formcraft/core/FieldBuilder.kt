package io.formcraft.core

/**
 * DSL builder for configuring a single field.
 *
 * Usage:
 *   field("email") {
 *       initial("")
 *       strategy(ValidationStrategy.OnFocusLost)
 *       rules(Required, Email)
 *       asyncRule("unique") { value ->
 *           if (api.isTaken(value)) ValidationResult.Error("Taken")
 *           else ValidationResult.Success
 *       }
 *   }
 */
class FieldBuilder<T>(
    private val key: String,
    private val defaultInitial: T
) {
    private var initialValue: T = defaultInitial
    private val validators = mutableListOf<Validator<T>>()
    private val asyncValidators = mutableListOf<Pair<String, AsyncValidator<T>>>()
    private var strategy: ValidationStrategy = ValidationStrategy.OnChange
    private var debounceMs: Long = 300L
    private var isRequired: Boolean = false

    /** Set the initial / default value for this field. */
    fun initial(value: T) { initialValue = value }

    /** Override the validation trigger strategy for this field only. */
    fun strategy(s: ValidationStrategy) { strategy = s }

    /** Set async validation debounce in milliseconds (default: 300ms). */
    fun debounce(ms: Long) { debounceMs = ms }

    /** Add one or more synchronous validators. */
    fun rules(vararg v: Validator<T>) { validators.addAll(v) }

    /** Add a single async validator with a unique key (for identification). */
    fun asyncRule(key: String, validator: AsyncValidator<T>) {
        asyncValidators.add(key to validator)
    }

    /** Shorthand: asyncRule with a suspend lambda. */
    fun asyncRule(key: String, block: suspend (T) -> ValidationResult) {
        asyncValidators.add(key to AsyncValidator { block(it) })
    }

    internal fun build(): FieldConfig<T> = FieldConfig(
        key = key,
        initialValue = initialValue,
        validators = validators.toList(),
        asyncValidators = asyncValidators.toList(),
        strategy = strategy,
        isRequired = isRequired,
        debounceMs = debounceMs
    )
}
