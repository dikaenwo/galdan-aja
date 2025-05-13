package com.example.galdanaja.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.galdanaja.MainActivity
import com.example.galdanaja.databinding.ActivitySplashScreenBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(3000) // tunggu 3 detik

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // User sudah login ➔ MainActivity
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            } else {
                // Belum login ➔ OnBoardingActivity
                startActivity(Intent(this@SplashScreenActivity, OnBoardingActivity::class.java))
            }
            finish() // tutup Splash supaya gak bisa di-back
        }
    }
}
