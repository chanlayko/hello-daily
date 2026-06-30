package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ShoppingItemEntity
import com.example.data.entity.ShoppingItemWithCategory
import com.example.ui.widgets.getCategoryIcon
import com.example.viewmodel.ExpenseTrackerViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ExpenseTrackerViewModel,
    shoppingItems: List<ShoppingItemWithCategory>,
    categories: List<CategoryEntity>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currencySymbol = viewModel.getCurrencySymbol()
    val lang by viewModel.languageMode.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedItemForEdit by remember { mutableStateOf<ShoppingItemWithCategory?>(null) }
    var selectedItemForBuy by remember { mutableStateOf<ShoppingItemWithCategory?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<ShoppingItemWithCategory?>(null) }

    // Split items into "To Buy" (active) and "Bought" (completed)
    val activeItems = shoppingItems.filter { !it.shoppingItem.isBought }
    val boughtItems = shoppingItems.filter { it.shoppingItem.isBought }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (lang == "Myanmar") "ဝယ်စရာ စာရင်းမှတ်စု" else "Shopping Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(if (lang == "Myanmar") "ဝယ်စရာ စာရင်းမှတ်တမ်း" else "List of items to buy", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.testTag("add_shopping_item_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Shopping Note")
            }
        }
    ) { innerPadding ->
        if (shoppingItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Shopping",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = if (lang == "Myanmar") "ဝယ်စရာစာရင်း မရှိသေးပါ" else "No shopping items yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (lang == "Myanmar") {
                            "ကိုယ်ဝယ်ရမယ့်ပစ္စည်းတွေကို Note ထားပြီး လိုအပ်တဲ့အချိန်မှာ အလွယ်တကူ စာရင်းသွင်းနိုင်ပါတယ်။ အသစ်ထည့်ရန် '+' ခလုတ်ကို နှိပ်ပါ။"
                        } else {
                            "Keep track of items you need to buy and easily record them as expenses when bought. Press the '+' button to add a new item."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp, top = 8.dp)
            ) {
                // ACTIVE ITEMS SECTION
                if (activeItems.isNotEmpty()) {
                    item {
                        Text(
                            text = if (lang == "Myanmar") "ဝယ်ရန်ကျန်သေးသော စာရင်း (${activeItems.size})" else "Active Items (${activeItems.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(activeItems, key = { it.shoppingItem.id }) { item ->
                        ShoppingItemCard(
                            item = item,
                            currencySymbol = currencySymbol,
                            lang = lang,
                            onToggleBought = {
                                viewModel.toggleShoppingItemBought(item.shoppingItem) {
                                    Toast.makeText(
                                        context,
                                        if (lang == "Myanmar") "စရိတ်စာရင်း (Dashboard) သို့ အလိုအလျောက် ပေါင်းထည့်လိုက်ပါပြီ" else "Automatically recorded to dashboard expenses",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onEdit = { selectedItemForEdit = item },
                            onBuy = { selectedItemForBuy = item },
                            onDelete = { showDeleteConfirmDialog = item }
                        )
                    }
                }

                // COMPLETED ITEMS SECTION
                if (boughtItems.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (lang == "Myanmar") "ဝယ်ယူပြီးခဲ့သော စာရင်း (${boughtItems.size})" else "Completed Items (${boughtItems.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(boughtItems, key = { it.shoppingItem.id }) { item ->
                        ShoppingItemCard(
                            item = item,
                            currencySymbol = currencySymbol,
                            lang = lang,
                            onToggleBought = {
                                viewModel.toggleShoppingItemBought(item.shoppingItem) {
                                    Toast.makeText(
                                        context,
                                        if (lang == "Myanmar") "ဝယ်ရန်စာရင်းသို့ ပြန်လည်ရွှေ့လိုက်ပါပြီ" else "Moved back to active shopping list",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onEdit = { selectedItemForEdit = item },
                            onBuy = null, // Already bought
                            onDelete = { showDeleteConfirmDialog = item }
                        )
                    }
                }
            }
        }
    }

    // ADD DIALOG
    if (showAddDialog) {
        ShoppingItemEditDialog(
            title = if (lang == "Myanmar") "ဝယ်စရာအသစ်ထည့်ရန် (Add Item)" else "Add New Shopping Item",
            categories = categories,
            lang = lang,
            onDismiss = { showAddDialog = false },
            onSave = { title, estAmount, categoryId, note ->
                viewModel.addShoppingItem(title, estAmount, categoryId, note) {
                    Toast.makeText(
                        context,
                        if (lang == "Myanmar") "ဝယ်စရာစာရင်းထဲသို့ ပေါင်းထည့်ပြီးပါပြီ" else "Added to shopping list",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                showAddDialog = false
            }
        )
    }

    // EDIT DIALOG
    selectedItemForEdit?.let { item ->
        ShoppingItemEditDialog(
            title = if (lang == "Myanmar") "ပြင်ဆင်ရန် (Edit Item)" else "Edit Shopping Item",
            item = item,
            categories = categories,
            lang = lang,
            onDismiss = { selectedItemForEdit = null },
            onSave = { title, estAmount, categoryId, note ->
                viewModel.updateShoppingItem(
                    id = item.shoppingItem.id,
                    title = title,
                    estimatedAmount = estAmount,
                    categoryId = categoryId,
                    note = note,
                    isBought = item.shoppingItem.isBought
                ) {
                    Toast.makeText(
                        context,
                        if (lang == "Myanmar") "ပြင်ဆင်မှု အောင်မြင်ပါသည်" else "Updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                selectedItemForEdit = null
            }
        )
    }

    // CONVERT TO ACTUAL EXPENSE DIALOG
    selectedItemForBuy?.let { item ->
        BuyConfirmationDialog(
            item = item,
            currencySymbol = currencySymbol,
            lang = lang,
            onDismiss = { selectedItemForBuy = null },
            onConfirm = { actualAmount ->
                viewModel.convertShoppingItemToExpense(item, actualAmount) {
                    Toast.makeText(
                        context,
                        if (lang == "Myanmar") "စရိတ်စာရင်းထဲသို့ ထည့်သွင်းပြီး ဝယ်ပြီးစာရင်းအဖြစ် မှတ်သားလိုက်ပါပြီ" else "Recorded as expense and marked as purchased",
                        Toast.LENGTH_LONG
                    ).show()
                }
                selectedItemForBuy = null
            }
        )
    }

    // DELETE CONFIRMATION DIALOG
    showDeleteConfirmDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text(if (lang == "Myanmar") "ဖျက်ရန် သေချာပါသလား?" else "Are you sure you want to delete?") },
            text = { Text(if (lang == "Myanmar") "'${item.shoppingItem.title}' ကို ဝယ်စရာစာရင်းထဲမှ ဖျက်ပစ်ပါမည်။" else "This will remove '${item.shoppingItem.title}' from your shopping list.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteShoppingItem(item.shoppingItem) {
                            Toast.makeText(
                                context,
                                if (lang == "Myanmar") "ဖျက်ပြီးပါပြီ" else "Deleted item",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (lang == "Myanmar") "ဖျက်မည် (Delete)" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text(if (lang == "Myanmar") "မဖျက်တော့ပါ (Cancel)" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun ShoppingItemCard(
    item: ShoppingItemWithCategory,
    currencySymbol: String,
    lang: String,
    onToggleBought: () -> Unit,
    onEdit: () -> Unit,
    onBuy: (() -> Unit)?,
    onDelete: () -> Unit
) {
    val isBought = item.shoppingItem.isBought
    val textStyle = if (isBought) {
        MaterialTheme.typography.titleMedium.copy(
            textDecoration = TextDecoration.LineThrough,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    } else {
        MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    val containerColor = if (isBought) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shopping_item_card_${item.shoppingItem.id}"),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isBought) 0.dp else 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox to quickly toggle bought state
            Checkbox(
                checked = isBought,
                onCheckedChange = { onToggleBought() },
                modifier = Modifier.testTag("shopping_item_check_${item.shoppingItem.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Category Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isBought) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(item.category?.icon ?: "category"),
                    contentDescription = item.category?.name ?: "Category",
                    tint = if (isBought) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                           else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.shoppingItem.title,
                    style = textStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (lang == "Myanmar") "ခန့်မှန်းခြေ - " else "Estimated - ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${String.format(Locale.getDefault(), "%.2f", item.shoppingItem.estimatedAmount)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isBought) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                               else MaterialTheme.colorScheme.primary
                    )
                }

                if (item.shoppingItem.note.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.shoppingItem.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // If not bought, show "Buy / Add Expense" button
                if (onBuy != null) {
                    IconButton(
                        onClick = onBuy,
                        modifier = Modifier
                            .testTag("shopping_item_buy_btn_${item.shoppingItem.id}")
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCartCheckout,
                            contentDescription = "Buy and record",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Edit Button
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit note",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Button
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete note",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemEditDialog(
    title: String,
    item: ShoppingItemWithCategory? = null,
    categories: List<CategoryEntity>,
    lang: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, Int, String) -> Unit
) {
    var titleInput by remember { mutableStateOf(item?.shoppingItem?.title ?: "") }
    var amountInput by remember { mutableStateOf(item?.shoppingItem?.estimatedAmount?.toString() ?: "") }
    var selectedCategoryId by remember { mutableStateOf(item?.shoppingItem?.categoryId ?: categories.firstOrNull()?.id ?: 0) }
    var noteInput by remember { mutableStateOf(item?.shoppingItem?.note ?: "") }

    var expandedCategoryMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Item Name
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text(if (lang == "Myanmar") "ပစ္စည်းအမည် (Item Name) *" else "Item Name *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shopping_item_title_input"),
                    singleLine = true
                )

                // Estimated Amount
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text(if (lang == "Myanmar") "ခန့်မှန်းခြေ ဈေးနှုန်း (Estimated Amount) *" else "Estimated Price *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shopping_item_amount_input"),
                    singleLine = true
                )

                // Category Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategoryMenu,
                    onExpandedChange = { expandedCategoryMenu = !expandedCategoryMenu }
                ) {
                    val selectedCategory = categories.find { it.id == selectedCategoryId }
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedCategory?.name ?: (if (lang == "Myanmar") "အမျိုးအစား ရွေးရန်" else "Select Category"),
                        onValueChange = {},
                        label = { Text(if (lang == "Myanmar") "အမျိုးအစား (Category)" else "Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryMenu) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategoryMenu,
                        onDismissRequest = { expandedCategoryMenu = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = getCategoryIcon(category.icon),
                                            contentDescription = category.name,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(category.name)
                                    }
                                },
                                onClick = {
                                    selectedCategoryId = category.id
                                    expandedCategoryMenu = false
                                }
                            )
                        }
                    }
                }

                // Note
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text(if (lang == "Myanmar") "အသေးစိတ်မှတ်စု (Notes/Description)" else "Details / Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shopping_item_note_input"),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                    if (titleInput.isBlank()) {
                        return@Button
                    }
                    onSave(titleInput, amt, selectedCategoryId, noteInput)
                },
                modifier = Modifier.testTag("shopping_item_save_button")
            ) {
                Text(if (lang == "Myanmar") "သိမ်းဆည်းမည် (Save)" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (lang == "Myanmar") "မလုပ်တော့ပါ (Cancel)" else "Cancel")
            }
        }
    )
}

@Composable
fun BuyConfirmationDialog(
    item: ShoppingItemWithCategory,
    currencySymbol: String,
    lang: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var actualAmountInput by remember { mutableStateOf(item.shoppingItem.estimatedAmount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(if (lang == "Myanmar") "ဝယ်ယူမှု မှတ်တမ်းသွင်းရန်" else "Confirm Purchase", style = MaterialTheme.typography.titleLarge)
                Text("Confirm Purchase", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (lang == "Myanmar") {
                        "'${item.shoppingItem.title}' အတွက် အမှန်တကယ်ကျသင့်ငွေကို စရိတ်စာရင်းထဲသို့ ပေါင်းထည့်ပါမည်।"
                    } else {
                        "This will record the actual purchase price for '${item.shoppingItem.title}' into your expense history."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = actualAmountInput,
                    onValueChange = { actualAmountInput = it },
                    label = { Text(if (lang == "Myanmar") "အမှန်တကယ် ကျသင့်ငွေ (Actual Amount) *" else "Actual Purchase Amount *") },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shopping_item_actual_amount_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = actualAmountInput.toDoubleOrNull() ?: item.shoppingItem.estimatedAmount
                    onConfirm(amt)
                },
                modifier = Modifier.testTag("shopping_item_buy_confirm_button")
            ) {
                Text(if (lang == "Myanmar") "ဝယ်ယူပြီး (Confirm)" else "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (lang == "Myanmar") "မလုပ်တော့ပါ (Cancel)" else "Cancel")
            }
        }
    )
}
