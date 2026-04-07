package com.sundram.formcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sundram.formcraft.screens.*
import com.sundram.formcraft.ui.theme.FormCraftTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FormCraftTheme {
                FormCraftApp()
            }
        }
    }
}

private enum class Screen {
    Home, Login, SignUp, Checkout, MultiStep
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormCraftApp() {
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    BackHandler(enabled = currentScreen != Screen.Home) {
        currentScreen = Screen.Home
    }

    when (currentScreen) {
        Screen.Home     -> HomeScreen(onNavigate = { currentScreen = it })
        Screen.Login    -> LoginFormScreen(onBack = { currentScreen = Screen.Home })
        Screen.SignUp   -> SignUpFormScreen(onBack = { currentScreen = Screen.Home })
        Screen.Checkout -> CheckoutFormScreen(onBack = { currentScreen = Screen.Home })
        Screen.MultiStep -> MultiStepFormScreen(onBack = { currentScreen = Screen.Home })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(onNavigate: (Screen) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FormCraft Demos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                "Select a demo to explore FormCraft's capabilities",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            DemoCard(
                title = "Login Form",
                description = "Email + password with OnChange validation",
                onClick = { onNavigate(Screen.Login) }
            )
            DemoCard(
                title = "Sign Up Form",
                description = "Cross-field validation (Matches), StrongPassword rule, MustBeTrue checkbox",
                onClick = { onNavigate(Screen.SignUp) }
            )
            DemoCard(
                title = "Checkout Form",
                description = "Multi-column layout, card number, expiry pattern, CVV validation",
                onClick = { onNavigate(Screen.Checkout) }
            )
            DemoCard(
                title = "Multi-Step Wizard",
                description = "3-step registration with animated transitions and step indicator",
                onClick = { onNavigate(Screen.MultiStep) }
            )

            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "About FormCraft",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "Zero Kotlin reflection",
                        "Async validation with debounce",
                        "Cross-field rules (Matches, NotMatches)",
                        "Per-field validation strategies",
                        "Multi-step form support",
                        "Kotlin Multiplatform ready"
                    ).forEach { feature ->
                        Text(
                            "• $feature",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DemoCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
