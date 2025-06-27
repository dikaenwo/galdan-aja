package com.example.galdanaja.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.galdanaja.R
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

    private fun showQrisDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_qris, null)

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Scan QRIS untuk Membayar")
            .setView(dialogView)
            .setPositiveButton("Saya sudah bayar") { dialog, _ ->
                dialog.dismiss()
                savePaymentNotification()
            }
            .setNegativeButton("Batal", null)

        builder.create().show()
    }

    private fun savePaymentNotification() {
        val userId = FirebaseHelper.auth.currentUser?.uid ?: return

        val notification = mapOf(
            "title" to "Pembayaran sedang divalidasi",
            "description" to "Pembayaran kamu sedang diperiksa oleh admin.",
            "time" to System.currentTimeMillis(),
            "userId" to userId
        )

        FirebaseHelper.firestore
            .collection("notifications")
            .add(notification)
            .addOnSuccessListener {
                Toast.makeText(context, "Notifikasi terkirim", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal mengirim notifikasi", Toast.LENGTH_SHORT).show()
            }
    }



    private fun updateTotal() {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
