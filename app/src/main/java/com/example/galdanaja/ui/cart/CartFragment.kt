package com.example.galdanaja.ui.cart

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.galdanaja.R
import com.example.galdanaja.adapter.CartAdapter
import com.example.galdanaja.databinding.FragmentCartBinding
import com.example.galdanaja.helper.FirebaseHelper
import com.example.galdanaja.item.CartItem
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val cartItems = mutableListOf<CartItem>()
    private lateinit var cartAdapter: CartAdapter
    private var selectedProofUri: Uri? = null

    // Launcher untuk memilih gambar
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedProofUri = uri
                // Menampilkan pratinjau di dialog yang sedang aktif
                (dialogView?.findViewById<ImageView>(R.id.ivProofPreview))?.apply {
                    setImageURI(uri)
                    visibility = View.VISIBLE
                }
                // Mengaktifkan tombol "Saya Sudah Bayar"
                positiveButton?.isEnabled = true
            }
        }
    }

    private var dialogView: View? = null
    private var positiveButton: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cartAdapter = CartAdapter(cartItems) { updateTotal() }
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }
        loadCartData()
    }

    private fun loadCartData() {
        // ... (Fungsi loadCartData Anda tetap sama)
        val userId = FirebaseHelper.auth.currentUser?.uid ?: return

        FirebaseHelper.firestore
            .collection("carts")
            .document(userId)
            .collection("items")
            .get()
            .addOnSuccessListener { documents ->
                cartItems.clear()
                for (doc in documents) {
                    val productId = doc.getString("productId") ?: continue
                    val name = doc.getString("name") ?: "Tanpa Nama"
                    val price = doc.getLong("price")?.toInt() ?: 0
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val quantity = doc.getLong("quantity")?.toInt() ?: 1
                    val sellerId = doc.getString("sellerId") ?: continue

                    cartItems.add(
                        CartItem(
                            productId = productId,
                            name = name,
                            price = price,
                            imageUrl = imageUrl,
                            quantity = quantity,
                            sellerId = sellerId
                        )
                    )
                }
                cartAdapter.notifyDataSetChanged()
                updateTotal()
            }
    }

    private fun showQrisDialog() {
        selectedProofUri = null // Reset URI setiap kali dialog dibuka
        dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_qris, null)
        val btnUpload = dialogView?.findViewById<Button>(R.id.btnUploadProof)

        btnUpload?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Scan QRIS & Upload Bukti")
            .setView(dialogView)
            .setPositiveButton("Saya sudah bayar") { _, _ ->
                selectedProofUri?.let {
                    uploadProofToHosting(it)
                } ?: Toast.makeText(requireContext(), "Harap upload bukti pembayaran", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()

        // Tombol "Saya sudah bayar" dinonaktifkan di awal
        positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton?.isEnabled = false
    }

    private fun uploadProofToHosting(fileUri: Uri) {
        Toast.makeText(requireContext(), "Mengupload bukti pembayaran...", Toast.LENGTH_SHORT).show()
        try {
            val inputStream = requireContext().contentResolver.openInputStream(fileUri)
            val byteArray = inputStream?.readBytes()
            inputStream?.close()

            if (byteArray == null) {
                Toast.makeText(requireContext(), "Gagal membaca file gambar", Toast.LENGTH_SHORT).show()
                return
            }

            val fileName = "proof_${System.currentTimeMillis()}.jpg"
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull()))
                .build()

            val request = Request.Builder()
                .url("https://celotehyuk.com/upload.php") // URL Hosting Anda
                .post(requestBody)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Gagal upload: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string()
                        val json = JSONObject(responseBody)
                        if (json.getBoolean("success")) {
                            val fileUrl = json.getString("url")
                            // Setelah URL didapat, baru proses pesanan
                            processPaymentAndCreateOrders(fileUrl)
                        } else {
                            requireActivity().runOnUiThread { Toast.makeText(requireContext(), "Upload gagal di server", Toast.LENGTH_SHORT).show()}
                        }
                    } catch (e: Exception) {
                        requireActivity().runOnUiThread { Toast.makeText(requireContext(), "Respon server tidak valid", Toast.LENGTH_SHORT).show()}
                    }
                }
            })
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processPaymentAndCreateOrders(proofUrl: String) {
        // ... (fungsi ini hampir sama, hanya perlu menambahkan proofUrl ke orderData)
        val buyerId = FirebaseHelper.auth.currentUser?.uid ?: return
        if (cartItems.isEmpty()) return

        val ordersBySeller = cartItems.groupBy { it.sellerId }
        val db = FirebaseHelper.firestore
        val batch = db.batch()

        for ((sellerId, items) in ordersBySeller) {
            val orderRef = db.collection("orders").document()
            val subTotal = items.sumOf { it.price * it.quantity }

            val orderData = hashMapOf(
                "orderId" to orderRef.id,
                "buyerId" to buyerId,
                "sellerId" to sellerId,
                "items" to items.map { mapOf(
                    "productId" to it.productId, "name" to it.name, "price" to it.price,
                    "quantity" to it.quantity, "imageUrl" to it.imageUrl
                )},
                "totalAmount" to subTotal,
                "status" to "pending_validation",
                "proofOfPaymentUrl" to proofUrl, // <-- TAMBAHKAN URL BUKTI BAYAR DI SINI
                "timestamp" to System.currentTimeMillis()
            )
            batch.set(orderRef, orderData)
        }

        val cartItemsRef = db.collection("carts").document(buyerId).collection("items")
        for (item in cartItems) {
            batch.delete(cartItemsRef.document(item.productId))
        }

        batch.commit()
            .addOnSuccessListener {
                // KIRIM NOTIFIKASI SETELAH PESANAN DIBUAT
                sendPendingNotification(buyerId)

                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Pembayaran berhasil, pesanan menunggu validasi.", Toast.LENGTH_LONG).show()
                    loadCartData()
                }
            }
            .addOnFailureListener { e ->
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Gagal memproses pesanan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateTotal() {
        // ... (fungsi updateTotal Anda tetap sama)
        val total = cartItems.sumOf { it.price * it.quantity }
        binding.tvTotalPrice.text = "Rp ${String.format("%,d", total).replace(',', '.')}"

        binding.btnProceedToPay.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(context, "Keranjang kosong", Toast.LENGTH_SHORT).show()
            } else {
                showQrisDialog()
            }
        }
    }


    private fun sendPendingNotification(userId: String) {
        val notification = mapOf(
            "userId" to userId,
            "title" to "Menunggu Konfirmasi Pembayaran",
            "description" to "Pembayaran Anda telah diterima dan sedang menunggu validasi oleh admin. Mohon ditunggu.",
            "time" to System.currentTimeMillis()
        )

        FirebaseHelper.firestore.collection("notifications")
            .add(notification)
            .addOnSuccessListener {
                Log.d("CartFragment", "Notifikasi 'pending' berhasil dikirim.")
            }
            .addOnFailureListener {
                Log.w("CartFragment", "Gagal mengirim notifikasi 'pending'.")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}