package com.example.galdanaja.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date


data class Chat(
    @DocumentId
    var id: String? = null,
    val participants: List<String> = emptyList(),
    val lastMessage: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null,
    val unreadCount: Map<String, Long> = emptyMap()
)
