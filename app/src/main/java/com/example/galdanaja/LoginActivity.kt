package com.example.galdanaja

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.galdanaja.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseHelper.auth.currentUser != null) {
            // Kalau masih login, langsung ke MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.emailLogin.addTextChangedListener(inputWatcher)
        binding.passwordLogin.addTextChangedListener(inputWatcher)
        updateButtonState()
        
        // Tambahkan listener untuk toggle password visibility
        setupPasswordToggle()

        binding.btnLogin.setOnClickListener {
            val email = binding.emailLogin.text.toString()
            val password = binding.passwordLogin.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                FirebaseHelper.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Setelah login berhasil, CEK Firestore
                            val uid = FirebaseHelper.auth.currentUser?.uid
                            if (uid != null) {
                                val db = FirebaseHelper.firestore
                                db.collection("users").document(uid)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            // Data profil ADA ➔ langsung ke MainActivity
                                            startActivity(Intent(this, MainActivity::class.java))
                                        } else {
                                            // Data profil BELUM ADA ➔ ke CreateProfileActivity
                                            startActivity(Intent(this, CreateProfileActivity::class.java))
                                        }
                                        finish() // supaya login activity tidak bisa balik lagi
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Gagal cek data: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Email atau password tidak terdaftar", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Tambahkan fungsi baru untuk mengatur toggle password
    private fun setupPasswordToggle() {
        binding.ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            
            if (isPasswordVisible) {
                // Tampilkan password
                binding.passwordLogin.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_open)
            } else {
                // Sembunyikan password
                binding.passwordLogin.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_closed)
            }
            
            // Pindahkan kursor ke akhir teks
            binding.passwordLogin.setSelection(binding.passwordLogin.text.length)
        }
    }

    private val inputWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            updateButtonState()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun updateButtonState() {
        val email = binding.emailLogin.text.toString().trim()
        val password = binding.passwordLogin.text.toString().trim()

        val isFormValid = email.isNotEmpty() && password.isNotEmpty()

        binding.btnLogin.isEnabled = isFormValid
        binding.btnLogin.setTextColor(
            ContextCompat.getColor(
                this,
                if (isFormValid) android.R.color.white else R.color.typhographySecondaryGray
            )
        )
    }
}
