package io.formcraft.core

/**
 * Controls WHEN validation runs for a field.
 *
 * - [OnChange]    : validate on every keystroke (eager, real-time feedback)
 * - [OnFocusLost] : validate when user leaves the field (balanced UX)
 * - [OnSubmit]    : validate only when form.submit() is called (lazy)
 */
enum class ValidationStrategy {
    OnChange,
    OnFocusLost,
    OnSubmit
}
