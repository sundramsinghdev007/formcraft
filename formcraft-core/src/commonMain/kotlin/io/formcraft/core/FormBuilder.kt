package io.formcraft.core

import kotlinx.coroutines.CoroutineScope

/**
 * DSL builder for [FormState].
 * Collects field configurations before the form is built.
 */
class FormBuilder(private val globalStrategy: ValidationStrategy) {

    private val fieldConfigs = mutableListOf<FieldConfig<*>>()

    /**
     * Register a String field.
     * Most common — covers text, email, password, search fields.
     */
    fun field(
        key: String,
        initialValue: String = "",
        block: FieldBuilder<String>.() -> Unit = {}
    ) {
        val builder = FieldBuilder(key, initialValue).apply {
            strategy(globalStrategy)
            block()
        }
        fieldConfigs.add(builder.build())
    }

    /**
     * Register a Boolean field (checkbox, toggle).
     */
    fun boolField(
        key: String,
        initialValue: Boolean = false,
        block: FieldBuilder<Boolean>.() -> Unit = {}
    ) {
        val builder = FieldBuilder(key, initialValue).apply {
            strategy(globalStrategy)
            block()
        }
        fieldConfigs.add(builder.build())
    }

    /**
     * Register an Int field (age, quantity, etc.).
     */
    fun intField(
        key: String,
        initialValue: Int = 0,
        block: FieldBuilder<Int>.() -> Unit = {}
    ) {
        val builder = FieldBuilder(key, initialValue).apply {
            strategy(globalStrategy)
            block()
        }
        fieldConfigs.add(builder.build())
    }

    /**
     * Register a typed field for any custom type (e.g., Date, Enum).
     */
    fun <T> typedField(
        key: String,
        initialValue: T,
        block: FieldBuilder<T>.() -> Unit = {}
    ) {
        val builder = FieldBuilder(key, initialValue).apply {
            strategy(globalStrategy)
            block()
        }
        fieldConfigs.add(builder.build())
    }

    @Suppress("UNCHECKED_CAST")
    internal fun buildFields(
        scope: CoroutineScope,
        onFieldChanged: () -> Unit
    ): Map<String, FieldState<*>> {
        return fieldConfigs.associate { config ->
            config.key to FieldState(
                config = config as FieldConfig<Any?>,
                scope = scope,
                onFieldChanged = onFieldChanged
            )
        }
    }
}
