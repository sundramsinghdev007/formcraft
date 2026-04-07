package com.sundram.formcraft.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.formcraft.compose.components.FormCheckbox
import io.formcraft.compose.components.FormDropdown
import io.formcraft.compose.components.FormTextField
import io.formcraft.core.MultiStepFormState
import io.formcraft.core.ValidationStrategy
import io.formcraft.core.rules.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiStepFormScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val wizard = remember {
        MultiStepFormState(scope = scope) {
            step {                                           // Step 1 — Personal Info
                field("firstName") { rules(Required, MinLength(2)) }
                field("lastName")  { rules(Required, MinLength(2)) }
                field("email")     { rules(Required, Email) }
                field("phone")     { rules(Required, PhoneNumber) }
            }
            step {                                           // Step 2 — Account Setup
                field("username")        { rules(Required, MinLength(3), AlphaNumeric) }
                field("password")        { rules(Required, StrongPassword) }
                field("confirmPassword") { rules(Required, Matches("password")) }
            }
            step {                                           // Step 3 — Preferences
                field("country")        { rules(Required) }
                boolField("newsletter") { }
                boolField("acceptTerms") { rules(MustBeTrue) }
            }
        }
    }

    val currentStepIndex by wizard.currentStepIndex.collectAsState()
    val isSubmitting by wizard.isSubmitting.collectAsState()
    var submitted by remember { mutableStateOf(false) }

    val stepTitles = listOf("Personal Info", "Account Setup", "Preferences")
    val countries = listOf("United States", "United Kingdom", "Canada", "Australia", "India", "Germany", "France", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registration Wizard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // ── Step indicator ───────────────────────────────────────────────
            StepIndicator(
                steps = stepTitles,
                currentStep = currentStepIndex,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            HorizontalDivider()

            // ── Animated step content ────────────────────────────────────────
            AnimatedContent(
                targetState = currentStepIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    } else {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                label = "step_content"
            ) { step ->
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(stepTitles[step], style = MaterialTheme.typography.titleLarge)

                    when (step) {
                        0 -> {
                            val s = wizard.step(0)
                            FormTextField(fieldState = s.field("firstName"), label = "First Name")
                            FormTextField(fieldState = s.field("lastName"),  label = "Last Name")
                            FormTextField(
                                fieldState = s.field("email"), label = "Email",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                            FormTextField(
                                fieldState = s.field("phone"), label = "Phone",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                        }
                        1 -> {
                            val s = wizard.step(1)
                            FormTextField(fieldState = s.field("username"), label = "Username")
                            FormTextField(
                                fieldState = s.field("password"), label = "Password",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = PasswordVisualTransformation()
                            )
                            FormTextField(
                                fieldState = s.field("confirmPassword"), label = "Confirm Password",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = PasswordVisualTransformation()
                            )
                        }
                        2 -> {
                            val s = wizard.step(2)
                            FormDropdown(
                                fieldState = s.field("country"),
                                label = "Country",
                                options = countries
                            )
                            FormCheckbox(
                                fieldState = s.field("newsletter"),
                                label = "Send me product updates and news"
                            )
                            FormCheckbox(
                                fieldState = s.field("acceptTerms"),
                                label = "I accept the Terms and Conditions"
                            )
                        }
                    }

                    if (submitted) {
                        Card(colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )) {
                            val name = "${wizard.step(0).field<String>("firstName").currentValue} " +
                                       "${wizard.step(0).field<String>("lastName").currentValue}"
                            Text(
                                "Welcome, $name! Registration complete.",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // ── Navigation buttons ───────────────────────────────────────────
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!wizard.isFirstStep) {
                    OutlinedButton(
                        onClick = { wizard.previousStep() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back")
                    }
                }

                Button(
                    onClick = {
                        if (wizard.isLastStep) {
                            wizard.submit { submitted = true }
                        } else {
                            wizard.nextStep()
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f)
                ) {
                    when {
                        isSubmitting -> CircularProgressIndicator(
                            modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        wizard.isLastStep -> Text("Submit")
                        else -> Text("Next")
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    steps: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, title ->
            val isActive    = index == currentStep
            val isCompleted = index < currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isActive    -> MaterialTheme.colorScheme.primaryContainer
                        else        -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isCompleted) "✓" else "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = when {
                                isCompleted -> MaterialTheme.colorScheme.onPrimary
                                isActive    -> MaterialTheme.colorScheme.onPrimaryContainer
                                else        -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (index < steps.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(bottom = 20.dp),
                    color = if (isCompleted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}
