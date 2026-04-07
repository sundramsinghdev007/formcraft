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
import androidx.compose.ui.unit.dp
import io.formcraft.compose.components.FormTextField
import io.formcraft.compose.rememberFormState
import io.formcraft.core.rules.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutFormScreen(onBack: () -> Unit) {
    val form = rememberFormState {
        // Shipping
        field("fullName")  { rules(Required, MinLength(2)) }
        field("address")   { rules(Required, MinLength(5)) }
        field("city")      { rules(Required) }
        field("zip")       { rules(Required, ExactLength(5, "ZIP must be 5 digits"), IsNumeric) }
        // Payment
        field("cardNumber") { rules(Required, ExactLength(16, "Card number must be 16 digits"), IsNumeric) }
        field("expiry")     { rules(Required, Pattern("^(0[1-9]|1[0-2])/[0-9]{2}$", "Use MM/YY format")) }
        field("cvv")        { rules(Required, MinLength(3), MaxLength(4), IsNumeric) }
    }

    val isSubmitting by form.isSubmitting.collectAsState()
    var submitted by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Shipping ─────────────────────────────────────────────────────
            Text("Shipping Address", style = MaterialTheme.typography.titleMedium)

            FormTextField(fieldState = form.field("fullName"), label = "Full Name")
            FormTextField(fieldState = form.field("address"), label = "Street Address")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormTextField(
                    fieldState = form.field("city"),
                    label = "City",
                    modifier = Modifier.weight(1f)
                )
                FormTextField(
                    fieldState = form.field("zip"),
                    label = "ZIP",
                    modifier = Modifier.weight(0.6f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Payment ───────────────────────────────────────────────────────
            Text("Payment Details", style = MaterialTheme.typography.titleMedium)

            FormTextField(
                fieldState = form.field("cardNumber"),
                label = "Card Number",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormTextField(
                    fieldState = form.field("expiry"),
                    label = "Expiry (MM/YY)",
                    modifier = Modifier.weight(1f)
                )
                FormTextField(
                    fieldState = form.field("cvv"),
                    label = "CVV",
                    modifier = Modifier.weight(0.6f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
            }

            if (submitted) {
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )) {
                    Text(
                        "Order placed for ${form.field<String>("fullName").currentValue}!",
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
                        modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Place Order")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
