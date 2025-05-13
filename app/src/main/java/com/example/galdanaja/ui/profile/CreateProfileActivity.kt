package com.example.galdanaja.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.galdanaja.MainActivity
import com.example.galdanaja.databinding.ActivityCreateProfileBinding
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.yalantis.ucrop.UCrop
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.io.File

class CreateProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateProfileBinding
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreateProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupListeners()

        binding.btnCamera.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }

        binding.btnRegisterProfile.setOnClickListener {
            if (imageUri != null) {
                uploadToHosting(imageUri!!)
            } else {
                Toast.makeText(this, "Pilih foto dulu!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setupListeners() {
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.emailRegister.addTextChangedListener(textWatcher)
        binding.kodeNegara.addTextChangedListener(textWatcher)
        binding.nomorTelpon.addTextChangedListener(textWatcher)
    }

    private fun validateForm() {
        val name = binding.emailRegister.text.toString().trim()
        val countryCode = binding.kodeNegara.text.toString().trim()
        val phone = binding.nomorTelpon.text.toString().trim()

        binding.btnRegisterProfile.isEnabled = name.isNotEmpty() && countryCode.isNotEmpty() && phone.isNotEmpty()
    }



    private fun uploadToHosting(fileUri: Uri) {
        val file = File(fileUri.path!!)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", file.name,
                RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
            )
            .build()

        val request = Request.Builder()
            .url("https://celotehyuk.com/upload.php")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CreateProfileActivity, "Gagal upload: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody)

                if (json.getBoolean("success")) {
                    val fileUrl = json.getString("url")
                    runOnUiThread {
                        Toast.makeText(this@CreateProfileActivity, "Upload berhasil!", Toast.LENGTH_SHORT).show()
                        saveUserToFirestore(fileUrl)  // simpan user + url ke firestore
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CreateProfileActivity, "Upload gagal di server.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }


    private fun saveUserToFirestore(profileImageUrl: String) {
        val fullName = binding.emailRegister.text.toString().trim()
        val phoneNumber = "${binding.kodeNegara.text}${binding.nomorTelpon.text}"

        val userMap = hashMapOf(
            "name" to fullName,
            "phone" to phoneNumber,
            "profileImage" to profileImageUrl
        )

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile berhasil disimpan!", Toast.LENGTH_SHORT).show()

                    // ➡️ Setelah berhasil simpan, langsung ke MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal simpan profile: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User belum login.", Toast.LENGTH_SHORT).show()
        }
    }



    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                startCrop(uri)
            }
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options()
        options.setCircleDimmedLayer(true)
        options.setCompressionQuality(90)

        UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(500, 500)
            .withOptions(options)
            .start(this)
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            binding.imgProfile.setImageURI(resultUri)
            imageUri = resultUri
        }
    }
}
