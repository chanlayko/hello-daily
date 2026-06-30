package com.example.data.repository

import android.content.Context
import com.example.data.dao.BudgetDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.ShoppingItemDao
import com.example.data.database.AppDatabase
import com.example.data.entity.BudgetEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.ExpenseWithCategory
import com.example.data.entity.ShoppingItemEntity
import com.example.data.entity.ShoppingItemWithCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseRepository(
    private val database: AppDatabase,
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val shoppingItemDao: ShoppingItemDao
) {
    val allCategoriesFlow: Flow<List<CategoryEntity>> = categoryDao.getAllCategoriesFlow()
    val allExpensesFlow: Flow<List<ExpenseWithCategory>> = expenseDao.getAllExpensesFlow()
    val allBudgetsFlow: Flow<List<BudgetEntity>> = budgetDao.getAllBudgetsFlow()
    val allShoppingItemsFlow: Flow<List<ShoppingItemWithCategory>> = shoppingItemDao.getAllShoppingItemsFlow()

    suspend fun getAllShoppingItems(): List<ShoppingItemWithCategory> = withContext(Dispatchers.IO) {
        shoppingItemDao.getAllShoppingItems()
    }

    suspend fun getShoppingItemById(id: Int): ShoppingItemWithCategory? = withContext(Dispatchers.IO) {
        shoppingItemDao.getShoppingItemById(id)
    }

    suspend fun insertShoppingItem(item: ShoppingItemEntity): Long = withContext(Dispatchers.IO) {
        shoppingItemDao.insertShoppingItem(item)
    }

    suspend fun updateShoppingItem(item: ShoppingItemEntity) = withContext(Dispatchers.IO) {
        shoppingItemDao.updateShoppingItem(item)
    }

    suspend fun deleteShoppingItem(item: ShoppingItemEntity) = withContext(Dispatchers.IO) {
        shoppingItemDao.deleteShoppingItem(item)
    }

    suspend fun deleteShoppingItemById(id: Int) = withContext(Dispatchers.IO) {
        shoppingItemDao.deleteShoppingItemById(id)
    }

    suspend fun getAllCategories(): List<CategoryEntity> {
        return withContext(Dispatchers.IO) {
            categoryDao.getAllCategories()
        }
    }

    suspend fun getCategoryById(id: Int): CategoryEntity? = withContext(Dispatchers.IO) {
        categoryDao.getCategoryById(id)
    }

    suspend fun insertCategory(category: CategoryEntity): Long = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategory(category)
    }

    suspend fun getAllExpenses(): List<ExpenseWithCategory> = withContext(Dispatchers.IO) {
        expenseDao.getAllExpenses()
    }

    suspend fun getExpenseById(id: Int): ExpenseWithCategory? = withContext(Dispatchers.IO) {
        expenseDao.getExpenseById(id)
    }

    fun getExpensesInRangeFlow(startDate: String, endDate: String): Flow<List<ExpenseWithCategory>> {
        return expenseDao.getExpensesInRangeFlow(startDate, endDate)
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Int) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpenseById(id)
    }

    suspend fun getBudgetByType(type: String): BudgetEntity? = withContext(Dispatchers.IO) {
        budgetDao.getBudgetByType(type)
    }

    fun getBudgetByTypeFlow(type: String): Flow<BudgetEntity?> {
        return budgetDao.getBudgetByTypeFlow(type)
    }

    suspend fun insertBudget(budget: BudgetEntity): Long = withContext(Dispatchers.IO) {
        budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun seedDefaultCategoriesIfEmpty() = withContext(Dispatchers.IO) {
        val list = categoryDao.getAllCategories()
        if (list.isEmpty()) {
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val defaults = listOf(
                CategoryEntity(name = "Food", icon = "restaurant", createdAt = now),
                CategoryEntity(name = "Transportation", icon = "commute", createdAt = now),
                CategoryEntity(name = "Shopping", icon = "shopping_cart", createdAt = now),
                CategoryEntity(name = "Education", icon = "school", createdAt = now),
                CategoryEntity(name = "Entertainment", icon = "videogame_asset", createdAt = now),
                CategoryEntity(name = "Health", icon = "medical_services", createdAt = now),
                CategoryEntity(name = "Bills", icon = "receipt", createdAt = now),
                CategoryEntity(name = "Rent", icon = "home", createdAt = now),
                CategoryEntity(name = "Salary", icon = "payments", createdAt = now),
                CategoryEntity(name = "Other", icon = "category", createdAt = now)
            )
            categoryDao.insertCategories(defaults)
        }
    }

    // Export database content to a given OutputStream (Backup)
    suspend fun backupDatabase(context: Context, outputStream: OutputStream): Boolean = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath("expense_tracker_database")
            if (dbFile.exists()) {
                // Ensure everything in WAL is written to main DB file before starting backup
                database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
                
                val inputStream = FileInputStream(dbFile)
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Import database content from a given InputStream (Restore)
    suspend fun restoreDatabase(context: Context, inputStream: InputStream): Boolean = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath("expense_tracker_database")
            
            // Close database first to release file locks
            if (database.isOpen) {
                database.close()
            }

            // Copy input stream over the database file
            val outputStream = FileOutputStream(dbFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()

            // Delete WAL and SHM files to ensure next open re-initializes clean state
            val shmFile = File(dbFile.path + "-shm")
            val walFile = File(dbFile.path + "-wal")
            if (shmFile.exists()) shmFile.delete()
            if (walFile.exists()) walFile.delete()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
