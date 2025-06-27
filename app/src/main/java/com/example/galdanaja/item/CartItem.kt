package com.example.galdanaja.item

data class CartItem(
    val productId: String = "",
    val name: String = "",
    val price: Int = 0,            // <-- ini INT
    val imageUrl: String = "",
    var quantity: Int = 1
)
