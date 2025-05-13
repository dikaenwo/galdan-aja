package com.example.galdanaja.item

data class GaldanItem(
    val name: String,
    val price: String,
    val photo: Int,  // Diubah dari imageResId ke photo untuk sesuai dengan GaldanAdapter
    val userPhotoResId: Int = 0,  // Dibuat opsional dengan nilai default
    val userName: String = ""     // Dibuat opsional dengan nilai default
)
