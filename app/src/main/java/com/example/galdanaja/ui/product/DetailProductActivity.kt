package com.example.galdanaja.ui.product

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.galdanaja.R
import com.example.galdanaja.databinding.ActivityDetailProductBinding

class DetailProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailProductBinding
    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
    
        // Mendapatkan data dari intent
        val productId = intent.getStringExtra("PRODUCT_ID") ?: ""
        val productName = intent.getStringExtra("PRODUCT_NAME") ?: "Produk"
        val productPrice = intent.getStringExtra("PRODUCT_PRICE") ?: "Rp.0"
        val productStock = intent.getStringExtra("PRODUCT_STOCK")?.toIntOrNull() ?: 0
        val productImageUrl = intent.getStringExtra("PRODUCT_IMAGE_URL") ?: ""
        val productDescription = intent.getStringExtra("PRODUCT_DESCRIPTION") ?: "Tidak ada deskripsi"
        val productCategory = intent.getStringExtra("PRODUCT_CATEGORY") ?: ""
        val productUserId = intent.getStringExtra("PRODUCT_USER_ID") ?: ""
        val productDate = intent.getStringExtra("PRODUCT_DATE") ?: "Tanggal tidak tersedia"
        val sellerName = intent.getStringExtra("PRODUCT_USER_NAME") ?: "Penjual"
    
        // Mengatur data ke tampilan
        binding.textView12.text = productName
        binding.tvTotalPrice.text = productPrice
        binding.textView13.text = productDescription
        binding.tvDetailContent.text = "- Kategori: $productCategory\n- Stok: $productStock\n- ${productDescription.replace(".", "\n-")}"
        
        // Mengatur informasi penjual
        binding.tvSeller.text = sellerName
        binding.tvDate.text = productDate
        
        // Memuat gambar produk menggunakan Glide
        if (productImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(productImageUrl)
                .placeholder(R.drawable.nunu)
                .error(R.drawable.nunu)
                .into(binding.shapeableImageView)
        } else {
            binding.shapeableImageView.setImageResource(R.drawable.nunu)
        }
    
        // Set listener untuk tombol kembali
        binding.imageButton2.setOnClickListener {
            finish()
        }
    
        // Set listener untuk tombol favorit
        binding.imageButton3.setOnClickListener {
            // Implementasi untuk menambahkan ke favorit
            Toast.makeText(this, "Ditambahkan ke favorit", Toast.LENGTH_SHORT).show()
        }
    
        // Inisialisasi nilai awal kuantitas
        binding.tvQuantity.text = quantity.toString()
        Log.d("DetailProductActivity", "Kuantitas awal: $quantity")
        
        // Set listener untuk tombol minus
        binding.btnMinus.setOnClickListener {
            Log.d("DetailProductActivity", "Tombol MINUS diklik")
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
                updateTotalPrice(productPrice)
                Log.d("DetailProductActivity", "Kuantitas setelah dikurangi: $quantity")
            } else {
                Log.d("DetailProductActivity", "Kuantitas sudah minimum: $quantity")
            }
        }
        
        // Set listener untuk tombol plus
        binding.btnPlus.setOnClickListener {
            Log.d("DetailProductActivity", "Tombol PLUS diklik")
            // Batasi kuantitas berdasarkan stok yang tersedia
            if (productStock > 0 && quantity < productStock) {
                quantity++
                binding.tvQuantity.text = quantity.toString()
                updateTotalPrice(productPrice)
                Log.d("DetailProductActivity", "Kuantitas setelah ditambah: $quantity, Stock: $productStock")
            } else if (productStock > 0) {
                Log.d("DetailProductActivity", "Stok tidak mencukupi. Kuantitas: $quantity, Stock: $productStock")
                Toast.makeText(this, "Stok produk tidak mencukupi", Toast.LENGTH_SHORT).show()
            }
        }
    
        // Set listener untuk tombol bayar
        binding.btnProceedToPay.setOnClickListener {
            // Implementasi untuk memesan produk
            Toast.makeText(this, "Produk berhasil dipesan", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateTotalPrice(basePrice: String) {
        // Mengekstrak angka dari string harga (misalnya "Rp.3000" menjadi 3000)
        val priceValue = basePrice.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
        
        // Menghitung total harga berdasarkan kuantitas
        val totalPrice = priceValue * quantity
        
        // Format total harga dengan format Rupiah
        binding.tvTotalPrice.text = "Rp ${totalPrice}"
        Log.d("DetailProductActivity", "Harga diupdate: Rp ${totalPrice} (${quantity} x ${priceValue})")
    }
}