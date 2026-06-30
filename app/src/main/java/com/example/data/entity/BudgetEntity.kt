package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "budget_type")
    val budgetType: String, // "daily", "weekly", "monthly"
    val amount: Double,
    @ColumnInfo(name = "start_date")
    val startDate: String, // format: YYYY-MM-DD
    @ColumnInfo(name = "end_date")
    val endDate: String // format: YYYY-MM-DD
)
