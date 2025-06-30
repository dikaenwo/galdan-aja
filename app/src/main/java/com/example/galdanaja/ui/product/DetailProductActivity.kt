package com.example.galdanaja.ui.product

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.galdanaja.R
import com.example.galdanaja.databinding.ActivityDetailProductBinding
import com.example.galdanaja.helper.FirebaseHelper
import com.example.galdanaja.item.CartItem

class DetailProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailProductBinding
    private var quantity = 1
    private var basePrice = 0
    private var productStock = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getStringExtra("PRODUCT_ID") ?: ""
        val productName = intent.getStringExtra("PRODUCT_NAME") ?: "Produk"
        val productPrice = intent.getStringExtra("PRODUCT_PRICE") ?: "Rp.0"
        productStock = intent.getStringExtra("PRODUCT_STOCK")?.toIntOrNull() ?: 0
        val productImageUrl = intent.getStringExtra("PRODUCT_IMAGE_URL") ?: ""
        val productDescription = intent.getStringExtra("PRODUCT_DESCRIPTION") ?: "Tidak ada deskripsi"
        val productCategory = intent.getStringExtra("PRODUCT_CATEGORY") ?: ""
        val productUserId = intent.getStringExtra("PRODUCT_USER_ID") ?: ""
        val productDate = intent.getStringExtra("PRODUCT_DATE") ?: "Tanggal tidak tersedia"
        val sellerName = intent.getStringExtra("PRODUCT_USER_NAME") ?: "Penjual"

        // Parse base price sekali di awal
        basePrice = productPrice.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0

        // Log untuk debugging - lebih detail
        Log.d("DetailProductActivity", "=== DEBUGGING INFO ===")
        Log.d("DetailProductActivity", "Raw Product Price: '$productPrice'")
        Log.d("DetailProductActivity", "Raw Product Stock: '${intent.getStringExtra("PRODUCT_STOCK")}'")
        Log.d("DetailProductActivity", "Parsed Base Price: $basePrice")
        Log.d("DetailProductActivity", "Parsed Product Stock: $productStock")
        Log.d("DetailProductActivity", "Initial Quantity: $quantity")
        Log.d("DetailProductActivity", "======================")

        // Debugging: Cek semua intent extras
        intent.extras?.let { bundle ->
            for (key in bundle.keySet()) {
                val value = bundle.get(key)
                Log.d("DetailProductActivity", "Intent Extra: $key = '$value' (${value?.javaClass?.simpleName})")
            }
        }

        binding.textView12.text = productName
        binding.tvTotalPrice.text = productPrice
        binding.textView13.text = productDescription
        binding.tvDetailContent.text = "- Kategori: $productCategory\n- Stok: $productStock\n- ${productDescription.replace(".", "\n-")}"

        binding.tvSeller.text = sellerName
        binding.tvDate.text = productDate

        if (productImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(productImageUrl)
                .placeholder(R.drawable.nunu)
                .error(R.drawable.nunu)
                .into(binding.shapeableImageView)
        } else {
            binding.shapeableImageView.setImageResource(R.drawable.nunu)
        }

        binding.imageButton2.setOnClickListener {
            finish()
        }

        binding.imageButton3.setOnClickListener {
            // Ambil ID dan nama penjual dari intent
            val sellerId = intent.getStringExtra("PRODUCT_USER_ID") ?: ""
            val sellerName = intent.getStringExtra("PRODUCT_USER_NAME") ?: "Penjual"

            if (sellerId.isNotEmpty()) {
                startChatWithSeller(sellerId, sellerName)
            } else {
                Toast.makeText(this, "ID Penjual tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }

        // Set initial quantity display
        binding.tvQuantity.text = quantity.toString()

        binding.btnMinus.setOnClickListener {
            Log.d("DetailProductActivity", "Tombol MINUS diklik")
            if (quantity > 1) {
                quantity--
                updateQuantityDisplay()
                Log.d("DetailProductActivity", "Kuantitas setelah dikurangi: $quantity")
            } else {
                Log.d("DetailProductActivity", "Kuantitas sudah minimum: $quantity")
                Toast.makeText(this, "Kuantitas minimal adalah 1", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnPlus.setOnClickListener {
            Log.d("DetailProductActivity", "=== TOMBOL PLUS DIKLIK ===")
            Log.d("DetailProductActivity", "Current quantity: $quantity")
            Log.d("DetailProductActivity", "Product stock: $productStock")
            Log.d("DetailProductActivity", "Raw stock string: '${intent.getStringExtra("PRODUCT_STOCK")}'")

            // Cek apakah masih bisa menambah quantity
            when {
                productStock <= 0 -> {
                    Log.d("DetailProductActivity", "KONDISI: Stok produk habis atau tidak valid")
                    Toast.makeText(this, "Stok produk habis atau tidak valid", Toast.LENGTH_SHORT).show()
                }
                quantity >= productStock -> {
                    Log.d("DetailProductActivity", "KONDISI: Kuantitas sudah mencapai maksimal stok")
                    Toast.makeText(this, "Kuantitas sudah mencapai maksimal stok ($productStock)", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Log.d("DetailProductActivity", "KONDISI: Menambah quantity")
                    quantity++
                    updateQuantityDisplay()
                    Log.d("DetailProductActivity", "Kuantitas berhasil ditambah menjadi: $quantity")
                }
            }
            Log.d("DetailProductActivity", "========================")
        }

        binding.btnProceedToPay.setOnClickListener {
            val userId = FirebaseHelper.auth.currentUser?.uid

            if (userId == null) {
                Toast.makeText(this, "Anda belum login", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val productId = intent.getStringExtra("PRODUCT_ID") ?: return@setOnClickListener

            val cartItem = hashMapOf(
                "productId" to productId,
                "name" to binding.textView12.text.toString(),
                "price" to basePrice,
                "imageUrl" to intent.getStringExtra("PRODUCT_IMAGE_URL"),
                "quantity" to quantity,
                "sellerId" to productUserId // <-- PASTIKAN ANDA MENYIMPAN INI
            )

            FirebaseHelper.firestore
                .collection("carts")
                .document(userId)
                .collection("items")
                .document(productId)
                .set(cartItem)
                .addOnSuccessListener {
                    Toast.makeText(this, "Produk ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                }
        }


    }

    private fun updateQuantityDisplay() {
        binding.tvQuantity.text = quantity.toString()
        updateTotalPrice()
    }

    private fun updateTotalPrice() {
        val totalPrice = basePrice * quantity
        binding.tvTotalPrice.text = "Rp ${String.format("%,d", totalPrice).replace(',', '.')}"
        Log.d("DetailProductActivity", "Harga diupdate: Rp $totalPrice ($quantity x $basePrice)")
    }

    private fun startChatWithSeller(sellerId: String, sellerName: String) {
        // 1. Dapatkan ID user saat ini (pembeli)
        val currentUserId = FirebaseHelper.auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Anda harus login untuk memulai chat", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Cek agar user tidak bisa chat dengan diri sendiri
        if (currentUserId == sellerId) {
            Toast.makeText(this, "Anda tidak bisa mengirim pesan ke diri sendiri", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Buat daftar peserta untuk query. Urutkan agar konsisten.
        val participants = listOf(currentUserId, sellerId).sorted()

        // 4. Query ke Firestore untuk mencari chat yang pesertanya sama persis
        FirebaseHelper.firestore.collection("chats")
            .whereEqualTo("participants", participants)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // 5a. JIKA CHAT DITEMUKAN: Langsung navigasi dengan ID yang ada
                    val chatId = querySnapshot.documents.first().id
                    navigateToChatActivity(chatId, sellerId, sellerName)
                } else {
                    // 5b. JIKA CHAT TIDAK DITEMUKAN: Buat dokumen chat baru
                    val newChat = hashMapOf(
                        "participants" to participants,
                        "lastMessage" to "Mulai percakapan...",
                        "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )

                    FirebaseHelper.firestore.collection("chats")
                        .add(newChat)
                        .addOnSuccessListener { documentReference ->
                            // Navigasi setelah chat berhasil dibuat
                            navigateToChatActivity(documentReference.id, sellerId, sellerName)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal memulai chat: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mencari chat: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Di dalam DetailProductActivity.kt
    private fun navigateToChatActivity(chatId: String, sellerId: String, sellerName: String) {
        val intent = Intent(this, com.example.galdanaja.ui.chat.ChatActivity::class.java).apply {
            putExtra("CHAT_ID", chatId)
            putExtra("OTHER_USER_ID", sellerId)
            // Pastikan baris ini ada dan sellerName tidak kosong
            putExtra("OTHER_USER_NAME", sellerName)
        }
        startActivity(intent)
    }
}