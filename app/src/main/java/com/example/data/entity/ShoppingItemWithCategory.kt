package com.example.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ShoppingItemWithCategory(
    @Embedded val shoppingItem: ShoppingItemEntity,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)
