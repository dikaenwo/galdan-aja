package com.example.galdanaja

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.galdanaja.databinding.ActivityDetailProductBinding

class DetailProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailProductBinding
    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
    
        // Mendapatkan data dari intent
        val productName = intent.getStringExtra("PRODUCT_NAME") ?: "Risol Mayo isi Ayam dan Sosis"
        val productPrice = intent.getStringExtra("PRODUCT_PRICE") ?: "Rp.3000"
        val productImage = intent.getIntExtra("PRODUCT_PHOTO", R.drawable.nunu)
    
        // Mengatur data ke tampilan
        binding.textView12.text = productName
        binding.tvTotalPrice.text = productPrice
        binding.shapeableImageView.setImageResource(productImage)
    
        // Set listener untuk tombol kembali
        binding.imageButton2.setOnClickListener {
            finish()
        }
    
        // Set listener untuk tombol favorit
        binding.imageButton3.setOnClickListener {
            // Implementasi untuk menambahkan ke favorit
            Toast.makeText(this, "Ditambahkan ke favorit", Toast.LENGTH_SHORT).show()
        }
    
        // Set listener untuk tombol minus
        binding.btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
                updateTotalPrice(productPrice)
            }
        }
    
        // Set listener untuk tombol plus
        binding.btnPlus.setOnClickListener {
            quantity++
            binding.tvQuantity.text = quantity.toString()
            updateTotalPrice(productPrice)
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
    }
}