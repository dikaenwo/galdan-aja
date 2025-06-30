package com.example.galdanaja.penjual.tambah

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.galdanaja.databinding.FragmentTambahBinding // Ganti nama binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class TambahFragment : Fragment() {

    // Cara standar dan aman untuk menangani View Binding di Fragment
    private var _binding: FragmentTambahBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inisialisasi binding di onCreateView
        _binding = FragmentTambahBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Panggil semua setup listener dan UI di sini
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
        binding.etStock.addTextChangedListener(textWatcher)
        binding.etDate.addTextChangedListener(textWatcher)
        binding.etDescription.addTextChangedListener(textWatcher)

        // Setup submit button
        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                imageUri?.let {
                    uploadToHosting(it)
                } ?: Toast.makeText(requireContext(), "Pilih gambar produk dulu!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Lengkapi semua data produk!", Toast.LENGTH_SHORT).show()
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
                requireContext(), // Gunakan requireContext()
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
        val stock = binding.etStock.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        val isFormValid = productName.isNotEmpty() && price.isNotEmpty() &&
                stock.isNotEmpty() && date.isNotEmpty() && description.isNotEmpty() &&
                (binding.rbMakanan.isChecked || binding.rbMinuman.isChecked) && imageUri != null

        binding.btnSubmit.isEnabled = isFormValid
        return isFormValid
    }

    private fun uploadToHosting(fileUri: Uri) {
        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.text = "Sedang Mengupload..."

        try {
            val inputStream = requireContext().contentResolver.openInputStream(fileUri) // Gunakan requireContext()
            val byteArray = inputStream?.readBytes()
            inputStream?.close()

            if (byteArray == null) {
                handleUploadFailure("Gagal membaca file gambar")
                return
            }

            val fileName = "product_${System.currentTimeMillis()}.jpg"
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull()))
                .build()

            val request = Request.Builder()
                .url("https://celotehyuk.com/upload.php")
                .post(requestBody)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    handleUploadFailure("Gagal upload: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    try {
                        val json = JSONObject(responseBody)
                        if (json.getBoolean("success")) {
                            val fileUrl = json.getString("url")
                            requireActivity().runOnUiThread { // Gunakan requireActivity()
                                Toast.makeText(requireContext(), "Upload berhasil!", Toast.LENGTH_SHORT).show()
                                saveProductToFirestore(fileUrl)
                            }
                        } else {
                            handleUploadFailure("Upload gagal di server.")
                        }
                    } catch (e: Exception) {
                        handleUploadFailure("Respon server tidak valid.")
                    }
                }
            })
        } catch (e: Exception) {
            handleUploadFailure("Error: ${e.message}")
        }
    }

    // Fungsi bantuan untuk menangani kegagalan upload
    private fun handleUploadFailure(message: String) {
        requireActivity().runOnUiThread { // Gunakan requireActivity()
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            binding.btnSubmit.isEnabled = true
            binding.btnSubmit.text = "Tambah Produk"
        }
    }

    private fun saveProductToFirestore(imageUrl: String) {
        val productName = binding.etProductName.text.toString().trim()
        val priceString = binding.etPrice.text.toString().trim()
        val stockString = binding.etStock.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val category = if (binding.rbMakanan.isChecked) "Makanan" else "Minuman"
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            handleUploadFailure("Gagal mendapatkan ID user, silakan login ulang.")
            return
        }

        val productMap = hashMapOf(
            "name" to productName,
            "price" to (priceString.toLongOrNull() ?: 0L), // Konversi ke Long (angka)
            "stock" to (stockString.toLongOrNull() ?: 0L), // Konversi ke Long (angka)
            // ...
            "date" to date,
            "description" to description,
            "category" to category,
            "imageUrl" to imageUrl,
            "userId" to userId,
            "createdAt" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance().collection("products")
            .add(productMap)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Produk berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                // Kembali ke halaman sebelumnya
                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                handleUploadFailure("Gagal menyimpan produk: ${e.message}")
            }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri
                binding.ivUploadIcon.setImageURI(uri)
                binding.ivUploadIcon.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.tvSelectFile.visibility = View.GONE
                binding.layoutImageUpload.setBackgroundResource(android.R.color.transparent)
                validateForm() // Validasi ulang form setelah gambar dipilih
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hindari memory leak dengan membersihkan binding
        _binding = null
    }
}