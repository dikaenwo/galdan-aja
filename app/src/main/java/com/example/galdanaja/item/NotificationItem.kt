package com.example.galdanaja.item

import com.example.galdanaja.R

data class NotificationItem(
    val title: String,
    val description: String,
    val timeAgo: String,
    val logoResId: Int = R.drawable.push_n_logo
)