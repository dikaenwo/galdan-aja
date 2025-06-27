package com.example.galdanaja.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.galdanaja.adapter.CartAdapter
import com.example.galdanaja.databinding.FragmentCartBinding
import com.example.galdanaja.helper.FirebaseHelper
import com.example.galdanaja.item.CartItem

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val cartItems = mutableListOf<CartItem>()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi adapter dengan listener perubahan
        cartAdapter = CartAdapter(cartItems) {
            updateTotal()
        }

        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }

        loadCartData()
    }

    private fun loadCartData() {
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

                    cartItems.add(
                        CartItem(
                            productId = productId,
                            name = name,
                            price = price,
                            imageUrl = imageUrl,
                            quantity = quantity
                        )
                    )
                }

                cartAdapter.notifyDataSetChanged()
                updateTotal()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal mengambil data keranjang", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTotal() {
        val total = cartItems.sumOf { it.price * it.quantity }
        binding.tvTotalPrice.text = "Rp ${String.format("%,d", total).replace(',', '.')}"

        binding.btnProceedToPay.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(context, "Keranjang kosong", Toast.LENGTH_SHORT).show()
            } else {
                // Checkout simulasi: hapus isi cart
                val userId = FirebaseHelper.auth.currentUser?.uid ?: return@setOnClickListener
                FirebaseHelper.firestore
                    .collection("carts")
                    .document(userId)
                    .collection("items")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val batch = FirebaseHelper.firestore.batch()
                        snapshot.documents.forEach { doc ->
                            batch.delete(doc.reference)
                        }
                        batch.commit().addOnSuccessListener {
                            cartItems.clear()
                            cartAdapter.notifyDataSetChanged()
                            updateTotal()
                            Toast.makeText(context, "Pembayaran berhasil & keranjang dikosongkan", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Gagal saat checkout", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
