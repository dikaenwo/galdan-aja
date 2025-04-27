package com.example.galdanaja

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.galdanaja.databinding.ActivityEndOnBoardingBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

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

        // Facebook Button
        binding.facebookButton.setOnClickListener {
            // Menampilkan Toast dengan pesan "Coming Soon"
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
        }

        // Email Button (Jika kamu ingin menambahkan)
        binding.emailButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java)) // Kamu bisa pindah ke RegisterActivity atau LoginActivity
        }
    }

    // Menangani hasil dari Google Sign-In
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            oneTapClient.getSignInCredentialFromIntent(data).googleIdToken?.let { idToken ->
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, MainActivity::class.java)) // Pindah ke halaman utama setelah login
                            finish()
                        } else {
                            Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } ?: Toast.makeText(this, "Gagal ambil token Google", Toast.LENGTH_SHORT).show()
        }
    }
}
