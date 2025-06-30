package com.example.galdanaja.ui.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.galdanaja.MainActivity
import com.example.galdanaja.R
import com.example.galdanaja.databinding.ActivityEndOnBoardingBinding
import com.example.galdanaja.ui.profile.CreateProfileActivity
import com.example.galdanaja.ui.register.RegisterActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class EndOnBoardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEndOnBoardingBinding
    private lateinit var oneTapClient: SignInClient

    private val GOOGLE_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEndOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Google Sign-In setup
        oneTapClient = Identity.getSignInClient(this)

        binding.googleButton.setOnClickListener {
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .build()

            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener {
                    Log.d("GoogleSignIn", "Sign-In berhasil. Pending Intent: ${it.pendingIntent}")
                    startIntentSenderForResult(
                        it.pendingIntent.intentSender, GOOGLE_SIGN_IN,
                        null, 0, 0, 0, null
                    )
                }
                .addOnFailureListener {
                    Log.e("GoogleSignIn", "Gagal login dengan Google.", it)
                    Toast.makeText(this, "Gagal login dengan Google.", Toast.LENGTH_SHORT).show()
                }
        }

        binding.facebookButton.setOnClickListener {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.emailButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
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
                                // Login Firebase berhasil, sekarang cek profilnya di Firestore
                                val uid = task.result?.user?.uid
                                if (uid != null) {
                                    checkProfileAndNavigate(uid) // Panggil fungsi navigasi cerdas
                                } else {
                                    Toast.makeText(this, "Gagal mendapatkan UID pengguna.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Gagal mendapatkan token Google", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Error menangani hasil sign-in", e)
                Toast.makeText(this, "Terjadi kesalahan saat login.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Fungsi ini mengecek ke Firestore apakah user dengan UID tertentu sudah punya
     * dokumen profil atau belum, lalu mengarahkan ke halaman yang sesuai.
     */
    private fun checkProfileAndNavigate(uid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // KASUS 1: Profil sudah ada di Firestore
                    // Langsung arahkan ke halaman utama
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // KASUS 2: Pengguna baru, profil belum ada
                    // Arahkan ke halaman pembuatan profil
                    val intent = Intent(this, CreateProfileActivity::class.java)

                    // BONUS: Kirim data dari Google untuk mengisi form secara otomatis
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    intent.putExtra("USER_NAME", currentUser?.displayName)
                    intent.putExtra("USER_PHOTO_URL", currentUser?.photoUrl.toString())

                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memeriksa profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}