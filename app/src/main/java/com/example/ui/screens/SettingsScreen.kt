package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ExpenseWithCategory
import com.example.ui.LanguageHelper
import com.example.viewmodel.ExpenseTrackerViewModel

@Composable
fun SettingsScreen(
    viewModel: ExpenseTrackerViewModel,
    expenses: List<ExpenseWithCategory>,
    onNavigateToCategories: () -> Unit
) {
    val context = LocalContext.current

    val themeMode by viewModel.themeMode.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val lang by viewModel.languageMode.collectAsState()

    var showCurrencyMenu by remember { mutableStateOf(false) }

    // Backup & Restore Picker Contract Setup
    val fileRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importDatabaseFromUri(context, uri) { success ->
                if (success) {
                    // Success, forces content updates globally
                }
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = LanguageHelper.translate("Settings", lang),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Category configuration item links
            Text(
                text = LanguageHelper.translate("Preferences", lang),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Removed Language Selection card as requested to stop language changing

            SettingItem(
                title = LanguageHelper.translate("Manage Categories", lang),
                subTitle = LanguageHelper.translate("Configure transaction categories and custom icons", lang),
                icon = Icons.Default.Category,
                onClick = onNavigateToCategories
            )

            // Theme Settings Row Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(LanguageHelper.translate("Visual Theme Mode", lang), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(LanguageHelper.translate("Configure application appearance theme", lang), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val modes = listOf("System", "Light", "Dark")
                        modes.forEach { mode ->
                            val isSelected = themeMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(mode) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }

            // Currency selection item
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .clickable { showCurrencyMenu = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(LanguageHelper.translate("Active Currency", lang), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(LanguageHelper.translate("Current selected currency symbol: ", lang) + viewModel.getCurrencySymbol(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box {
                        AssistChip(
                            onClick = { showCurrencyMenu = true },
                            label = { Text(selectedCurrency) }
                        )

                        DropdownMenu(
                            expanded = showCurrencyMenu,
                            onDismissRequest = { showCurrencyMenu = false }
                        ) {
                            listOf("USD", "EUR", "JPY", "MMK").forEach { cur ->
                                DropdownMenuItem(
                                    text = { Text(cur) },
                                    onClick = {
                                        viewModel.setSelectedCurrency(cur)
                                        showCurrencyMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Supabase Family Sync Section
            Text(
                text = "Family Cloud Sync",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val supabaseAccessToken by viewModel.supabaseAccessToken.collectAsState()
            val isLoggedIn = supabaseAccessToken.isNotEmpty()
            val userEmail by viewModel.supabaseUserEmail.collectAsState()
            val isSyncing by viewModel.supabaseIsSyncing.collectAsState()

            var isFamilySyncExpanded by remember(isLoggedIn) { mutableStateOf(!isLoggedIn) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isLoggedIn) {
                        // Clickable header for folding/unfolding when logged in
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isFamilySyncExpanded = !isFamilySyncExpanded },
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Connected with Family Account", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text("User Email: $userEmail", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(
                                imageVector = if (isFamilySyncExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isFamilySyncExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (isFamilySyncExpanded) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.syncWithSupabase { success ->
                                            if (success) {
                                                Toast.makeText(context, "Data synced successfully", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Sync failed", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    enabled = !isSyncing,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Syncing...")
                                    } else {
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Sync Now")
                                    }
                                }

                                OutlinedButton(
                                    onClick = { viewModel.supabaseSignOut() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Logout")
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Sign up or log in with a Supabase account to sync your shopping lists and expenses with other family members.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        var email by remember { mutableStateOf("") }
                        var password by remember { mutableStateOf("") }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        viewModel.supabaseSignIn(email, password) { success, message ->
                                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Please enter both Email and Password", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Login")
                            }

                            OutlinedButton(
                                onClick = {
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        viewModel.supabaseSignUp(email, password) { success, message ->
                                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Please enter both Email and Password", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign Up")
                            }
                        }
                    }
                }
            }

            // Export Section
            Text(
                text = LanguageHelper.translate("Export Records Report", lang),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (lang == "Myanmar") "မှတ်တမ်းများကို ဖိုင်အဖြစ် ထုတ်ယူသိမ်းဆည်းပါ။ လက်ရှိ သင့်စရိတ်မှတ်တမ်း ရလဒ်များအတိုင်း ဖိုင်ထုတ်ပေးမည် ဖြစ်သည်။" else "Generate and save local reports of matching records. Export targets the currently loaded results of history files.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.exportToCSV(context, expenses) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.TextSnippet, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (lang == "Myanmar") "CSV ထုတ်ယူရန်" else "Export CSV")
                        }

                        Button(
                            onClick = { viewModel.exportToPDF(context, expenses) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (lang == "Myanmar") "PDF ထုတ်ယူရန်" else "Export PDF")
                        }
                    }
                }
            }

            // Backup & Restore Database Section
            Text(
                text = if (lang == "Myanmar") "အရန်သိမ်းဆည်းမှုနှင့် စနစ်ပြန်လည်ရယူခြင်း" else "Backup and System Restore",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (lang == "Myanmar") "ဖုန်းအပြောင်းအလဲလုပ်လျှင် သို့မဟုတ် ဒေတာများမပျောက်ပျက်စေရန် ဒေတာဘေ့စ်ကို ဖိုင်အဖြစ် အရန်သိမ်းဆည်းနိုင်ပြီး ယခင်သိမ်းဆည်းထားသော အရန်ဖိုင်မှ ဒေတာများကိုလည်း ပြန်လည်သွင်းယူနိုင်ပါသည်။" else "Protect data from loss. Backup produces a local copy of SQLite database file. Restore imports and reloads database history.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.backupDatabaseFile(context) {} },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (lang == "Myanmar") "အရန်သိမ်းဆည်းမည်" else "Local Backup")
                        }

                        OutlinedButton(
                            onClick = { fileRestoreLauncher.launch(arrayOf("*/*")) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (lang == "Myanmar") "အရန်ဖိုင် ပြန်သွင်းမည်" else "Import Backup")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subTitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(subTitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
