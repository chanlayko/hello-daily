package com.example.ui.widgets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getCategoryIcon(name: String): ImageVector {
    return when (name.lowercase()) {
        "restaurant", "food" -> Icons.Default.Restaurant
        "commute", "transportation", "transport" -> Icons.Default.Commute
        "shopping_cart", "shopping" -> Icons.Default.ShoppingCart
        "school", "education" -> Icons.Default.School
        "videogame_asset", "entertainment" -> Icons.Default.VideogameAsset
        "medical_services", "health" -> Icons.Default.MedicalServices
        "receipt", "bills" -> Icons.Default.Receipt
        "home", "rent" -> Icons.Default.Home
        "payments", "salary" -> Icons.Default.Payments
        "star" -> Icons.Default.Star
        "card_giftcard" -> Icons.Default.CardGiftcard
        "fitness_center" -> Icons.Default.FitnessCenter
        "flight" -> Icons.Default.Flight
        "pets" -> Icons.Default.Pets
        else -> Icons.Default.Category
    }
}

fun getAllAvailableIcons(): List<Pair<String, ImageVector>> {
    return listOf(
        "restaurant" to Icons.Default.Restaurant,
        "commute" to Icons.Default.Commute,
        "shopping_cart" to Icons.Default.ShoppingCart,
        "school" to Icons.Default.School,
        "videogame_asset" to Icons.Default.VideogameAsset,
        "medical_services" to Icons.Default.MedicalServices,
        "receipt" to Icons.Default.Receipt,
        "home" to Icons.Default.Home,
        "payments" to Icons.Default.Payments,
        "star" to Icons.Default.Star,
        "card_giftcard" to Icons.Default.CardGiftcard,
        "fitness_center" to Icons.Default.FitnessCenter,
        "flight" to Icons.Default.Flight,
        "pets" to Icons.Default.Pets,
        "category" to Icons.Default.Category
    )
}
