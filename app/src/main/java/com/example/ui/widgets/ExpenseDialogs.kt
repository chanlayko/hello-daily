package com.example.ui.widgets

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ExpenseWithCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryDialog(
    categories: List<CategoryEntity>,
    existingExpense: ExpenseWithCategory? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, categoryId: Int, date: String, note: String) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(existingExpense?.expense?.title ?: "") }
    var amountText by remember { mutableStateOf(existingExpense?.expense?.amount?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(existingExpense?.category ?: categories.firstOrNull()) }
    var note by remember { mutableStateOf(existingExpense?.expense?.note ?: "") }
    
    // Default to the current date or edited date
    var expenseDate by remember { mutableStateOf(existingExpense?.expense?.expenseDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }

    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Date Picker initialization
    val calendar = Calendar.getInstance()
    if (expenseDate.isNotEmpty()) {
        try {
            val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expenseDate)
            if (parsedDate != null) {
                calendar.time = parsedDate
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            expenseDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingExpense == null) "Add Expense" else "Edit Expense",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = it.trim().isEmpty()
                    },
                    label = { Text("Expense Title *") },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = titleError,
                    singleLine = true,
                    supportingText = {
                        if (titleError) {
                            Text("Title is required", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        val am = it.toDoubleOrNull()
                        amountError = am == null || am <= 0.0
                    },
                    label = { Text("Expense Amount *") },
                    leadingIcon = { Text("$ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = amountError,
                    singleLine = true,
                    supportingText = {
                        if (amountError) {
                            Text("Amount must be a positive number", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                // Category Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category *") },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(selectedCategory?.icon ?: ""),
                                contentDescription = null
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = getCategoryIcon(category.icon),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(category.name)
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date Picker Field
                OutlinedTextField(
                    value = expenseDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Expense Date *") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Pick Date",
                            modifier = Modifier.clickable { datePickerDialog.show() }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                )

                // Note Field
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val isTInvalid = title.trim().isEmpty()
                    val amVal = amountText.toDoubleOrNull()
                    val isAInvalid = amVal == null || amVal <= 0.0

                    titleError = isTInvalid
                    amountError = isAInvalid

                    if (!isTInvalid && !isAInvalid && selectedCategory != null) {
                        onSave(title.trim(), amVal!!, selectedCategory!!.id, expenseDate, note.trim())
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
