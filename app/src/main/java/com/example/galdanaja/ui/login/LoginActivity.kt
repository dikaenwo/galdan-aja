package com.example.galdanaja.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.galdanaja.MainActivity
import com.example.galdanaja.R
import com.example.galdanaja.databinding.ActivityLoginBinding
import com.example.galdanaja.helper.FirebaseHelper
import com.example.galdanaja.ui.profile.CreateProfileActivity
import com.example.galdanaja.ui.register.RegisterActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false

    // Properti untuk Google Sign-In
    private lateinit var oneTapClient: SignInClient
    private val GOOGLE_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseHelper.auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup listeners
        setupEmailPasswordLogin()
        setupGoogleLogin()
        setupOtherListeners()
    }

    private fun setupEmailPasswordLogin() {
        binding.emailLogin.addTextChangedListener(inputWatcher)
        binding.passwordLogin.addTextChangedListener(inputWatcher)
        updateButtonState()

        binding.btnLogin.setOnClickListener {
            val email = binding.emailLogin.text.toString()
            val password = binding.passwordLogin.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                FirebaseHelper.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = task.result?.user?.uid
                            if (uid != null) {
                                navigateAfterLogin(uid) // Panggil fungsi navigasi
                            }
                        } else {
                            Toast.makeText(this, "Email atau password tidak terdaftar", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun setupGoogleLogin() {
        oneTapClient = Identity.getSignInClient(this)
        binding.googleButton.setOnClickListener {
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                ).build()

            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, GOOGLE_SIGN_IN,
                        null, 0, 0, 0, null
                    )
                }
                .addOnFailureListener { e ->
                    Log.e("GoogleSignIn", "Gagal memulai Google Sign-In.", e)
                    Toast.makeText(this, "Gagal login dengan Google.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupOtherListeners() {
        binding.tvDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        setupPasswordToggle()
    }

    // Fungsi ini sekarang menjadi pusat logika setelah login berhasil
    private fun navigateAfterLogin(uid: String) {
        val db = FirebaseHelper.firestore
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Data profil ADA ➔ langsung ke MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // Data profil BELUM ADA ➔ ke CreateProfileActivity
                    startActivity(Intent(this, CreateProfileActivity::class.java))
                }
                finish() // Tutup LoginActivity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memeriksa data profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Menangani hasil dari Google Sign-In
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = task.result?.user?.uid
                                if (uid != null) {
                                    navigateAfterLogin(uid) // Panggil fungsi navigasi yang sama
                                }
                            } else {
                                Toast.makeText(this, "Login Firebase gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Gagal mendapatkan token Google", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Error handling sign in result", e)
                Toast.makeText(this, "Terjadi kesalahan saat login", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Helper Functions (tetap sama) ---
    private fun setupPasswordToggle() {
        binding.ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.passwordLogin.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_open)
            } else {
                binding.passwordLogin.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_closed)
            }
            binding.passwordLogin.setSelection(binding.passwordLogin.text.length)
        }
    }

    private val inputWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) { updateButtonState() }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun updateButtonState() {
        val email = binding.emailLogin.text.toString().trim()
        val password = binding.passwordLogin.text.toString().trim()
        binding.btnLogin.isEnabled = email.isNotEmpty() && password.isNotEmpty()
        binding.btnLogin.setTextColor(
            ContextCompat.getColor(this, if (binding.btnLogin.isEnabled) android.R.color.white else R.color.typhographySecondaryGray)
        )
    }
}