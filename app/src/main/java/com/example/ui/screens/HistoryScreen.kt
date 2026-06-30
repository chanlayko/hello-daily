package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ExpenseWithCategory
import com.example.ui.widgets.ExpenseEntryDialog
import com.example.ui.widgets.getCategoryIcon
import com.example.viewmodel.ExpenseTrackerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: ExpenseTrackerViewModel,
    filteredExpenses: List<ExpenseWithCategory>,
    categories: List<CategoryEntity>,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Observe search and filters from ViewModel
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterCategoryId by viewModel.filterCategoryId.collectAsState()
    val filterStartDate by viewModel.filterStartDate.collectAsState()
    val filterEndDate by viewModel.filterEndDate.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    var showEditDialog by remember { mutableStateOf<ExpenseWithCategory?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<ExpenseWithCategory?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    val currencySymbol = viewModel.getCurrencySymbol()

    // Date range helper pickers
    val calendar = Calendar.getInstance()
    val startPicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val sel = Calendar.getInstance()
            sel.set(year, month, dayOfMonth)
            viewModel.filterStartDate.value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(sel.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val endPicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val sel = Calendar.getInstance()
            sel.set(year, month, dayOfMonth)
            viewModel.filterEndDate.value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(sel.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    text = "Transaction History",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search title, note, or category...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Horizontal Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "All" chip
                InputChip(
                    selected = filterCategoryId == null,
                    onClick = { viewModel.filterCategoryId.value = null },
                    label = { Text("All Categories") }
                )

                categories.forEach { category ->
                    InputChip(
                        selected = filterCategoryId == category.id,
                        onClick = { viewModel.filterCategoryId.value = category.id },
                        label = { Text(category.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(category.icon),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Date Range pickers and Sort buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Start date chip trigger
                    AssistChip(
                        onClick = { startPicker.show() },
                        label = { Text(filterStartDate ?: "Start Date") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        trailingIcon = {
                            if (filterStartDate != null) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { viewModel.filterStartDate.value = null }
                                )
                            }
                        }
                    )

                    // End date chip trigger
                    AssistChip(
                        onClick = { endPicker.show() },
                        label = { Text(filterEndDate ?: "End Date") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        trailingIcon = {
                            if (filterEndDate != null) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { viewModel.filterEndDate.value = null }
                                )
                            }
                        }
                    )
                }

                // Sort Dropdown button
                Box {
                    AssistChip(
                        onClick = { showSortMenu = true },
                        label = { Text(sortOption) },
                        leadingIcon = { Icon(Icons.Default.Sort, contentDescription = "Sort") }
                    )

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        listOf("Date (Newest)", "Date (Oldest)", "Amount (Highest)", "Amount (Lowest)").forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    viewModel.sortOption.value = opt
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Expenses summary count & Clear all filters helper
            val activeFilters = filterCategoryId != null || filterStartDate != null || filterEndDate != null || searchQuery.isNotEmpty()
            if (activeFilters) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Found ${filteredExpenses.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = {
                            viewModel.filterCategoryId.value = null
                            viewModel.filterStartDate.value = null
                            viewModel.filterEndDate.value = null
                            viewModel.searchQuery.value = ""
                        }
                    ) {
                        Text("Clear Filters", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Transactions list or Empty state helper
            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No match",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No recorded transactions match.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Try clearing queries or adjust date ranges.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredExpenses, key = { it.expense.id }) { item ->
                        ExpenseItemRow(
                            item = item,
                            currencySymbol = currencySymbol,
                            onEdit = { showEditDialog = item },
                            onDelete = { showDeleteConfirmDialog = item }
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog != null) {
        ExpenseEntryDialog(
            categories = categories,
            existingExpense = showEditDialog,
            onDismiss = { showEditDialog = null },
            onSave = { title, amount, categoryId, date, note ->
                viewModel.updateExpense(showEditDialog!!.expense.id, title, amount, categoryId, date, note)
                showEditDialog = null
            }
        )
    }

    if (showDeleteConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Transaction?") },
            text = { Text("Are you sure you want to permanently delete this expense? This action is offline synchronous and immediate.") },
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
