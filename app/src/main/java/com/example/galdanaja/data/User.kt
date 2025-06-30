package com.example.galdanaja.data

import com.google.firebase.firestore.DocumentId



data class User(
    @DocumentId
    var id: String? = null,
    val name: String? = null,
    val profileImage: String? = null, // <== DIUBAH agar cocok dengan Firestore
    val email: String? = null,
    val phone: String? = null
)