package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ExpenseWithCategory
import com.example.ui.widgets.CustomBarChart
import com.example.ui.widgets.CustomPieChart
import com.example.ui.widgets.CustomTrendLineChart
import com.example.ui.widgets.ChartPalette
import com.example.viewmodel.ExpenseTrackerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: ExpenseTrackerViewModel,
    expenses: List<ExpenseWithCategory>,
    onBack: () -> Unit
) {
    val stats = remember(expenses) { viewModel.computeStats(expenses) }
    val currencySymbol = viewModel.getCurrencySymbol()

    // 1. Calculate Monthly Expense Bar Chart (last 5 months)
    val monthlyData = remember(expenses) {
        val data = mutableMapOf<String, Double>()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val labelSdf = SimpleDateFormat("MMM", Locale.getDefault())

        // Collect last 5 months
        for (i in 4 downTo 0) {
            val pastCal = Calendar.getInstance()
            pastCal.add(Calendar.MONTH, -i)
            val monthKey = sdf.format(pastCal.time)
            val monthLabel = labelSdf.format(pastCal.time)
            
            val total = expenses.filter { it.expense.expenseDate.startsWith(monthKey) }
                .sumOf { it.expense.amount }
            data[monthLabel] = total
        }
        data
    }

    // 2. Calculate Last 7 Days trend Points and Labels
    val (trendPoints, trendLabels) = remember(expenses) {
        val points = mutableListOf<Double>()
        val labels = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -6)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val labelSdf = SimpleDateFormat("E", Locale.getDefault()) // Mon, Tue, etc.

        for (i in 0..6) {
            val dateStr = sdf.format(cal.time)
            val labelStr = labelSdf.format(cal.time)
            val dayTotal = expenses.filter { it.expense.expenseDate == dateStr }.sumOf { it.expense.amount }
            points.add(dayTotal)
            labels.add(labelStr)
            cal.add(Calendar.DATE, 1)
        }
        Pair(points, labels)
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
                    text = "Reports & Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Dynamic Tab Summaries (Daily, Weekly, Monthly Summary cards)
            Text(
                text = "Summary Reports",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Daily Report Card
                ReportSumCard(
                    title = "Today",
                    total = stats.todayTotal,
                    subText = "${stats.todayCount} transactions",
                    modifier = Modifier.weight(1f)
                )

                // Weekly Report Card
                ReportSumCard(
                    title = "This Week",
                    total = stats.weekTotal,
                    subText = "Weekly limit dynamic",
                    modifier = Modifier.weight(1f)
                )

                // Monthly Report Card
                ReportSumCard(
                    title = "This Month",
                    total = stats.monthTotal,
                    subText = "Monthly summary",
                    modifier = Modifier.weight(1f)
                )
            }

            // Pie Distribution card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Category Expenses (Pie)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    CustomPieChart(
                        data = stats.categoryWiseSummary,
                        currencySymbol = currencySymbol
                    )
                }
            }

            // Bar Chart card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Monthly Spending Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    CustomBarChart(
                        data = monthlyData,
                        currencySymbol = currencySymbol,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }

            // Trend line card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Weekly Activity Trend (7 Days)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    CustomTrendLineChart(
                        points = trendPoints,
                        labels = trendLabels,
                        currencySymbol = currencySymbol,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }

            // Category Comparison list details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Category Comparison Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (stats.categoryWiseSummary.isEmpty()) {
                        Text(
                            text = "No category data to display standard comparison breakdown list.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        stats.categoryWiseSummary.entries.sortedByDescending { it.value }.forEachIndexed { idx, entry ->
                            val color = ChartPalette[idx % ChartPalette.size]
                            val progress = if (stats.allTimeTotal > 0) (entry.value / stats.allTimeTotal).toFloat() else 0.0f

                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = entry.key,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${currencySymbol}${String.format(Locale.getDefault(), "%.1f", entry.value)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    color = color,
                                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ReportSumCard(
    title: String,
    total: Double,
    subText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format(Locale.getDefault(), "$%.0f", total),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}
