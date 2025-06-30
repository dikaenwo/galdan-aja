package com.example.galdanaja

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.galdanaja.databinding.ActivityMainBinding
import com.example.galdanaja.databinding.ActivityMainSellerBinding

class MainSellerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainSellerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainSellerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main_seller)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_tambah
            )
        )
        navView.itemIconTintList = null
        navView.itemBackground = null
        navView.setupWithNavController(navController)

    }
}