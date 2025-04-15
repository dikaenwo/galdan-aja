package com.example.galdanaja

import com.google.firebase.auth.FirebaseAuth

object FirebaseHelper {
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
}