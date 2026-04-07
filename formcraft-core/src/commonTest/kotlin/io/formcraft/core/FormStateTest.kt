package io.formcraft.core

import io.formcraft.core.rules.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FormStateTest {

    // ── Basic field registration ──────────────────────────────────────────────

    @Test
    fun `form initialises with empty fields and is invalid`() = runTest {
        val form = FormState {
            field("email") { rules(Required, Email) }
            field("password") { rules(Required, MinLength(8)) }
        }
        assertFalse(form.isValid.first())
    }

    // ── Sync validation ───────────────────────────────────────────────────────

    @Test
    fun `valid email passes Email rule`() = runTest {
        val form = FormState {
            field("email") { rules(Required, Email) }
        }
        form.field<String>("email").onValueChange("hello@example.com")
        assertNull(form.field<String>("email").currentError)
        assertTrue(form.field<String>("email").isValid)
    }

    @Test
    fun `invalid email fails Email rule with correct message`() = runTest {
        val form = FormState {
            field("email") { rules(Required, Email) }
        }
        form.field<String>("email").onValueChange("not-an-email")
        assertEquals("Enter a valid email address", form.field<String>("email").currentError)
    }

    @Test
    fun `empty field fails Required rule`() = runTest {
        val form = FormState {
            field("name") { rules(Required) }
        }
        form.field<String>("name").onValueChange("")
        assertEquals("This field is required", form.field<String>("name").currentError)
    }

    @Test
    fun `password shorter than minimum fails MinLength`() = runTest {
        val form = FormState {
            field("password") { rules(Required, MinLength(8)) }
        }
        form.field<String>("password").onValueChange("short")
        assertEquals("Must be at least 8 characters", form.field<String>("password").currentError)
    }

    // ── Cross-field validation ────────────────────────────────────────────────

    @Test
    fun `confirm password must match password`() = runTest {
        val form = FormState {
            field("password")        { rules(Required, MinLength(8)) }
            field("confirmPassword") { rules(Required, Matches("password")) }
        }
        form.field<String>("password").onValueChange("Secret@123")
        form.field<String>("confirmPassword").onValueChange("Different")
        assertEquals("Fields do not match", form.field<String>("confirmPassword").currentError)

        form.field<String>("confirmPassword").onValueChange("Secret@123")
        assertNull(form.field<String>("confirmPassword").currentError)
    }

    // ── Form-level validate() ─────────────────────────────────────────────────

    @Test
    fun `validate() marks all fields as touched and returns false for empty form`() = runTest {
        val form = FormState {
            field("email")    { rules(Required, Email) }
            field("password") { rules(Required, MinLength(8)) }
        }
        val result = form.validate()
        assertFalse(result)
        assertTrue(form.field<String>("email").isTouched.first())
        assertTrue(form.field<String>("password").isTouched.first())
    }

    @Test
    fun `validate() returns true when all fields are valid`() = runTest {
        val form = FormState {
            field("email")    { rules(Required, Email) }
            field("password") { rules(Required, MinLength(8)) }
        }
        form.field<String>("email").onValueChange("valid@test.com")
        form.field<String>("password").onValueChange("StrongPass1!")
        assertTrue(form.validate())
    }

    // ── reset() ───────────────────────────────────────────────────────────────

    @Test
    fun `reset() clears all field values and errors`() = runTest {
        val form = FormState {
            field("email") { rules(Required, Email) }
        }
        form.field<String>("email").onValueChange("bad")
        form.validate()

        form.reset()

        assertEquals("", form.field<String>("email").currentValue)
        assertNull(form.field<String>("email").currentError)
        assertFalse(form.field<String>("email").isTouched.first())
    }

    // ── collectData() ─────────────────────────────────────────────────────────

    @Test
    fun `collectData() returns current values of all fields`() = runTest {
        val form = FormState {
            field("email") { rules(Required, Email) }
            field("name")  { rules(Required) }
        }
        form.field<String>("email").onValueChange("user@example.com")
        form.field<String>("name").onValueChange("Alice")

        val data = form.collectData()
        assertEquals("user@example.com", data["email"])
        assertEquals("Alice", data["name"])
    }

    // ── Async validation ──────────────────────────────────────────────────────

    @Test
    fun `async rule failing sets error on field`() = runTest {
        // Pass the test scope so virtual-time control works with advanceUntilIdle()
        val form = FormState(scope = this) {
            field("username") {
                rules(Required)
                asyncRule("unique") { _ ->
                    ValidationResult.Error("Username already taken")
                }
                debounce(0L) // No debounce in tests
            }
        }
        form.field<String>("username").onValueChange("takenUser")
        advanceUntilIdle()
        assertEquals("Username already taken", form.field<String>("username").currentError)
    }
}
