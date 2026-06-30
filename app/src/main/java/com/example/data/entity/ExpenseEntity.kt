package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    @ColumnInfo(name = "category_id", index = true)
    val categoryId: Int,
    @ColumnInfo(name = "expense_date")
    val expenseDate: String, // format: YYYY-MM-DD
    val note: String,
    @ColumnInfo(name = "created_at")
    val createdAt: String
)
