# FormCraft

**Zero-reflection, type-safe form validation for Jetpack Compose & Kotlin Multiplatform.**

[![CI](https://github.com/sundramsingh/FormCraft/actions/workflows/ci.yml/badge.svg)](https://github.com/sundramsingh/FormCraft/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.formcraft/formcraft-core.svg?label=Maven%20Central)](https://central.sonatype.com/search?q=io.formcraft)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.x-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/Android-API%2024%2B-brightgreen)](https://developer.android.com/about/versions/nougat)

---

## The Problem

Every Android app has forms. And every form needs validation. The common solutions all have the same problems:

- **Manual `if/else` in ViewModels** — boilerplate that needs to be written from scratch in every project
- **Reflection-based libraries** — break silently under R8 / ProGuard minification
- **Generic validation libs** — not aware of Compose, focus events, or touched state
- **Data Binding validators** — XML-only, no Compose support

FormCraft is a purpose-built library that solves all of this with a clean DSL, zero reflection, and first-class Compose integration.

---

## Features

- **Zero Kotlin reflection** — safe with R8, ProGuard, and the K2 compiler
- **Declarative DSL** — define an entire form and all its rules in one readable block
- **Async validation with debounce** — check username availability via API without hammering the server
- **Cross-field rules** — `Matches`, `NotMatches` (password confirmation, new ≠ old password)
- **Per-field validation strategy** — `OnChange`, `OnFocusLost`, or `OnSubmit` per field
- **Multi-step form support** — `MultiStepFormState` for wizards with step-by-step validation
- **25+ built-in rules** — `Required`, `Email`, `StrongPassword`, `PhoneNumber`, `Min`, `Max`, `MustBeTrue`, and more
- **Kotlin Multiplatform ready** — `formcraft-core` is pure `commonMain`, no Android APIs
- **Fully observable** — all state exposed as `StateFlow`, integrates naturally with `collectAsState()`

---

## Quick Start

### 1. Add dependencies

```kotlin
// build.gradle.kts
dependencies {
    // Core (KMP) — use alone in non-Compose projects
    implementation("io.formcraft:formcraft-core:0.1.0")

    // Compose UI components (includes core transitively)
    implementation("io.formcraft:formcraft-compose:0.1.0")
}
```

### 2. Create a form

```kotlin
@Composable
fun LoginScreen() {
    val form = rememberFormState {
        field("email")    { rules(Required, Email) }
        field("password") { rules(Required, MinLength(8)) }
    }

    Column(Modifier.padding(16.dp)) {
        FormTextField(fieldState = form.field("email"), label = "Email")
        FormTextField(
            fieldState = form.field("password"),
            label = "Password",
            visualTransformation = PasswordVisualTransformation()
        )
        Button(onClick = { form.submit { data -> login(data) } }) {
            Text("Sign In")
        }
    }
}
```

That's it. No manual `remember { mutableStateOf("") }`, no error state variables, no `isEmailValid` booleans.

---

## Installation

### Requirements

| Component | Minimum |
|-----------|---------|
| Android | minSdk 24 (Android 7.0) |
| Kotlin | 2.2.x |
| Compose BOM | 2026.02.01+ |
| Coroutines | 1.9.0+ |
| AGP | 9.x |

### Gradle

```kotlin
// settings.gradle.kts — already included in Maven Central, no extra repos needed
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("io.formcraft:formcraft-compose:0.1.0")
}
```

> `formcraft-core` is declared as `api()` in `formcraft-compose`, so it is
> available transitively. You only need to add `formcraft-core` separately if
> you are using it without Compose (e.g., in a shared ViewModel module).

---

## Usage

### Defining Fields

```kotlin
val form = rememberFormState {
    // String fields (most common)
    field("email") {
        rules(Required, Email)
        strategy(ValidationStrategy.OnFocusLost)
    }

    // Parameterized initial value
    field("username", initialValue = "guest") {
        rules(Required, MinLength(3), AlphaNumeric)
    }

    // Boolean field (checkbox, toggle)
    boolField("acceptTerms") {
        rules(MustBeTrue)
    }

    // Integer field
    intField("age") {
        rules(Min(18), Max(120))
    }

    // Any custom type
    typedField("birthday", LocalDate(2000, 1, 1)) {
        rules(Before(LocalDate.today()))
    }
}
```

### Validation Strategies

Control when validation runs — globally or per field:

```kotlin
// Global strategy for the whole form
val form = rememberFormState(globalStrategy = ValidationStrategy.OnFocusLost) {
    field("email") { rules(Required, Email) }  // inherits OnFocusLost

    field("password") {
        strategy(ValidationStrategy.OnChange)  // override for this field
        rules(Required, StrongPassword)
    }
}
```

| Strategy | When validation runs | Best for |
|----------|---------------------|---------|
| `OnChange` | Every keystroke | Password strength meter |
| `OnFocusLost` | When user leaves the field | Email, phone — balanced UX |
| `OnSubmit` | Only when `form.validate()` is called | Simple forms |

### Cross-Field Validation

```kotlin
val form = rememberFormState {
    field("password")        { rules(Required, StrongPassword) }
    field("confirmPassword") { rules(Required, Matches("password")) }
    field("newPassword")     { rules(Required, NotMatches("password",
                                   message = "New password must differ from current")) }
}
```

`Matches` and `NotMatches` read the other field's live value at validation time — no reflection, no manual wiring.

### Async Validation

```kotlin
val form = rememberFormState {
    field("username") {
        rules(Required, MinLength(3), AlphaNumeric)
        asyncRule("unique") { value ->
            val taken = userRepository.isUsernameTaken(value)  // suspend call
            if (taken) ValidationResult.Error("Username already taken")
            else ValidationResult.Success
        }
        debounce(400L)  // wait 400ms after last keystroke before calling the API
    }
}
```

Show a loading indicator while checking:

```kotlin
val isValidating by form.field<String>("username").isValidating.collectAsState()

OutlinedTextField(
    trailingIcon = {
        if (isValidating) CircularProgressIndicator(Modifier.size(18.dp))
    }
)
```

### Form Actions

```kotlin
// Validate all fields (sync only), mark all as touched
val isValid = form.validate()

// Reset all fields to initial values, clear all errors
form.reset()

// Pre-fill fields (edit forms)
form.setFieldValue("email", user.email)
form.setFieldValue("name", user.name)

// Submit — validates, then executes the block if valid
form.submit { data ->
    // data: Map<String, Any?> with all field values
    viewModel.register(
        email = data["email"] as String,
        password = data["password"] as String
    )
}

// Collect all current values
val snapshot = form.collectData()

// All current errors
val errors: Map<String, String?> = form.errors
val firstError: String? = form.firstError
```

### Observing Form State

```kotlin
val isValid     by form.isValid.collectAsState()
val isDirty     by form.isDirty.collectAsState()
val isSubmitting by form.isSubmitting.collectAsState()

// Per field
val emailError  by form.field<String>("email").error.collectAsState()
val isTouched   by form.field<String>("email").isTouched.collectAsState()
val isValidating by form.field<String>("email").isValidating.collectAsState()

// Disable button while submitting
Button(
    onClick = { form.submit { /* ... */ } },
    enabled = !isSubmitting
) {
    Text(if (isSubmitting) "Loading..." else "Submit")
}
```

---

## Multi-Step Forms

```kotlin
val wizard = MultiStepFormState(scope = rememberCoroutineScope()) {
    step {                                       // Step 0 — Personal Info
        field("firstName") { rules(Required) }
        field("lastName")  { rules(Required) }
        field("email")     { rules(Required, Email) }
    }
    step {                                       // Step 1 — Account
        field("username")        { rules(Required, MinLength(3)) }
        field("password")        { rules(Required, StrongPassword) }
        field("confirmPassword") { rules(Required, Matches("password")) }
    }
    step {                                       // Step 2 — Preferences
        field("country")         { rules(Required) }
        boolField("acceptTerms") { rules(MustBeTrue) }
    }
}

// Navigation
wizard.nextStep()      // validates current step → advances if valid → returns Boolean
wizard.previousStep()  // goes back without validation
wizard.goToStep(2)     // validates all intermediate steps before jumping

// Observable step index
val stepIndex by wizard.currentStepIndex.collectAsState()

// On the last step
if (wizard.isLastStep) {
    wizard.submit { data ->
        // data contains keys from ALL steps merged
        viewModel.register(data)
    }
}
```

---

## Built-In Rules Reference

### Text Rules

| Rule | Description |
|------|-------------|
| `Required` | Not blank or whitespace |
| `Required(message)` | Required with a custom message |
| `MinLength(n)` | At least n characters |
| `MaxLength(n)` | At most n characters |
| `ExactLength(n)` | Exactly n characters |
| `Email` | Standard email format |
| `Url` | http/https URL |
| `PhoneNumber` | International phone number |
| `Pattern(regex, msg)` | Custom regex pattern |
| `NoWhitespace` | No space characters |
| `AlphaOnly` | Letters only |
| `AlphaNumeric` | Letters and digits only |
| `HasUppercase` | At least one uppercase letter |
| `HasDigit` | At least one digit |
| `HasSpecialChar` | At least one special character |
| `StrongPassword` | 8+ chars, uppercase, digit, special character |

### Number Rules

| Rule | Description |
|------|-------------|
| `Min(n)` | Integer >= n |
| `Max(n)` | Integer <= n |
| `Between(min, max)` | Integer in range |
| `Positive` | Integer > 0 |
| `NonNegative` | Integer >= 0 |
| `IsNumeric` | String parses to Int |

### Boolean Rules

| Rule | Description |
|------|-------------|
| `MustBeTrue` | Must be checked (accept terms, confirm action) |
| `MustBeFalse` | Must be unchecked |

### Cross-Field Rules

| Rule | Description |
|------|-------------|
| `Matches(fieldKey)` | Value must equal another field's value |
| `NotMatches(fieldKey)` | Value must differ from another field's value |

---

## Custom Rules

### Synchronous

```kotlin
// Lambda style (recommended)
val StartsWithREF = Validator<String> { value ->
    if (value.startsWith("REF-")) ValidationResult.Success
    else ValidationResult.Error("Must start with REF-")
}

// Parameterized factory
fun StartsWith(prefix: String) = Validator<String> { value ->
    if (value.startsWith(prefix)) ValidationResult.Success
    else ValidationResult.Error("Must start with $prefix")
}

// Usage
field("orderId") { rules(Required, StartsWith("ORD-")) }
```

### Asynchronous

```kotlin
val CheckPromoCode = AsyncValidator<String> { value ->
    val valid = promoRepository.isValid(value)  // suspend API call
    if (valid) ValidationResult.Success
    else ValidationResult.Error("Invalid or expired promo code")
}

field("promoCode") {
    rules(Required)
    asyncRule("promo_valid") { value -> CheckPromoCode.validate(value) }
    debounce(500L)
}
```

### Custom Cross-Field Rule

```kotlin
class SumEquals(private val otherKey: String, private val total: Int) :
    Validator<Int>, ReaderAware {

    private var reader: ((String) -> Any?)? = null

    override fun injectFieldReader(reader: (String) -> Any?) {
        this.reader = reader
    }

    override fun validate(value: Int): ValidationResult {
        val other = reader?.invoke(otherKey) as? Int ?: 0
        return if (value + other == total) ValidationResult.Success
        else ValidationResult.Error("Values must sum to $total")
    }
}
```

---

## Compose Components

### `FormTextField`

```kotlin
FormTextField(
    fieldState = form.field("email"),
    label = "Email Address",
    placeholder = "you@example.com",
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    visualTransformation = PasswordVisualTransformation(), // for password fields
    modifier = Modifier.fillMaxWidth()
)
```

Automatically handles: value binding, `onFocusGained`, `onFocusLost`, error display (shown only after the user has touched the field).

### `FormCheckbox`

```kotlin
FormCheckbox(
    fieldState = form.field("acceptTerms"),
    label = "I accept the Terms and Conditions"
)
```

### `FormDropdown`

```kotlin
FormDropdown(
    fieldState = form.field("country"),
    label = "Country",
    options = listOf("United States", "United Kingdom", "Canada", "Australia")
)
```

---

## Using with ViewModel

For forms that must survive configuration changes, create `FormState` in the ViewModel using `viewModelScope`:

```kotlin
class SignUpViewModel : ViewModel() {

    val form = FormState(scope = viewModelScope) {
        field("email")    { rules(Required, Email) }
        field("password") { rules(Required, StrongPassword) }
    }

    fun signUp() {
        form.submit { data ->
            repository.createAccount(
                email    = data["email"] as String,
                password = data["password"] as String
            )
        }
    }
}

// In Composable — no rememberFormState needed
@Composable
fun SignUpScreen(viewModel: SignUpViewModel = viewModel()) {
    val isSubmitting by viewModel.form.isSubmitting.collectAsState()

    FormTextField(fieldState = viewModel.form.field("email"), label = "Email")
    // ...
}
```

---

## Module Structure

```
io.formcraft
├── formcraft-core          Pure Kotlin, Kotlin Multiplatform (JVM + Android)
│   └── io.formcraft.core
│       ├── FormState           Central form controller
│       ├── FieldState          Observable state per field
│       ├── MultiStepFormState  Multi-step wizard
│       ├── ValidationResult    sealed: Success | Error
│       ├── ValidationStrategy  enum: OnChange | OnFocusLost | OnSubmit
│       ├── Validator           fun interface for sync rules
│       ├── AsyncValidator      fun interface for async rules
│       └── rules/              25+ built-in validators
│
└── formcraft-compose       Android only (depends on formcraft-core via api())
    └── io.formcraft.compose
        ├── rememberFormState   Compose-aware factory
        └── components/
            ├── FormTextField
            ├── FormCheckbox
            └── FormDropdown
```

---

## Sample App

The repository includes a sample app demonstrating all features:

| Screen | What it shows |
|--------|--------------|
| **Login Form** | Email + password, `OnChange` strategy |
| **Sign Up Form** | Cross-field `Matches`, `StrongPassword`, `MustBeTrue` checkbox |
| **Checkout Form** | Multi-column layout, card number, expiry pattern, CVV |
| **Multi-Step Wizard** | 3-step registration with animated slide transitions and a step progress indicator |

To run the sample app, open the project in Android Studio and run the `:sample-app` configuration.

---

## Testing

FormCraft is designed to be testable without Android or Compose:

```kotlin
class FormStateTest {

    @Test
    fun `invalid email shows error`() = runTest {
        val form = FormState {
            field("email") { rules(Required, Email) }
        }
        form.field<String>("email").onValueChange("notanemail")
        assertEquals("Enter a valid email address", form.field<String>("email").currentError)
    }

    @Test
    fun `async rule uses test scope`() = runTest {
        val form = FormState(scope = this) {
            field("username") {
                rules(Required)
                asyncRule("unique") { _ -> ValidationResult.Error("Taken") }
                debounce(0L)
            }
        }
        form.field<String>("username").onValueChange("taken")
        advanceUntilIdle()
        assertEquals("Taken", form.field<String>("username").currentError)
    }
}
```

Run all tests:

```bash
./gradlew :formcraft-core:jvmTest
```

---

## Contributing

Contributions are welcome. Please follow these steps:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/date-rules`
3. Write tests for your change
4. Ensure all tests pass: `./gradlew :formcraft-core:jvmTest`
5. Ensure the compose module builds: `./gradlew :formcraft-compose:build`
6. Open a pull request with a clear description of the change

### What to contribute

- New built-in rules (Date rules, `NotEmpty` for collections, etc.)
- Additional Compose components (`FormDatePicker`, `FormRadioGroup`, etc.)
- iOS/SwiftUI integration module
- Dokka documentation
- Bug reports via GitHub Issues

---

## Roadmap

- [ ] `DateRules` using `kotlinx-datetime`
- [ ] `FormDatePicker` and `FormRadioGroup` components
- [ ] iOS SwiftUI integration module (`formcraft-swiftui`)
- [ ] Dokka-generated API documentation site
- [ ] `formcraft-viewmodel` convenience module
- [ ] Localization support — error messages from `stringResource`
- [ ] Compose multiplatform support (desktop, web)

---

## License

```
Copyright 2025 Sundram Singh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## Acknowledgements

- [Jetpack Compose](https://developer.android.com/jetpack/compose) — Android's modern UI toolkit
- [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines) — Async/concurrency primitives
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) — Share code across platforms

---

<p align="center">
  Built by <a href="https://github.com/sundramsingh">Sundram Singh</a>
</p>
