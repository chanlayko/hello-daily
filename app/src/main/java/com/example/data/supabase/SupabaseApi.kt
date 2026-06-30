package com.example.data.supabase

import retrofit2.Response
import retrofit2.http.*

interface SupabaseApi {
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body request: SupabaseAuthRequest
    ): Response<SupabaseAuthResponse>

    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body request: SupabaseAuthRequest
    ): Response<SupabaseAuthResponse>

    // Get categories
    @GET("rest/v1/categories")
    suspend fun getCategories(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String
    ): List<SupabaseCategory>

    @POST("rest/v1/categories")
    suspend fun insertCategories(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body categories: List<SupabaseCategory>
    ): Response<Unit>

    // Get expenses
    @GET("rest/v1/expenses")
    suspend fun getExpenses(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String
    ): List<SupabaseExpense>

    @POST("rest/v1/expenses")
    suspend fun insertExpenses(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body expenses: List<SupabaseExpense>
    ): Response<Unit>

    // Get shopping items
    @GET("rest/v1/shopping_items")
    suspend fun getShoppingItems(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String
    ): List<SupabaseShoppingItem>

    @POST("rest/v1/shopping_items")
    suspend fun insertShoppingItems(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body items: List<SupabaseShoppingItem>
    ): Response<Unit>

    @DELETE("rest/v1/shopping_items")
    suspend fun deleteShoppingItem(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Query("id") query: String // e.g., "eq.5"
    ): Response<Unit>

    @PATCH("rest/v1/shopping_items")
    suspend fun updateShoppingItem(
        @Header("apikey") apiKey: String,
        @Header("Authorization") token: String,
        @Query("id") query: String, // e.g., "eq.5"
        @Body updates: SupabaseShoppingItemUpdate
    ): Response<Unit>
}
