package com.example.galdanaja

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseHelper {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
}
