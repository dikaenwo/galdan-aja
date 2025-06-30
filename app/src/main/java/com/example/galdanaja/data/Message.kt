package com.example.galdanaja.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Message(
    val text: String? = null,
    val senderId: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null
)