package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BudgetEntity
import com.example.data.entity.ExpenseWithCategory
import com.example.ui.widgets.CustomPieChart
import com.example.ui.widgets.ExpenseEntryDialog
import com.example.ui.widgets.getCategoryIcon
import com.example.ui.LanguageHelper
import com.example.viewmodel.ExpenseTrackerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ExpenseTrackerViewModel,
    expenses: List<ExpenseWithCategory>,
    budgets: List<BudgetEntity>,
    onNavigateToHistory: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToShopping: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    val shoppingItems by viewModel.shoppingItems.collectAsState()
    val supabaseUserEmail by viewModel.supabaseUserEmail.collectAsState()
    val supabaseIsSyncing by viewModel.supabaseIsSyncing.collectAsState()
    val lang by viewModel.languageMode.collectAsState()
    val isLoggedIn = viewModel.isSupabaseLoggedIn()
    val activeShoppingCount = remember(shoppingItems) { shoppingItems.count { !it.shoppingItem.isBought } }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedExpenseForEdit by remember { mutableStateOf<ExpenseWithCategory?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<ExpenseWithCategory?>(null) }

    val stats = remember(expenses) { viewModel.computeStats(expenses) }
    val currencySymbol = viewModel.getCurrencySymbol()

    // Find active budgets and compute warnings
    val activeDailyBudget = budgets.find { it.budgetType == "daily" }
    val activeWeeklyBudget = budgets.find { it.budgetType == "weekly" }
    val activeMonthlyBudget = budgets.find { it.budgetType == "monthly" }

    // Check warning states (if usage >= 80% or >= 100%)
    val dailyProgress = activeDailyBudget?.let { stats.todayTotal / it.amount } ?: 0.0
    val weeklyProgress = activeWeeklyBudget?.let { stats.weekTotal / it.amount } ?: 0.0
    val monthlyProgress = activeMonthlyBudget?.let { stats.monthTotal / it.amount } ?: 0.0

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.testTag("add_expense_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Modern High Density Top App Bar & Profile Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val displayName = if (isLoggedIn && supabaseUserEmail.isNotBlank()) {
                            supabaseUserEmail.substringBefore("@")
                        } else {
                            "John Doe"
                        }
                        val firstLetter = displayName.firstOrNull()?.uppercase() ?: "J"

                        // Styled round avatar circle
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEADDFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = firstLetter,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D)
                            )
                        }
                        Column {
                            Text(
                                text = if (isLoggedIn) "Family Member" else "Welcome back,",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Top header action icons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (isLoggedIn) {
                            IconButton(onClick = {
                                viewModel.syncWithSupabase { success ->
                                    if (success) {
                                        Toast.makeText(context, "Data synced successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Sync failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }) {
                                if (supabaseIsSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = "Sync",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        IconButton(onClick = onNavigateToHistory) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Budget Alerts Banner
            item {
                val showAlert = dailyProgress >= 0.8 || weeklyProgress >= 0.8 || monthlyProgress >= 0.8
                AnimatedVisibility(visible = showAlert) {
                    val alertType = when {
                        dailyProgress >= 1.0 || weeklyProgress >= 1.0 || monthlyProgress >= 1.0 -> "Over-Budget"
                        else -> "Warning (80% Reached)"
                    }
                    val alertColor = if (alertType == "Over-Budget") MaterialTheme.colorScheme.errorContainer else Color(0xFFFDE4C3)
                    val alertTextColor = if (alertType == "Over-Budget") MaterialTheme.colorScheme.onErrorContainer else Color(0xFF7D4F00)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = alertColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToBudgets() }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Budget Alert",
                                tint = alertTextColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Budget Alert: $alertType",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = alertTextColor
                                )
                                Text(
                                    text = "You are approaching or have exceeded your spending limit. Tap to adjust budgets.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = alertTextColor.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            // 2. High Density Indigo Monthly Hero Card (replacing standard 4 stats cards)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                val currentMonthName = remember(lang) {
                                    val enMonth = SimpleDateFormat("MMMM", Locale.ENGLISH).format(Date())
                                    if (lang == "Myanmar") {
                                        when(enMonth) {
                                            "January" -> "ဇန်နဝါရီ"
                                            "February" -> "ဖေဖော်ဝါရီ"
                                            "March" -> "မတ်"
                                            "April" -> "ဧပြီ"
                                            "May" -> "မေ"
                                            "June" -> "ဇွန်"
                                            "July" -> "ဇူလိုင်"
                                            "August" -> "ဩဂုတ်"
                                            "September" -> "စက်တင်ဘာ"
                                            "October" -> "အောက်တိုဘာ"
                                            "November" -> "နိုဝင်ဘာ"
                                            "December" -> "ဒီဇင်ဘာ"
                                            else -> enMonth
                                        }
                                    } else {
                                        enMonth
                                    }
                                }
                                Text(
                                    text = if (lang == "Myanmar") "$currentMonthName စုစုပေါင်း" else "$currentMonthName Total".uppercase(Locale.getDefault()),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, stats.monthTotal),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            // Transaction progress pill label
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                val monthChange = stats.monthPctChange
                                val changeText = if (monthChange >= 0.0) "+${String.format(Locale.getDefault(), "%.1f%%", monthChange)}"
                                                  else String.format(Locale.getDefault(), "%.1f%%", monthChange)
                                Text(
                                    text = if (monthChange != 0.0) changeText else "+12.5%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Semi-transparent divider line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = LanguageHelper.translate("This Week", lang).uppercase(Locale.getDefault()),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, stats.weekTotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Vertically aligned custom divider line
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(34.dp)
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp)
                            ) {
                                Text(
                                    text = LanguageHelper.translate("Today", lang).uppercase(Locale.getDefault()),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, stats.todayTotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 3. Compact High Density Budget Tracker Widget
            item {
                val budgetLimit = activeMonthlyBudget?.amount ?: 0.0
                val spent = stats.monthTotal
                val progress = if (budgetLimit > 0) (spent / budgetLimit).toFloat() else 0.0f
                val pctUsed = progress * 100.0f
                val displayProgress = progress.coerceIn(0f, 1f)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToBudgets() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LanguageHelper.translate("Monthly Budget", lang),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (budgetLimit > 0.0) String.format(Locale.getDefault(), "%.0f%% Used", pctUsed) else "No Limit Set",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (pctUsed >= 100.0f) MaterialTheme.colorScheme.error else if (pctUsed >= 80.0f) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // High Density linear track indicating progress
                        LinearProgressIndicator(
                            progress = { displayProgress },
                            color = if (pctUsed >= 100.0f) MaterialTheme.colorScheme.error else if (pctUsed >= 80.0f) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Spent: ${String.format(Locale.getDefault(), "%s%.2f", currencySymbol, spent)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (budgetLimit > 0.0) {
                                val remaining = budgetLimit - spent
                                Text(
                                    text = if (remaining >= 0.0) "Remaining: ${currencySymbol}${String.format(Locale.getDefault(), "%.2f", remaining)}"
                                           else "Over limit: ${currencySymbol}${String.format(Locale.getDefault(), "%.2f", Math.abs(remaining))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (remaining >= 0.0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = "Set limit thresholds",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Quick Shopping List / Notes Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToShopping() }
                        .testTag("dashboard_shopping_notes_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Shopping",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = if (lang == "Myanmar") {
                                    if (activeShoppingCount > 0) "ဝယ်ရန်ကျန်သေးသောပစ္စည်း $activeShoppingCount ခု ရှိပါသည်။" else "ဝယ်စရာစာရင်းအားလုံး ဝယ်ယူပြီးပါပြီ။"
                                } else {
                                    if (activeShoppingCount > 0) "$activeShoppingCount pending items in shopping list." else "All shopping items purchased."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View List",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // 4. Quick Stats Grid (Top Category Spent)
            item {
                val topSpent = remember(stats.categoryWiseSummary) {
                    stats.categoryWiseSummary.entries
                        .filter { it.value > 0.0 }
                        .sortedByDescending { it.value }
                        .take(2)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category container 1
                    val cat1Name = topSpent.getOrNull(0)?.key ?: "Food & Drink"
                    val cat1Amount = topSpent.getOrNull(0)?.value ?: 0.0
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0BCFF)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = cat1Name.uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF49454F),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, cat1Amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D)
                            )
                        }
                    }

                    // Category container 2
                    val cat2Name = topSpent.getOrNull(1)?.key ?: "Entertainment"
                    val cat2Amount = topSpent.getOrNull(1)?.value ?: 0.0
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCAC4D0)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2FA))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = cat2Name.uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF49454F),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, cat2Amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B20)
                            )
                        }
                    }
                }
            }

            // Category summary distribution Pie Chart representation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = LanguageHelper.translate("Category Distribution", lang),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        CustomPieChart(
                            data = stats.categoryWiseSummary,
                            currencySymbol = currencySymbol
                        )
                    }
                }
            }

            // Recent transactions layout
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onNavigateToHistory) {
                        Text("See All", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (expenses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No expenses recorded yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(expenses.take(4)) { item ->
                    ExpenseItemRow(
                        item = item,
                        currencySymbol = currencySymbol,
                        onEdit = { selectedExpenseForEdit = item },
                        onDelete = { showDeleteConfirmDialog = item }
                    )
                }
            }
        }
    }

    // Dialog state controllers
    if (showAddDialog) {
        ExpenseEntryDialog(
            categories = categories,
            onDismiss = { showAddDialog = false },
            onSave = { title, amount, categoryId, date, note ->
                viewModel.addExpense(title, amount, categoryId, date, note)
                showAddDialog = false
            }
        )
    }

    if (selectedExpenseForEdit != null) {
        ExpenseEntryDialog(
            categories = categories,
            existingExpense = selectedExpenseForEdit,
            onDismiss = { selectedExpenseForEdit = null },
            onSave = { title, amount, categoryId, date, note ->
                viewModel.updateExpense(selectedExpenseForEdit!!.expense.id, title, amount, categoryId, date, note)
                selectedExpenseForEdit = null
            }
        )
    }

    if (showDeleteConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense? This action cannot be undone.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        viewModel.deleteExpense(showDeleteConfirmDialog!!.expense)
                        showDeleteConfirmDialog = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatsCard(
    title: String,
    amount: Double,
    txCount: Int,
    pctChange: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    hidePercentage: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$txCount Tx",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                )
                if (!hidePercentage) {
                    Spacer(modifier = Modifier.width(4.dp))
                    val pctColor = if (pctChange <= 0.0) Color(0xFF10B981) else Color(0xFFEF4444)
                    val icon = if (pctChange <= 0.0) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp
                    
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = pctColor,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f%%", Math.abs(pctChange)),
                        style = MaterialTheme.typography.labelSmall,
                        color = pctColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseItemRow(
    item: ExpenseWithCategory,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryNameLower = (item.category?.name ?: "other").lowercase(Locale.getDefault())
    
    // Dynamic styling based on category classification
    val (badgeBg, badgeTint) = when {
        categoryNameLower.contains("food") || categoryNameLower.contains("shopping") || categoryNameLower.contains("grocery") || categoryNameLower.contains("groceries") -> {
            Pair(Color(0xFFFAD8D8), Color(0xFFB3261E))
        }
        categoryNameLower.contains("salary") || categoryNameLower.contains("income") || categoryNameLower.contains("gift") || categoryNameLower.contains("deposit") -> {
            Pair(Color(0xFFDCFCE7), Color(0xFF146C2E))
        }
        else -> {
            Pair(Color(0xFFEADDFF), Color(0xFF21005D))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_card_${item.expense.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // High density styled custom shape badge box
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(item.category?.icon ?: ""),
                        contentDescription = item.category?.name,
                        tint = badgeTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.expense.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.category?.name ?: "Other"} • ${item.expense.expenseDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "-%s%.2f", currencySymbol, item.expense.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB3261E),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
