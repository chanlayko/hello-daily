package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LanguageHelper
import com.example.viewmodel.ExpenseTrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: ExpenseTrackerViewModel,
    onLoginSuccess: () -> Unit,
    onBypass: () -> Unit
) {
    val context = LocalContext.current
    val isLoggedIn = viewModel.isSupabaseLoggedIn()

    // Auto-navigate to dashboard if already logged in
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onLoginSuccess()
        }
    }

    val lang by viewModel.languageMode.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .testTag("login_screen_container")
    ) {
        // Quick language switcher removed as requested to stop language changing

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo or Icon Indicator
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudQueue,
                    contentDescription = "Cloud Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Burmese and English Title Header
            Text(
                text = "မိသားစု သုံးစွဲမှုမှတ်တမ်း",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Family Cloud Expense Sync",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("အီးမေးလ်လိပ်စာ (Email)") },
                placeholder = { Text("example@gmail.com") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "Email")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("စကားဝှက် (Password)") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Password")
                },
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = "Toggle password")
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Login Button
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Email နှင့် Password ထည့်သွင်းပေးပါ", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    viewModel.supabaseSignIn(email, password) { success, message ->
                        isLoading = false
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        if (success) {
                            onLoginSuccess()
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_submit_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "လော့ဂ်အင်ဝင်မည် (Login)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Register / Sign Up Button
            OutlinedButton(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Email နှင့် Password ထည့်သွင်းပေးပါ", Toast.LENGTH_SHORT).show()
                        return@OutlinedButton
                    }
                    isLoading = true
                    viewModel.supabaseSignUp(email, password) { success, message ->
                        isLoading = false
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_signup_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "အကောင့်အသစ်ဖွင့်မည် (Register)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Auto-Create 2 Demo Accounts Button
            var isCreatingDemo by remember { mutableStateOf(false) }
            TextButton(
                onClick = {
                    isCreatingDemo = true
                    Toast.makeText(context, "ပထမအကောင့် (demo1@gmail.com) ကို စတင်ဖန်တီးနေပါသည်...", Toast.LENGTH_SHORT).show()
                    viewModel.supabaseSignUp("demo1@gmail.com", "password123") { success1, msg1 ->
                        if (success1) {
                            Toast.makeText(context, "ပထမအကောင့် အောင်မြင်စွာ ဖန်တီးပြီးပါပြီ။ ဒုတိယအကောင့် (demo2@gmail.com) ကို ဆက်လက်ဖန်တီးနေပါသည်...", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "ပထမအကောင့် ဖန်တီးခြင်း: $msg1", Toast.LENGTH_SHORT).show()
                        }
                        
                        viewModel.supabaseSignUp("demo2@gmail.com", "password123") { success2, msg2 ->
                            isCreatingDemo = false
                            if (success2) {
                                Toast.makeText(context, "စမ်းသပ်ရန် အကောင့် ၂ခုစလုံးကို Supabase တွင် အောင်မြင်စွာ ထည့်သွင်းပြီးပါပြီ။", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "ဒုတိယအကောင့် ဖန်တီးခြင်း: $msg2", Toast.LENGTH_SHORT).show()
                            }
                            email = "demo1@gmail.com"
                            password = "password123"
                        }
                    }
                },
                enabled = !isLoading && !isCreatingDemo,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_auto_create_demo_button")
            ) {
                if (isCreatingDemo) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "အကောင့်များ ဖန်တီးနေသည်...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "✨ စမ်းသပ်ရန် အကောင့် ၂ခု အလိုအလျောက်ဖွင့်မည် (Auto-Create 2 Demo Accounts)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
                Text(
                    text = "သို့မဟုတ်",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Continue Offline (Bypass)
            TextButton(
                onClick = onBypass,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_bypass_button")
            ) {
                Text(
                    text = "အော့ဖ်လိုင်း ဆက်လက်အသုံးပြုမည် (Continue Offline)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
