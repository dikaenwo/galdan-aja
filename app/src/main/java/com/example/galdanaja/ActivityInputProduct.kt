package com.example.galdanaja

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.galdanaja.databinding.ActivityInputProductBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ActivityInputProduct : AppCompatActivity() {
    private lateinit var binding: ActivityInputProductBinding
    private var imageUri: Uri? = null
    private val calendar = Calendar.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInputProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupListeners()
        setupDatePicker()
    }
    
    private fun setupListeners() {
        // Setup image upload
        binding.cardImageUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }
        
        // Setup form validation
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        
        binding.etProductName.addTextChangedListener(textWatcher)
        binding.etPrice.addTextChangedListener(textWatcher)
        binding.etStock.addTextChangedListener(textWatcher)  // Tambahkan listener untuk stock
        binding.etDate.addTextChangedListener(textWatcher)
        binding.etDescription.addTextChangedListener(textWatcher)
        
        // Setup submit button
        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                if (imageUri != null) {
                    uploadToHosting(imageUri!!)
                } else {
                    Toast.makeText(this, "Pilih gambar produk dulu!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Lengkapi semua data produk!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        
        binding.etDate.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
    
    private fun updateDateInView() {
        val format = "dd MMM yyyy"
        val sdf = SimpleDateFormat(format, Locale("id", "ID"))
        binding.etDate.setText(sdf.format(calendar.time))
    }
    
    private fun validateForm(): Boolean {
        val productName = binding.etProductName.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val stock = binding.etStock.text.toString().trim()  // Ambil nilai stock
        val date = binding.etDate.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        
        binding.btnSubmit.isEnabled = productName.isNotEmpty() && price.isNotEmpty() && 
                                      stock.isNotEmpty() &&  // Tambahkan validasi stock
                                      date.isNotEmpty() && description.isNotEmpty() && 
                                      (binding.rbMakanan.isChecked || binding.rbMinuman.isChecked)
        
        return binding.btnSubmit.isEnabled
    }
    
    private fun uploadToHosting(fileUri: Uri) {
        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.text = "Sedang Mengupload..."
        
        try {
            // Gunakan ContentResolver untuk mendapatkan InputStream dari Uri
            val inputStream = contentResolver.openInputStream(fileUri)
            val byteArray = inputStream?.readBytes()
            inputStream?.close()
            
            if (byteArray == null) {
                runOnUiThread {
                    Toast.makeText(this, "Gagal membaca file gambar", Toast.LENGTH_SHORT).show()
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = "Tambah Produk"
                }
                return
            }
            
            // Buat nama file unik
            val fileName = "product_${System.currentTimeMillis()}.jpg"
            
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file", fileName,
                    RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
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
                        Toast.makeText(this@ActivityInputProduct, "Gagal upload: ${e.message}", Toast.LENGTH_SHORT).show()
                        binding.btnSubmit.isEnabled = true
                        binding.btnSubmit.text = "Tambah Produk"
                    }
                }
    
                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    val json = JSONObject(responseBody)
    
                    if (json.getBoolean("success")) {
                        val fileUrl = json.getString("url")
                        runOnUiThread {
                            Toast.makeText(this@ActivityInputProduct, "Upload berhasil!", Toast.LENGTH_SHORT).show()
                            saveProductToFirestore(fileUrl)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@ActivityInputProduct, "Upload gagal di server.", Toast.LENGTH_SHORT).show()
                            binding.btnSubmit.isEnabled = true
                            binding.btnSubmit.text = "Tambah Produk"
                        }
                    }
                }
            })
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSubmit.isEnabled = true
                binding.btnSubmit.text = "Tambah Produk"
            }
        }
    }
    
    private fun saveProductToFirestore(imageUrl: String) {
        val productName = binding.etProductName.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val stock = binding.etStock.text.toString().trim()  // Ambil nilai stock
        val date = binding.etDate.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val category = if (binding.rbMakanan.isChecked) "Makanan" else "Minuman"
        
        val productMap = hashMapOf(
            "name" to productName,
            "price" to price.toInt(),
            "stock" to stock.toInt(),  // Tambahkan stock ke Firestore
            "date" to date,
            "description" to description,
            "category" to category,
            "imageUrl" to imageUrl,
            "userId" to FirebaseAuth.getInstance().currentUser?.uid,
            "createdAt" to System.currentTimeMillis()
        )
        
        val db = FirebaseFirestore.getInstance()
        db.collection("products")
            .add(productMap)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Produk berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke halaman sebelumnya
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan produk: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSubmit.isEnabled = true
                binding.btnSubmit.text = "Tambah Produk"
            }
    }
    
    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Simpan URI gambar
                imageUri = uri
                
                try {
                    // Gunakan MediaStore untuk menampilkan preview gambar
                    binding.ivUploadIcon.setImageURI(null) // Reset image view
                    binding.ivUploadIcon.setImageURI(uri) // Set gambar baru
                    binding.ivUploadIcon.setColorFilter(null) // Hapus tint
                    binding.ivUploadIcon.scaleType = ImageView.ScaleType.CENTER_CROP
                    
                    // Ubah ukuran ImageView menjadi penuh
                    val layoutParams = binding.ivUploadIcon.layoutParams as LinearLayout.LayoutParams
                    layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                    layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
                    binding.ivUploadIcon.layoutParams = layoutParams
                    
                    // Sembunyikan teks "Select file"
                    binding.tvSelectFile.visibility = View.GONE
                    
                    // Ubah background dari dashed border menjadi solid
                    binding.layoutImageUpload.setBackgroundResource(android.R.color.transparent)
                    
                    // Pastikan gambar terlihat dengan benar
                    binding.ivUploadIcon.invalidate()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error menampilkan gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}