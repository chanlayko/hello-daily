package com.example.data.dao

import androidx.room.*
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.ExpenseWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Transaction
    @Query("SELECT * FROM expenses ORDER BY expense_date DESC, id DESC")
    fun getAllExpensesFlow(): Flow<List<ExpenseWithCategory>>

    @Transaction
    @Query("SELECT * FROM expenses ORDER BY expense_date DESC, id DESC")
    suspend fun getAllExpenses(): List<ExpenseWithCategory>

    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Int): ExpenseWithCategory?

    @Transaction
    @Query("SELECT * FROM expenses WHERE expense_date BETWEEN :startDate AND :endDate ORDER BY expense_date DESC, id DESC")
    fun getExpensesInRangeFlow(startDate: String, endDate: String): Flow<List<ExpenseWithCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Int)
}
