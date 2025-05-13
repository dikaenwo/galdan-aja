package com.example.galdanaja.item

data class CartItem(
    val name: String,
    val price: String,
    val imageResId: Int,
    var quantity: Int
)