package com.example.data.supabase

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseAuthRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class SupabaseUser(
    val id: String,
    val email: String?
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
    @Json(name = "access_token") val accessToken: String?,
    @Json(name = "refresh_token") val refreshToken: String?,
    @Json(name = "user") val user: SupabaseUser?
)

@JsonClass(generateAdapter = true)
data class SupabaseCategory(
    val id: Int? = null,
    val name: String,
    val icon: String,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class SupabaseExpense(
    val id: Int? = null,
    val title: String,
    val amount: Double,
    @Json(name = "category_id") val categoryId: Int,
    @Json(name = "expense_date") val expenseDate: String,
    val note: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "user_email") val userEmail: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseShoppingItem(
    val id: Int? = null,
    val title: String,
    @Json(name = "estimated_amount") val estimatedAmount: Double,
    @Json(name = "category_id") val categoryId: Int,
    val note: String,
    @Json(name = "is_bought") val isBought: Boolean,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "user_email") val userEmail: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseShoppingItemUpdate(
    val title: String,
    @Json(name = "estimated_amount") val estimatedAmount: Double,
    @Json(name = "category_id") val categoryId: Int,
    val note: String,
    @Json(name = "is_bought") val isBought: Boolean
)
