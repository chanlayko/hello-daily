package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_items",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    @ColumnInfo(name = "estimated_amount")
    val estimatedAmount: Double,
    @ColumnInfo(name = "category_id", index = true)
    val categoryId: Int,
    val note: String,
    @ColumnInfo(name = "is_bought")
    val isBought: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: String
)
