package com.example.galdanaja.item

data class CartItem(
    val productId: String,
    val name: String,
    val price: Int,
    val imageUrl: String,
    var quantity: Int,
    val sellerId: String // <-- TAMBAHKAN FIELD INI
)
