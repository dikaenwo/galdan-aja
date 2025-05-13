package com.example.galdanaja.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.galdanaja.R
import com.example.galdanaja.databinding.ActivityProfileBinding
import com.example.galdanaja.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Profil telah diperbarui, muat ulang data
            loadUserProfile()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        
        // Load user data
        loadUserProfile()
        
        // Set click listeners
        binding.icBack.setOnClickListener {
            finish()
        }
        
        binding.tvBack.setOnClickListener {
            finish()
        }
        
        binding.tvEdit.setOnClickListener {
            // Periksa apakah pengguna sudah memiliki data di Firestore
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(currentUser.uid).get()
                    .addOnSuccessListener { document ->
                        val intent = Intent(this, EditProfileActivity::class.java)
                        // Tambahkan flag untuk menandai apakah pengguna baru atau tidak
                        intent.putExtra("IS_NEW_USER", !document.exists())
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        // Jika gagal memeriksa, anggap sebagai pengguna baru
                        val intent = Intent(this, EditProfileActivity::class.java)
                        intent.putExtra("IS_NEW_USER", true)
                        startActivity(intent)
                    }
            }
        }
        
        binding.tvLogout.setOnClickListener {
            // Hapus semua data sesi
            auth.signOut()
            
            // Hapus data SharedPreferences jika ada
            getSharedPreferences("galdan_prefs", MODE_PRIVATE).edit().clear().apply()
            
            // Pastikan semua aktivitas dibersihkan
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Muat ulang data profil setiap kali activity menjadi visible
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        
        if (currentUser != null) {
            // Tampilkan email dari Auth terlebih dahulu
            binding.tvEmail.text = currentUser.email ?: ""
            
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        val email = document.getString("email") ?: currentUser.email
                        val phone = document.getString("phone")
                        val profileImageUrl = document.getString("profileImage")
                        
                        binding.textView17.text = name ?: "User"
                        binding.tvEmail.text = email ?: ""
                        binding.tvPhone.text = phone ?: ""
                        
                        if (profileImageUrl != null) {
                            Glide.with(this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.avatars)
                                .error(R.drawable.avatars)
                                .into(binding.imgProfile)
                        }
                    } else {
                        // Fallback ke data dari Auth
                        binding.textView17.text = currentUser.displayName ?: "User"
                        binding.tvEmail.text = currentUser.email ?: ""
                        
                        if (currentUser.photoUrl != null) {
                            Glide.with(this)
                                .load(currentUser.photoUrl)
                                .placeholder(R.drawable.avatars)
                                .error(R.drawable.avatars)
                                .into(binding.imgProfile)
                        }
                    }
                }
                .addOnFailureListener {
                    // Fallback ke data dari Auth
                    binding.textView17.text = currentUser.displayName ?: "User"
                    binding.tvEmail.text = currentUser.email ?: ""
                    
                    if (currentUser.photoUrl != null) {
                        Glide.with(this)
                            .load(currentUser.photoUrl)
                            .placeholder(R.drawable.avatars)
                            .error(R.drawable.avatars)
                            .into(binding.imgProfile)
                    }
                }
        }
    }
}