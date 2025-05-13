package com.example.galdanaja.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.galdanaja.R
import com.example.galdanaja.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
import java.util.UUID

class EditProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedImageUri: Uri? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        // Load user data
        loadUserProfile()
        
        // Set click listeners
        binding.icBack.setOnClickListener {
            finish()
        }
        
        binding.tvBack.setOnClickListener {
            finish()
        }
        
        binding.btnCamera.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }
        
        binding.tvEdit.setOnClickListener {
            saveUserProfile()
        }
    }
    
    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        val phone = document.getString("phone")
                        val profileImageUrl = document.getString("profileImage")
                        
                        binding.emailRegister.setText(name ?: "")
                        
                        if (phone != null && phone.isNotEmpty()) {
                            if (phone.startsWith("+62")) {
                                binding.kodeNegara.setText("+62")
                                binding.nomorTelpon.setText(phone.substring(3))
                            } else {
                                binding.nomorTelpon.setText(phone)
                            }
                        }
                        
                        if (profileImageUrl != null) {
                            Glide.with(this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.avatars)
                                .error(R.drawable.avatars)
                                .into(binding.imgProfile)
                        }
                    } else {
                        // Fallback ke data dari Auth
                        binding.emailRegister.setText(currentUser.displayName ?: "")
                        
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
    
    private fun saveUserProfile() {
        val currentUser = auth.currentUser
        
        if (currentUser != null) {
            val name = binding.emailRegister.text.toString()
            val kodeNegara = binding.kodeNegara.text.toString()
            val nomorTelpon = binding.nomorTelpon.text.toString()
            val phone = "$kodeNegara$nomorTelpon"
            
            // Tambahkan email ke userData untuk memastikan data lengkap
            val userData = hashMapOf(
                "name" to name,
                "phone" to phone,
                "email" to (currentUser.email ?: ""),
                "provider" to (currentUser.providerData.firstOrNull()?.providerId ?: "password")
            )
            
            // Periksa apakah dokumen pengguna sudah ada
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Dokumen ada, lakukan update
                        if (selectedImageUri != null) {
                            uploadToHosting(selectedImageUri!!, userData, currentUser.uid)
                        } else {
                            updateUserData(currentUser.uid, userData)
                        }
                    } else {
                        // Dokumen belum ada, lakukan set (bukan update)
                        if (selectedImageUri != null) {
                            uploadToHosting(selectedImageUri!!, userData, currentUser.uid, true)
                        } else {
                            setUserData(currentUser.uid, userData)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal memeriksa data pengguna: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    
    // Modifikasi fungsi uploadToHosting untuk mendukung set dan update
    private fun uploadToHosting(fileUri: Uri, userData: HashMap<String, String>, userId: String, isNewUser: Boolean = false) {
        try {
            // Gunakan content resolver untuk mendapatkan file path yang benar
            val filePath = getRealPathFromURI(fileUri)
            val file = File(filePath)
            
            if (!file.exists()) {
                runOnUiThread {
                    Toast.makeText(this@EditProfileActivity, "File tidak ditemukan", Toast.LENGTH_SHORT).show()
                    // Jika file tidak ada, tetap update data lainnya
                    updateUserData(userId, userData)
                }
                return
            }
            
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
                        Toast.makeText(this@EditProfileActivity, "Gagal upload: ${e.message}", Toast.LENGTH_SHORT).show()
                        // Jika gagal upload, tetap update data lainnya
                        updateUserData(userId, userData)
                    }
                }
    
                // Ubah bagian callback onResponse
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            val json = JSONObject(responseBody)
    
                            if (json.getBoolean("success")) {
                                val fileUrl = json.getString("url")
                                runOnUiThread {
                                    Toast.makeText(this@EditProfileActivity, "Upload berhasil!", Toast.LENGTH_SHORT).show()
                                    userData["profileImage"] = fileUrl
                                    
                                    // Gunakan fungsi yang sesuai berdasarkan isNewUser
                                    if (isNewUser) {
                                        setUserData(userId, userData)
                                    } else {
                                        updateUserData(userId, userData)
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@EditProfileActivity, "Upload gagal di server: ${json.optString("message", "Unknown error")}", Toast.LENGTH_SHORT).show()
                                    // Jika gagal upload, tetap update data lainnya
                                    updateUserData(userId, userData)
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@EditProfileActivity, "Respons kosong dari server", Toast.LENGTH_SHORT).show()
                                updateUserData(userId, userData)
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@EditProfileActivity, "Error memproses respons: ${e.message}", Toast.LENGTH_SHORT).show()
                            updateUserData(userId, userData)
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Error saat memproses file: ${e.message}", Toast.LENGTH_SHORT).show()
            // Jika terjadi error, tetap update data lainnya
            updateUserData(userId, userData)
        }
    }
    
    // Tambahkan fungsi untuk mendapatkan path file yang sebenarnya dari URI
    private fun getRealPathFromURI(uri: Uri): String {
        // Untuk file yang sudah di-crop oleh UCrop
        if (uri.path?.contains("cropped_") == true && uri.scheme == "file") {
            return uri.path!!
        }
        
        // Untuk file dari galeri
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        
        // Fallback: gunakan cache file jika path tidak bisa didapatkan
        val fileName = "temp_profile_${System.currentTimeMillis()}.jpg"
        val tempFile = File(cacheDir, fileName)
        
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = tempFile.outputStream()
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            return tempFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            // Jika gagal, kembalikan path URI saja
            return uri.path ?: ""
        }
    }
    
    private fun setUserData(userId: String, userData: HashMap<String, String>) {
        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil berhasil dibuat", Toast.LENGTH_SHORT).show()
                
                // Kirim hasil update kembali ke ProfileActivity
                val resultIntent = Intent()
                resultIntent.putExtra("PROFILE_UPDATED", true)
                setResult(Activity.RESULT_OK, resultIntent)
                
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal membuat profil", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun updateUserData(userId: String, userData: HashMap<String, String>) {
        db.collection("users").document(userId)
            .update(userData as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                
                // Kirim hasil update kembali ke ProfileActivity
                val resultIntent = Intent()
                resultIntent.putExtra("PROFILE_UPDATED", true)
                setResult(Activity.RESULT_OK, resultIntent)
                
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
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
            if (resultUri != null) {
                binding.imgProfile.setImageURI(resultUri)
                selectedImageUri = resultUri
                
                // Log untuk debugging
                Log.d("EditProfile", "URI hasil crop: $resultUri")
                Log.d("EditProfile", "Path: ${resultUri.path}")
                Log.d("EditProfile", "File exists: ${File(resultUri.path ?: "").exists()}")
            } else {
                Toast.makeText(this, "Gagal mendapatkan gambar hasil crop", Toast.LENGTH_SHORT).show()
            }
        }
    }
}