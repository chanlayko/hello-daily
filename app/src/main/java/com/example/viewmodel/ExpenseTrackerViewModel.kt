package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.BudgetEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.ExpenseWithCategory
import com.example.data.entity.ShoppingItemEntity
import com.example.data.entity.ShoppingItemWithCategory
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpenseTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val prefs = application.getSharedPreferences("expense_tracker_prefs", Context.MODE_PRIVATE)

    // Persistent Settings
    val themeMode = MutableStateFlow(prefs.getString("theme_mode", "System") ?: "System") // System, Light, Dark
    val selectedCurrency = MutableStateFlow(prefs.getString("selected_currency", "USD") ?: "USD") // USD, EUR, JPY, MMK
    val languageMode = MutableStateFlow("English")

    // Active Database flows
    val categories: StateFlow<List<CategoryEntity>>
    val expenses: StateFlow<List<ExpenseWithCategory>>
    val budgets: StateFlow<List<BudgetEntity>>
    val shoppingItems: StateFlow<List<ShoppingItemWithCategory>>

    // Filter and Sort states for History screen
    val searchQuery = MutableStateFlow("")
    val filterCategoryId = MutableStateFlow<Int?>(null)
    val filterStartDate = MutableStateFlow<String?>(null) // format: YYYY-MM-DD
    val filterEndDate = MutableStateFlow<String?>(null) // format: YYYY-MM-DD
    val sortOption = MutableStateFlow("Date (Newest)") // "Date (Newest)", "Date (Oldest)", "Amount (Highest)", "Amount (Lowest)"

    // Collected dynamic list of history products
    val filteredHistoryExpenses: StateFlow<List<ExpenseWithCategory>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(
            database = database,
            categoryDao = database.categoryDao(),
            expenseDao = database.expenseDao(),
            budgetDao = database.budgetDao(),
            shoppingItemDao = database.shoppingItemDao()
        )

        categories = repository.allCategoriesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        expenses = repository.allExpensesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        budgets = repository.allBudgetsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        shoppingItems = repository.allShoppingItemsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed default categories
        viewModelScope.launch {
            repository.seedDefaultCategoriesIfEmpty()
        }

        // Setup dynamic filtering of historical expenses
        filteredHistoryExpenses = combine(
            expenses,
            searchQuery,
            filterCategoryId,
            filterStartDate,
            filterEndDate,
            sortOption
        ) { array ->
            @Suppress("UNCHECKED_CAST")
            val expenseList = array[0] as List<ExpenseWithCategory>
            val query = array[1] as String
            val catId = array[2] as Int?
            val start = array[3] as String?
            val end = array[4] as String?
            val sort = array[5] as String

            var list = expenseList

            // Search filtering (title, note, category name)
            if (query.isNotEmpty()) {
                val lowercaseQuery = query.lowercase()
                list = list.filter {
                    it.expense.title.lowercase().contains(lowercaseQuery) ||
                            it.expense.note.lowercase().contains(lowercaseQuery) ||
                            (it.category?.name?.lowercase()?.contains(lowercaseQuery) ?: false)
                }
            }

            // Category filtering
            if (catId != null) {
                list = list.filter { it.expense.categoryId == catId }
            }

            // Date-range filtering
            if (start != null && end != null) {
                list = list.filter { it.expense.expenseDate in start..end }
            } else if (start != null) {
                list = list.filter { it.expense.expenseDate >= start }
            } else if (end != null) {
                list = list.filter { it.expense.expenseDate <= end }
            }

            // Sorting
            when (sort) {
                "Date (Newest)" -> list.sortedWith(compareByDescending<ExpenseWithCategory> { it.expense.expenseDate }.thenByDescending { it.expense.id })
                "Date (Oldest)" -> list.sortedWith(compareBy<ExpenseWithCategory> { it.expense.expenseDate }.thenBy { it.expense.id })
                "Amount (Highest)" -> list.sortedByDescending { it.expense.amount }
                "Amount (Lowest)" -> list.sortedBy { it.expense.amount }
                else -> list
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // Settings actions
    fun setThemeMode(mode: String) {
        themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun setSelectedCurrency(currency: String) {
        selectedCurrency.value = currency
        prefs.edit().putString("selected_currency", currency).apply()
    }

    fun setLanguageMode(mode: String) {
        languageMode.value = "English"
        prefs.edit().putString("language_mode", "English").apply()
    }

    fun getCurrencySymbol(): String {
        return when (selectedCurrency.value) {
            "USD" -> "$"
            "EUR" -> "€"
            "JPY" -> "¥"
            "MMK" -> "Ks"
            else -> "$"
        }
    }

    // Expense operations
    fun addExpense(title: String, amount: Double, categoryId: Int, date: String, note: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val expense = ExpenseEntity(
                title = title,
                amount = amount,
                categoryId = categoryId,
                expenseDate = date,
                note = note,
                createdAt = now
            )
            repository.insertExpense(expense)
            if (isSupabaseLoggedIn()) {
                syncWithSupabase()
            }
            onComplete()
        }
    }

    fun updateExpense(id: Int, title: String, amount: Double, categoryId: Int, date: String, note: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val current = repository.getExpenseById(id)
            if (current != null) {
                val updated = current.expense.copy(
                    title = title,
                    amount = amount,
                    categoryId = categoryId,
                    expenseDate = date,
                    note = note
                )
                repository.updateExpense(updated)
                if (isSupabaseLoggedIn()) {
                    syncWithSupabase()
                }
            }
            onComplete()
        }
    }

    fun deleteExpense(expense: ExpenseEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            if (isSupabaseLoggedIn()) {
                syncWithSupabase()
            }
            onComplete()
        }
    }

    fun deleteExpenseById(id: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteExpenseById(id)
            if (isSupabaseLoggedIn()) {
                syncWithSupabase()
            }
            onComplete()
        }
    }

    // Shopping List / Note operations
    fun addShoppingItem(title: String, estimatedAmount: Double, categoryId: Int, note: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val item = ShoppingItemEntity(
                title = title,
                estimatedAmount = estimatedAmount,
                categoryId = categoryId,
                note = note,
                isBought = false,
                createdAt = now
            )
            repository.insertShoppingItem(item)
            if (isSupabaseLoggedIn()) {
                syncWithSupabase()
            }
            onComplete()
        }
    }

    fun updateShoppingItem(id: Int, title: String, estimatedAmount: Double, categoryId: Int, note: String, isBought: Boolean, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val current = repository.getShoppingItemById(id)
            if (current != null) {
                val updated = current.shoppingItem.copy(
                    title = title,
                    estimatedAmount = estimatedAmount,
                    categoryId = categoryId,
                    note = note,
                    isBought = isBought
                )
                repository.updateShoppingItem(updated)
                if (isSupabaseLoggedIn()) {
                    syncWithSupabase()
                }
            }
            onComplete()
        }
    }

    fun toggleShoppingItemBought(item: ShoppingItemEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val updated = item.copy(isBought = !item.isBought)
            repository.updateShoppingItem(updated)
            
            // If checking it as bought, automatically add to expenses/dashboard
            if (updated.isBought) {
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val todayDate = getTodayString()
                val expense = ExpenseEntity(
                    title = item.title,
                    amount = item.estimatedAmount,
                    categoryId = item.categoryId,
                    expenseDate = todayDate,
                    note = if (item.note.isNotEmpty()) "From Note: ${item.note}" else "From Shopping Note",
                    createdAt = now
                )
                repository.insertExpense(expense)
            }
            if (isSupabaseLoggedIn()) {
                syncWithSupabase()
            }
            onComplete()
        }
    }

    fun deleteShoppingItem(item: ShoppingItemEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
            if (isSupabaseLoggedIn()) {
                syncWithSupabase()
            }
            onComplete()
        }
    }

    fun convertShoppingItemToExpense(item: ShoppingItemWithCategory, actualAmount: Double, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val todayDate = getTodayString()
            val expense = ExpenseEntity(
                title = item.shoppingItem.title,
                amount = actualAmount,
                categoryId = item.shoppingItem.categoryId,
                expenseDate = todayDate,
                note = if (item.shoppingItem.note.isNotEmpty()) "From Note: ${item.shoppingItem.note}" else "From Shopping Note",
                createdAt = now
            )
            repository.insertExpense(expense)
            
            // Mark as bought
            val updatedShoppingItem = item.shoppingItem.copy(isBought = true)
            repository.updateShoppingItem(updatedShoppingItem)
            
            if (isSupabaseLoggedIn()) {
                syncWithSupabase()
            }
            onComplete()
        }
    }

    // Category operations
    fun addCategory(name: String, icon: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            repository.insertCategory(CategoryEntity(name = name, icon = icon, createdAt = now))
            if (isSupabaseLoggedIn()) {
                syncWithSupabase()
            }
            onComplete()
        }
    }

    fun updateCategory(id: Int, name: String, icon: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            repository.updateCategory(CategoryEntity(id = id, name = name, icon = icon, createdAt = now))
            onComplete()
        }
    }

    fun deleteCategory(category: CategoryEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            onComplete()
        }
    }

    // Budget operations
    fun setBudget(type: String, amount: Double, startDate: String, endDate: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val existing = repository.getBudgetByType(type)
            if (existing != null) {
                val updated = existing.copy(amount = amount, startDate = startDate, endDate = endDate)
                repository.updateBudget(updated)
            } else {
                repository.insertBudget(BudgetEntity(budgetType = type, amount = amount, startDate = startDate, endDate = endDate))
            }
            onComplete()
        }
    }

    // ----------------------------------------------------
    // COMPLETED STATISTICS CALCULATIONS (Dashboard & reports)
    // ----------------------------------------------------

    fun getTodayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getYesterdayString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    fun getStartOfWeekString(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    fun getLastWeekRange(): Pair<String, String> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.WEEK_OF_YEAR, -1)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val start = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        cal.add(Calendar.DAY_OF_WEEK, 6)
        val end = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        return Pair(start, end)
    }

    fun getStartOfMonthString(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    fun getLastMonthRange(): Pair<String, String> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val start = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, maxDay)
        val end = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        return Pair(start, end)
    }

    // Aggregates statistics cleanly
    fun computeStats(expenseList: List<ExpenseWithCategory>): DashboardStats {
        val today = getTodayString()
        val yesterday = getYesterdayString()
        val startOfWeek = getStartOfWeekString()
        val (lastWeekStart, lastWeekEnd) = getLastWeekRange()
        val startOfMonth = getStartOfMonthString()
        val (lastMonthStart, lastMonthEnd) = getLastMonthRange()

        // 1. Today's total and tx count
        val todayExpenses = expenseList.filter { it.expense.expenseDate == today }
        val todayTotal = todayExpenses.sumOf { it.expense.amount }
        val todayCount = todayExpenses.size

        val yesterdayExpenses = expenseList.filter { it.expense.expenseDate == yesterday }
        val yesterdayTotal = yesterdayExpenses.sumOf { it.expense.amount }
        val todayPctChange = if (yesterdayTotal > 0) ((todayTotal - yesterdayTotal) / yesterdayTotal) * 100.0 else 0.0

        // 2. This week's total and tx count
        val weekExpenses = expenseList.filter { it.expense.expenseDate >= startOfWeek && it.expense.expenseDate <= today }
        val weekTotal = weekExpenses.sumOf { it.expense.amount }
        val weekCount = weekExpenses.size

        val lastWeekExpenses = expenseList.filter { it.expense.expenseDate in lastWeekStart..lastWeekEnd }
        val lastWeekTotal = lastWeekExpenses.sumOf { it.expense.amount }
        val weekPctChange = if (lastWeekTotal > 0) ((weekTotal - lastWeekTotal) / lastWeekTotal) * 100.0 else 0.0

        // 3. This month's total and tx count
        val monthExpenses = expenseList.filter { it.expense.expenseDate >= startOfMonth && it.expense.expenseDate <= today }
        val monthTotal = monthExpenses.sumOf { it.expense.amount }
        val monthCount = monthExpenses.size

        val lastMonthExpenses = expenseList.filter { it.expense.expenseDate in lastMonthStart..lastMonthEnd }
        val lastMonthTotal = lastMonthExpenses.sumOf { it.expense.amount }
        val monthPctChange = if (lastMonthTotal > 0) ((monthTotal - lastMonthTotal) / lastMonthTotal) * 100.0 else 0.0

        // 4. All time stats
        val allTimeTotal = expenseList.sumOf { it.expense.amount }
        val allTimeCount = expenseList.size

        // 5. Category-wise expense summary
        val categoryWiseSummary = expenseList.groupBy { it.category?.name ?: "Other" }
            .mapValues { entry -> entry.value.sumOf { it.expense.amount } }

        return DashboardStats(
            todayTotal = todayTotal,
            todayCount = todayCount,
            todayPctChange = todayPctChange,
            weekTotal = weekTotal,
            weekCount = weekCount,
            weekPctChange = weekPctChange,
            monthTotal = monthTotal,
            monthCount = monthCount,
            monthPctChange = monthPctChange,
            allTimeTotal = allTimeTotal,
            allTimeCount = allTimeCount,
            categoryWiseSummary = categoryWiseSummary
        )
    }

    // Database Actions: Backup & Restore
    fun backupDatabaseFile(context: Context, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val df = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val backupFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ExpenseTracker_Backup_$df.db")
                val fos = FileOutputStream(backupFile)
                val success = repository.backupDatabase(context, fos)
                if (success) {
                    Toast.makeText(context, "Backup saved to ${backupFile.name}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Backup failed!", Toast.LENGTH_SHORT).show()
                }
                onComplete(success)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
    }

    fun restoreDatabaseFromFile(context: Context, file: File, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                if (!file.exists()) {
                    Toast.makeText(context, "Selected backup file does not exist!", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                    return@launch
                }
                val fis = FileInputStream(file)
                val success = repository.restoreDatabase(context, fis)
                if (success) {
                    Toast.makeText(context, "Backup successfully restored! App will restart.", Toast.LENGTH_LONG).show()
                    // Restart logic or reload. Since we are doing offline, let them press OK.
                } else {
                    Toast.makeText(context, "Restore failed!", Toast.LENGTH_SHORT).show()
                }
                onComplete(success)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
    }

    fun importDatabaseFromUri(context: Context, uri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val success = repository.restoreDatabase(context, inputStream)
                    if (success) {
                        Toast.makeText(context, "Database restored successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Database restore failed!", Toast.LENGTH_SHORT).show()
                    }
                    onComplete(success)
                } else {
                    Toast.makeText(context, "Could not open selected backup file", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error active: ${e.message}", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
    }

    // ----------------------------------------------------
    // CSV EXPORT GENERATOR
    // ----------------------------------------------------
    fun exportToCSV(context: Context, filteredExpenses: List<ExpenseWithCategory>): Uri? {
        try {
            val df = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val exportFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Expenses_Report_$df.csv")
            val p = java.io.PrintWriter(exportFile)
            p.println("ID,Title,Amount,Category,Date,Note,Created At")
            var total = 0.0
            filteredExpenses.forEach {
                p.println("${it.expense.id},\"${it.expense.title}\",${it.expense.amount},\"${it.category?.name ?: "Other"}\",${it.expense.expenseDate},\"${it.expense.note}\",${it.expense.createdAt}")
                total += it.expense.amount
            }
            p.println()
            p.println(",Total Amount,${total}")
            p.flush()
            p.close()
            Toast.makeText(context, "CSV Exported to ${exportFile.name}", Toast.LENGTH_LONG).show()
            return Uri.fromFile(exportFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "CSV Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    // ----------------------------------------------------
    // PDF EXPORT GENERATOR (Native PdfDocument, 0 dependencies)
    // ----------------------------------------------------
    fun exportToPDF(context: Context, filteredExpenses: List<ExpenseWithCategory>): Uri? {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()

        // Page Header
        paint.color = Color.BLACK
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Daily Expense Tracker Report", 40f, 60f, paint)

        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        canvas.drawText("Generated on: $df", 40f, 80f, paint)
        canvas.drawText("Currency: ${selectedCurrency.value} (${getCurrencySymbol()})", 40f, 95f, paint)

        // Draw Line Table Header
        paint.color = Color.GRAY
        canvas.drawLine(40f, 115f, 555f, 115f, paint)

        // Table headers
        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        paint.textSize = 11f
        canvas.drawText("Date", 45f, 135f, paint)
        canvas.drawText("Category", 140f, 135f, paint)
        canvas.drawText("Title", 250f, 135f, paint)
        canvas.drawText("Amount", 480f, 135f, paint)

        paint.color = Color.GRAY
        canvas.drawLine(40f, 145f, 555f, 145f, paint)

        // Write rows
        paint.isFakeBoldText = false
        paint.textSize = 10f
        paint.color = Color.BLACK
        var y = 170f
        var total = 0.0

        for (item in filteredExpenses.take(20)) { // limit to 20 for single-page layout robustness
            canvas.drawText(item.expense.expenseDate, 45f, y, paint)
            canvas.drawText(item.category?.name ?: "Other", 140f, y, paint)
            
            // Truncate title if extremely long
            val textTitle = if (item.expense.title.length > 25) item.expense.title.take(22) + "..." else item.expense.title
            canvas.drawText(textTitle, 250f, y, paint)

            val roundedAmount = String.format(Locale.getDefault(), "%.2f", item.expense.amount)
            canvas.drawText("${getCurrencySymbol()}$roundedAmount", 480f, y, paint)
            total += item.expense.amount
            y += 25f
        }

        if (filteredExpenses.size > 20) {
            paint.color = Color.RED
            canvas.drawText("... truncated ${filteredExpenses.size - 20} more items ...", 45f, y + 10f, paint)
            y += 30f
        }

        // Draw Table Footer Line
        paint.color = Color.GRAY
        canvas.drawLine(40f, y - 10f, 555f, y - 10f, paint)

        // Bottom summary
        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        paint.textSize = 13f
        canvas.drawText("Total Sum:", 350f, y + 20f, paint)
        canvas.drawText("${getCurrencySymbol()}${String.format(Locale.getDefault(), "%.2f", total)}", 480f, y + 20f, paint)

        pdf.finishPage(page)

        try {
            val fileDf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val exportFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Expenses_Report_$fileDf.pdf")
            val outputStream = FileOutputStream(exportFile)
            pdf.writeTo(outputStream)
            pdf.close()
            outputStream.flush()
            outputStream.close()
            Toast.makeText(context, "PDF exported to ${exportFile.name}", Toast.LENGTH_LONG).show()
            return Uri.fromFile(exportFile)
        } catch (e: Exception) {
            e.printStackTrace()
            pdf.close()
            Toast.makeText(context, "PDF Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    // --- Supabase Authentication & Synchronization ---
    val supabaseAccessToken = MutableStateFlow(prefs.getString("supabase_access_token", "") ?: "")
    val supabaseUserEmail = MutableStateFlow(prefs.getString("supabase_user_email", "") ?: "")
    val supabaseIsSyncing = MutableStateFlow(false)

    fun isSupabaseLoggedIn(): Boolean {
        return supabaseAccessToken.value.isNotEmpty()
    }

    fun supabaseSignUp(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val apiKey = com.example.BuildConfig.SUPABASE_KEY
                val response = com.example.data.supabase.SupabaseService.api.signUp(
                    apiKey = apiKey,
                    authorization = "Bearer $apiKey",
                    request = com.example.data.supabase.SupabaseAuthRequest(email, password)
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.accessToken != null) {
                        saveSupabaseSession(body.accessToken, body.user?.email ?: email)
                        onResult(true, "Successfully signed up and logged in")
                    } else {
                        onResult(true, "Signed up successfully. Email verification may be required.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    val extractedError = extractSupabaseError(errorBody)
                    onResult(false, "Registration failed: $extractedError")
                }
            } catch (e: Exception) {
                onResult(false, "Connection failed: ${e.message}")
            }
        }
    }

    fun supabaseSignIn(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val apiKey = com.example.BuildConfig.SUPABASE_KEY
                val response = com.example.data.supabase.SupabaseService.api.signIn(
                    apiKey = apiKey,
                    authorization = "Bearer $apiKey",
                    request = com.example.data.supabase.SupabaseAuthRequest(email, password)
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val token = body.accessToken
                    if (!token.isNullOrEmpty()) {
                        saveSupabaseSession(token, body.user?.email ?: email)
                        onResult(true, "Login successful")
                        syncWithSupabase {}
                    } else {
                        onResult(false, "Token not found")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    val extractedError = extractSupabaseError(errorBody)
                    onResult(false, "Login failed: $extractedError")
                }
            } catch (e: Exception) {
                onResult(false, "Connection failed: ${e.message}")
            }
        }
    }

    private fun extractSupabaseError(jsonString: String): String {
        if (jsonString.trim().startsWith("<!DOCTYPE", ignoreCase = true) || 
            jsonString.trim().startsWith("<html", ignoreCase = true)) {
            return "Server returned HTML instead of JSON. This usually indicates that the Supabase URL is incorrect, your Supabase project is paused/disabled, or there is a network proxy blocking the API. Please double check your Project URL and API Key configuration."
        }
        try {
            val msgRegex = "\"msg\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val messageRegex = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val descRegex = "\"error_description\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val errorRegex = "\"error\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            
            val msgMatch = msgRegex.find(jsonString)
            if (msgMatch != null) return msgMatch.groupValues[1]
            
            val messageMatch = messageRegex.find(jsonString)
            if (messageMatch != null) return messageMatch.groupValues[1]
            
            val descMatch = descRegex.find(jsonString)
            if (descMatch != null) return descMatch.groupValues[1]
            
            val errorMatch = errorRegex.find(jsonString)
            if (errorMatch != null) return errorMatch.groupValues[1]
        } catch (e: Exception) {
            // ignore
        }
        return jsonString.ifBlank { "Unknown Error" }
    }

    fun supabaseSignOut() {
        saveSupabaseSession("", "")
    }

    private fun saveSupabaseSession(token: String, email: String) {
        supabaseAccessToken.value = token
        supabaseUserEmail.value = email
        prefs.edit()
            .putString("supabase_access_token", token)
            .putString("supabase_user_email", email)
            .apply()
    }

    fun syncWithSupabase(onComplete: (Boolean) -> Unit = {}) {
        val token = supabaseAccessToken.value
        if (token.isEmpty()) {
            onComplete(false)
            return
        }
        viewModelScope.launch {
            supabaseIsSyncing.value = true
            try {
                val apiKey = com.example.BuildConfig.SUPABASE_KEY
                val authHeader = "Bearer $token"

                // 1. Sync Categories
                val localCategories = repository.getAllCategories()
                val remoteCategories = try {
                    com.example.data.supabase.SupabaseService.api.getCategories(apiKey, authHeader)
                } catch (e: Exception) {
                    emptyList()
                }
                
                // Upload local categories not in remote
                val categoriesToUpload = localCategories.filter { local ->
                    remoteCategories.none { remote -> remote.name.equals(local.name, ignoreCase = true) }
                }.map {
                    com.example.data.supabase.SupabaseCategory(
                        name = it.name,
                        icon = it.icon,
                        createdAt = it.createdAt
                    )
                }
                if (categoriesToUpload.isNotEmpty()) {
                    com.example.data.supabase.SupabaseService.api.insertCategories(apiKey, authHeader, categories = categoriesToUpload)
                }

                // Re-fetch category and update local Room DB with any newly fetched remote categories
                val finalRemoteCategories = try {
                    com.example.data.supabase.SupabaseService.api.getCategories(apiKey, authHeader)
                } catch (e: Exception) {
                    remoteCategories
                }
                for (remote in finalRemoteCategories) {
                    if (localCategories.none { it.name.equals(remote.name, ignoreCase = true) }) {
                        repository.insertCategory(
                            com.example.data.entity.CategoryEntity(
                                name = remote.name,
                                icon = remote.icon,
                                createdAt = remote.createdAt
                            )
                        )
                    }
                }

                // Refresh our local category list for foreign key resolution
                val updatedLocalCategories = repository.getAllCategories()

                // Helper to map remote category name to local ID
                fun findLocalCategoryId(remoteCatId: Int, remoteCats: List<com.example.data.supabase.SupabaseCategory>): Int {
                    val remoteCat = remoteCats.find { it.id == remoteCatId } ?: return updatedLocalCategories.firstOrNull()?.id ?: 1
                    return updatedLocalCategories.find { it.name.equals(remoteCat.name, ignoreCase = true) }?.id ?: updatedLocalCategories.firstOrNull()?.id ?: 1
                }

                // Helper to map local category ID to remote ID
                fun findRemoteCategoryId(localCatId: Int, remoteCats: List<com.example.data.supabase.SupabaseCategory>): Int {
                    val localCat = updatedLocalCategories.find { it.id == localCatId } ?: return remoteCats.firstOrNull()?.id ?: 1
                    return remoteCats.find { it.name.equals(localCat.name, ignoreCase = true) }?.id ?: remoteCats.firstOrNull()?.id ?: 1
                }

                // 2. Sync Expenses
                val localExpenses = repository.getAllExpenses()
                val remoteExpenses = try {
                    com.example.data.supabase.SupabaseService.api.getExpenses(apiKey, authHeader)
                } catch (e: Exception) {
                    emptyList()
                }

                // Download missing remote expenses
                for (remote in remoteExpenses) {
                    val matches = localExpenses.any { local ->
                        local.expense.title == remote.title &&
                        local.expense.amount == remote.amount &&
                        local.expense.expenseDate == remote.expenseDate &&
                        local.expense.createdAt == remote.createdAt
                    }
                    if (!matches) {
                        val localCatId = findLocalCategoryId(remote.categoryId, finalRemoteCategories)
                        repository.insertExpense(
                            com.example.data.entity.ExpenseEntity(
                                title = remote.title,
                                amount = remote.amount,
                                categoryId = localCatId,
                                expenseDate = remote.expenseDate,
                                note = remote.note,
                                createdAt = remote.createdAt
                            )
                        )
                    }
                }

                // Upload missing local expenses
                val expensesToUpload = localExpenses.filter { local ->
                    remoteExpenses.none { remote ->
                        local.expense.title == remote.title &&
                        local.expense.amount == remote.amount &&
                        local.expense.expenseDate == remote.expenseDate &&
                        local.expense.createdAt == remote.createdAt
                    }
                }.map { local ->
                    val remoteCatId = findRemoteCategoryId(local.expense.categoryId, finalRemoteCategories)
                    com.example.data.supabase.SupabaseExpense(
                        title = local.expense.title,
                        amount = local.expense.amount,
                        categoryId = remoteCatId,
                        expenseDate = local.expense.expenseDate,
                        note = local.expense.note,
                        createdAt = local.expense.createdAt,
                        userEmail = supabaseUserEmail.value
                    )
                }
                if (expensesToUpload.isNotEmpty()) {
                    com.example.data.supabase.SupabaseService.api.insertExpenses(apiKey, authHeader, expenses = expensesToUpload)
                }

                // 3. Sync Shopping Items
                val localShoppingItems = repository.getAllShoppingItems()
                val remoteShoppingItems = try {
                    com.example.data.supabase.SupabaseService.api.getShoppingItems(apiKey, authHeader)
                } catch (e: Exception) {
                    emptyList()
                }

                // Sync remote to local
                for (remote in remoteShoppingItems) {
                    val localMatch = localShoppingItems.find { local ->
                        local.shoppingItem.title == remote.title &&
                        local.shoppingItem.createdAt == remote.createdAt
                    }
                    if (localMatch == null) {
                        val localCatId = findLocalCategoryId(remote.categoryId, finalRemoteCategories)
                        repository.insertShoppingItem(
                            com.example.data.entity.ShoppingItemEntity(
                                title = remote.title,
                                estimatedAmount = remote.estimatedAmount,
                                categoryId = localCatId,
                                note = remote.note,
                                isBought = remote.isBought,
                                createdAt = remote.createdAt
                            )
                        )
                    } else {
                        if (localMatch.shoppingItem.isBought != remote.isBought || localMatch.shoppingItem.note != remote.note) {
                            repository.updateShoppingItem(
                                localMatch.shoppingItem.copy(
                                    isBought = remote.isBought,
                                    note = remote.note
                                )
                            )
                        }
                    }
                }

                // Upload missing local to remote
                val shoppingToUpload = localShoppingItems.filter { local ->
                    remoteShoppingItems.none { remote ->
                        local.shoppingItem.title == remote.title &&
                        local.shoppingItem.createdAt == remote.createdAt
                    }
                }.map { local ->
                    val remoteCatId = findRemoteCategoryId(local.shoppingItem.categoryId, finalRemoteCategories)
                    com.example.data.supabase.SupabaseShoppingItem(
                        title = local.shoppingItem.title,
                        estimatedAmount = local.shoppingItem.estimatedAmount,
                        categoryId = remoteCatId,
                        note = local.shoppingItem.note,
                        isBought = local.shoppingItem.isBought,
                        createdAt = local.shoppingItem.createdAt,
                        userEmail = supabaseUserEmail.value
                    )
                }
                if (shoppingToUpload.isNotEmpty()) {
                    com.example.data.supabase.SupabaseService.api.insertShoppingItems(apiKey, authHeader, items = shoppingToUpload)
                }

                supabaseIsSyncing.value = false
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                supabaseIsSyncing.value = false
                onComplete(false)
            }
        }
    }
}

// Data class to wrap stats output
data class DashboardStats(
    val todayTotal: Double,
    val todayCount: Int,
    val todayPctChange: Double,
    val weekTotal: Double,
    val weekCount: Int,
    val weekPctChange: Double,
    val monthTotal: Double,
    val monthCount: Int,
    val monthPctChange: Double,
    val allTimeTotal: Double,
    val allTimeCount: Int,
    val categoryWiseSummary: Map<String, Double>
)
