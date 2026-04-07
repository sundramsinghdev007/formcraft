package com.sundram.formcraft.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.formcraft.compose.components.FormCheckbox
import io.formcraft.compose.components.FormTextField
import io.formcraft.compose.rememberFormState
import io.formcraft.core.rules.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpFormScreen(onBack: () -> Unit) {
    val form = rememberFormState {
        field("name")            { rules(Required, MinLength(2)) }
        field("email")           { rules(Required, Email) }
        field("password")        { rules(Required, StrongPassword) }
        field("confirmPassword") { rules(Required, Matches("password")) }
        boolField("acceptTerms") { rules(MustBeTrue) }
    }

    val isSubmitting by form.isSubmitting.collectAsState()
    var submitted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text("Join FormCraft", style = MaterialTheme.typography.headlineMedium)
            Text("Fill in the details below", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(8.dp))

            FormTextField(
                fieldState = form.field("name"),
                label = "Full Name"
            )

            FormTextField(
                fieldState = form.field("email"),
                label = "Email Address",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            FormTextField(
                fieldState = form.field("password"),
                label = "Password",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )

            FormTextField(
                fieldState = form.field("confirmPassword"),
                label = "Confirm Password",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )

            FormCheckbox(
                fieldState = form.field("acceptTerms"),
                label = "I accept the Terms and Conditions"
            )

            if (submitted) {
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )) {
                    Text(
                        "Account created for ${form.field<String>("name").currentValue}!",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Button(
                onClick = { form.submit { submitted = true } },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Account")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
