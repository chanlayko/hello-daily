package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entity.ShoppingItemEntity
import com.example.data.entity.ShoppingItemWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {
    @Transaction
    @Query("SELECT * FROM shopping_items ORDER BY is_bought ASC, id DESC")
    fun getAllShoppingItemsFlow(): Flow<List<ShoppingItemWithCategory>>

    @Transaction
    @Query("SELECT * FROM shopping_items ORDER BY is_bought ASC, id DESC")
    suspend fun getAllShoppingItems(): List<ShoppingItemWithCategory>

    @Query("SELECT * FROM shopping_items WHERE id = :id")
    suspend fun getShoppingItemById(id: Int): ShoppingItemWithCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItemEntity): Long

    @Update
    suspend fun updateShoppingItem(item: ShoppingItemEntity)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteShoppingItemById(id: Int)
}
