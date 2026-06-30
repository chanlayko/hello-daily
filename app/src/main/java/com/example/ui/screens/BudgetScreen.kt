package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.entity.BudgetEntity
import com.example.data.entity.ExpenseWithCategory
import com.example.viewmodel.ExpenseTrackerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: ExpenseTrackerViewModel,
    budgets: List<BudgetEntity>,
    expenses: List<ExpenseWithCategory>,
    onBack: () -> Unit
) {
    var showSetBudgetDialog by remember { mutableStateOf<String?>(null) } // "daily", "weekly", "monthly"
    var budgetAmountInput by remember { mutableStateOf("") }
    
    val stats = remember(expenses) { viewModel.computeStats(expenses) }
    val currencySymbol = viewModel.getCurrencySymbol()

    val dailyBudget = budgets.find { it.budgetType == "daily" }
    val weeklyBudget = budgets.find { it.budgetType == "weekly" }
    val monthlyBudget = budgets.find { it.budgetType == "monthly" }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.padding(end = 4.dp)) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Budget Management",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Set limit thresholds to maintain healthy spending habits.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 1. Daily Budget Card
            BudgetStatusCard(
                typeLabel = "Daily Budget",
                amountUsed = stats.todayTotal,
                budgetLimit = dailyBudget?.amount ?: 0.0,
                currencySymbol = currencySymbol,
                onSetBudget = {
                    budgetAmountInput = dailyBudget?.amount?.toString() ?: ""
                    showSetBudgetDialog = "daily"
                }
            )

            // 2. Weekly Budget Card
            BudgetStatusCard(
                typeLabel = "Weekly Budget",
                amountUsed = stats.weekTotal,
                budgetLimit = weeklyBudget?.amount ?: 0.0,
                currencySymbol = currencySymbol,
                onSetBudget = {
                    budgetAmountInput = weeklyBudget?.amount?.toString() ?: ""
                    showSetBudgetDialog = "weekly"
                }
            )

            // 3. Monthly Budget Card
            BudgetStatusCard(
                typeLabel = "Monthly Budget",
                amountUsed = stats.monthTotal,
                budgetLimit = monthlyBudget?.amount ?: 0.0,
                currencySymbol = currencySymbol,
                onSetBudget = {
                    budgetAmountInput = monthlyBudget?.amount?.toString() ?: ""
                    showSetBudgetDialog = "monthly"
                }
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showSetBudgetDialog != null) {
        val type = showSetBudgetDialog!!
        var hasError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showSetBudgetDialog = null },
            title = { Text("Configure ${type.replaceFirstChar { it.uppercase() }} Budget") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter limit amount for your $type budget:")
                    OutlinedTextField(
                        value = budgetAmountInput,
                        onValueChange = {
                            budgetAmountInput = it
                            val am = it.toDoubleOrNull()
                            hasError = am == null || am < 0.0
                        },
                        label = { Text("Budget Amount *") },
                        leadingIcon = { Text("$currencySymbol ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = hasError,
                        singleLine = true,
                        supportingText = {
                            if (hasError) {
                                Text("Please enter a valid positive number", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = budgetAmountInput.toDoubleOrNull()
                        if (parsed != null && parsed >= 0.0) {
                            val nowStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            
                            // Estimate dates matching standard daily, weekly or monthly bounds
                            val cal = Calendar.getInstance()
                            val startStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                            
                            when (type) {
                                "daily" -> cal.add(Calendar.DATE, 0)
                                "weekly" -> cal.add(Calendar.DATE, 6)
                                "monthly" -> cal.add(Calendar.MONTH, 1)
                            }
                            val endStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

                            viewModel.setBudget(type, parsed, startStr, endStr)
                            showSetBudgetDialog = null
                        } else {
                            hasError = true
                        }
                    }
                ) {
                    Text("Set Limit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetBudgetDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BudgetStatusCard(
    typeLabel: String,
    amountUsed: Double,
    budgetLimit: Double,
    currencySymbol: String,
    onSetBudget: () -> Unit
) {
    val progress = if (budgetLimit > 0.0) (amountUsed / budgetLimit).toFloat() else 0.0f
    val pctUsed = progress * 100.0f
    
    val (statusColor, outlineGlow, isWarning) = when {
        budgetLimit <= 0.0 -> Triple(MaterialTheme.colorScheme.primary, Color.Transparent, false)
        progress >= 1.0f -> Triple(Color(0xFFEF4444), Color(0xFFFFECEC), true) // Red: Exceeded limits
        progress >= 0.8f -> Triple(Color(0xFFF59E0B), Color(0xFFFFF9EE), true) // Amber: Warning > 80%
        else -> Triple(Color(0xFF10B981), Color.Transparent, false) // Green: Safe zone
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isWarning) outlineGlow else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = if (isWarning) androidx.compose.foundation.BorderStroke(1.dp, statusColor) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isWarning) statusColor else MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = onSetBudget,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isWarning) statusColor else MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = if (budgetLimit <= 0.0) "Set Limit" else "Update", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (budgetLimit <= 0.0) {
                Text(
                    text = "No budget limit configured.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Amount Used", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, amountUsed),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Limit Budget", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = String.format(Locale.getDefault(), "%s%.2f", currencySymbol, budgetLimit),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar indicator
                LinearProgressIndicator(
                    progress = { progress.coerceAtMost(1.0f) },
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val remaining = budgetLimit - amountUsed
                    Text(
                        text = if (remaining >= 0.0) "Remaining: ${currencySymbol}${String.format(Locale.getDefault(), "%.1f", remaining)}"
                               else "Over Budget: ${currencySymbol}${String.format(Locale.getDefault(), "%.1f", Math.abs(remaining))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (remaining >= 0.0) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFEF4444),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f%% Used", pctUsed),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Alert notification banner inside card
                if (pctUsed >= 80.0f) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (pctUsed >= 100.0f) "Fatal Exceeded Limits! Restrain expenses."
                                   else "Warning threshold of 80% budget limit reached!",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
