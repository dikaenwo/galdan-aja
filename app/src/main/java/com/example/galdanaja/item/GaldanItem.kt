package com.example.galdanaja.item

data class GaldanItem(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val stock: String = "",  // Field stock baru
    val imageUrl: String = "",  // URL gambar dari Firebase Storage/hosting
    val category: String = "",  // Kategori produk (Makanan/Minuman)
    val description: String = "",
    val date: String = "",
    val userId: String = "",  // ID pemilik produk
    val userName: String = "",  // Nama pemilik produk
    val userPhotoUrl: String = ""  // URL foto profil pemilik
) {
    // Constructor tambahan untuk kompatibilitas dengan kode lama
    constructor(name: String, price: String, photo: Int, userPhotoResId: Int = 0, userName: String = "") : this(
        id = "",
        name = name,
        price = price,
        stock = "",  // Tambahkan default stock kosong
        imageUrl = "",
        category = "",
        description = "",
        date = "",
        userId = "",
        userName = userName,
        userPhotoUrl = ""
    )
}
