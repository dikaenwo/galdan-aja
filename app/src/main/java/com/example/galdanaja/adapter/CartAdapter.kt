package com.example.galdanaja.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.galdanaja.databinding.ItemRowDaftarCartBinding
import com.example.galdanaja.helper.FirebaseHelper
import com.example.galdanaja.item.CartItem

class CartAdapter(
    private val items: MutableList<CartItem>,
    private val onCartChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: ItemRowDaftarCartBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemRowDaftarCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            textView8.text = item.name
            textView9.text = "Rp ${String.format("%,d", item.price).replace(',', '.')}"
            textView10.text = item.quantity.toString()

            Glide.with(imgItemPhoto.context)
                .load(item.imageUrl)
                .into(imgItemPhoto)

            btCartPlus.setOnClickListener {
                item.quantity++
                textView10.text = item.quantity.toString()
                updateQuantityInFirestore(item)
                onCartChanged()
            }

            btCartMin.setOnClickListener {
                if (item.quantity > 1) {
                    item.quantity--
                    textView10.text = item.quantity.toString()
                    updateQuantityInFirestore(item)
                    onCartChanged()
                } else {
                    Toast.makeText(holder.itemView.context, "Minimal 1 item", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun updateQuantityInFirestore(item: CartItem) {
        val userId = FirebaseHelper.auth.currentUser?.uid ?: return
        FirebaseHelper.firestore
            .collection("carts")
            .document(userId)
            .collection("items")
            .document(item.productId)
            .update("quantity", item.quantity)
            .addOnFailureListener {
                Toast.makeText(null, "Gagal update quantity", Toast.LENGTH_SHORT).show()
            }
    }
}

