package io.formcraft.core.rules

import io.formcraft.core.ValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextRulesTest {

    @Test fun `Required passes for non-blank`() {
        assertTrue(Required.validate("hello").isSuccess)
    }

    @Test fun `Required fails for blank`() {
        assertTrue(Required.validate("").isError)
        assertTrue(Required.validate("   ").isError)
    }

    @Test fun `MinLength passes when met`() {
        assertTrue(MinLength(5).validate("hello").isSuccess)
        assertTrue(MinLength(5).validate("hello world").isSuccess)
    }

    @Test fun `MinLength fails when not met`() {
        val result = MinLength(5).validate("hi")
        assertTrue(result.isError)
        assertEquals("Must be at least 5 characters", result.errorMessage)
    }

    @Test fun `Email passes valid address`() {
        assertTrue(Email.validate("user@example.com").isSuccess)
        assertTrue(Email.validate("a.b+c@d.co.uk").isSuccess)
    }

    @Test fun `Email fails invalid address`() {
        assertTrue(Email.validate("notanemail").isError)
        assertTrue(Email.validate("missing@tld").isError)
        assertTrue(Email.validate("@nodomain.com").isError)
    }

    @Test fun `HasUppercase passes when uppercase present`() {
        assertTrue(HasUppercase.validate("Hello").isSuccess)
    }

    @Test fun `HasUppercase fails when no uppercase`() {
        assertTrue(HasUppercase.validate("hello123").isError)
    }

    @Test fun `StrongPassword passes strong password`() {
        assertTrue(StrongPassword.validate("Secret@123").isSuccess)
    }

    @Test fun `StrongPassword fails weak password`() {
        assertTrue(StrongPassword.validate("weak").isError)
        assertTrue(StrongPassword.validate("alllowercase1!").isError)
    }

    @Test fun `PhoneNumber passes valid number`() {
        assertTrue(PhoneNumber.validate("+1234567890").isSuccess)
        assertTrue(PhoneNumber.validate("9876543210").isSuccess)
    }

    @Test fun `PhoneNumber fails short number`() {
        assertTrue(PhoneNumber.validate("123").isError)
    }
}
