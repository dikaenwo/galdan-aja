package com.example.galdanaja

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.galdanaja.databinding.ActivityMainBinding
import com.example.galdanaja.helper.FirebaseHelper
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_explore, R.id.navigation_favorite, R.id.navigation_cart, R.id.navigation_notification
            )
        )
        navView.itemIconTintList = null
        navView.itemBackground = null
        navView.setupWithNavController(navController)

    }

    private fun saveFcmToken() {
        val userId = FirebaseHelper.auth.currentUser?.uid ?: return
        val userDocRef = FirebaseHelper.firestore.collection("users").document(userId)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Dapatkan token baru
            val token = task.result

            // Simpan token ke dokumen user di Firestore
            userDocRef.update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "FCM Token updated successfully for user: $userId")
                }
                .addOnFailureListener { e ->
                    Log.w("FCM", "Error updating FCM token", e)
                }
        }
    }
}
