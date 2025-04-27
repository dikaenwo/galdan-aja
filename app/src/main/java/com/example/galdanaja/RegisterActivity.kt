package com.example.galdanaja

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.galdanaja.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvMasuk.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.checkBox.buttonDrawable = ContextCompat.getDrawable(this, R.drawable.checkbox_selector)
        binding.checkBox.buttonTintList = null
        binding.emailRegister.addTextChangedListener(inputWatcher)
        binding.passwordRegister.addTextChangedListener(inputWatcher)
        binding.checkBox.setOnCheckedChangeListener { _, _ ->
            updateButtonState()
        }
        updateButtonState()

        binding.btnRegister.setOnClickListener {
            val email = binding.emailRegister.text.toString()
            val password = binding.passwordRegister.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty())
                FirebaseHelper.auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                    if (it.isSuccessful){
                        startActivity(Intent(this,LoginActivity::class.java))
                        finish()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this,it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
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
        val email = binding.emailRegister.text.toString().trim()
        val password = binding.passwordRegister.text.toString().trim()
        val isChecked = binding.checkBox.isChecked

        val isFormValid = email.isNotEmpty() && password.isNotEmpty() && isChecked

        binding.btnRegister.isEnabled = isFormValid
        binding.btnRegister.setTextColor(
            ContextCompat.getColor(
                this,
                if (isFormValid) android.R.color.white else R.color.typhographySecondaryGray
            )
        )
    }
}
