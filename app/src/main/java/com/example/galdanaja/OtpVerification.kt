package com.example.galdanaja

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.example.galdanaja.databinding.ActivityOtpVerificationBinding

class OtpVerification : AppCompatActivity() {
    private lateinit var binding: ActivityOtpVerificationBinding
    private lateinit var verificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        verificationId = intent.getStringExtra("verificationId") ?: ""

        setupOtpInputs()

        val otpFields = listOf(
            binding.inputOTP1, binding.inputOTP2,
            binding.inputOTP3, binding.inputOTP4
        )

        binding.inputOTP4.setOnEditorActionListener { _, _, _ ->
            verifyOtp(otpFields)
            true
        }
    }

    private fun verifyOtp(otpFields: List<EditText>) {
        val code = otpFields.joinToString("") { it.text.toString().trim() }

        if (code.length != 6) {
            Toast.makeText(this, "Kode OTP harus 6 digit", Toast.LENGTH_SHORT).show()
            return
        }

        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, code)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Verifikasi berhasil!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Kode OTP tidak valid", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setupOtpInputs() {
        val inputs = listOf(
            binding.inputOTP1,
            binding.inputOTP2,
            binding.inputOTP3,
            binding.inputOTP4
        )

        for (i in inputs.indices) {
            inputs[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < inputs.size - 1) {
                        inputs[i + 1].requestFocus()
                    } else if (s?.isEmpty() == true && i > 0) {
                        inputs[i - 1].requestFocus()
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }



}
