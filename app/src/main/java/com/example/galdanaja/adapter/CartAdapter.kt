package com.example.galdanaja.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.galdanaja.item.CartItem
import com.example.galdanaja.databinding.ItemRowDaftarCartBinding

class CartAdapter(private val items: List<CartItem>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

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
            textView9.text = item.price
            imgItemPhoto.setImageResource(item.imageResId)
            textView10.text = item.quantity.toString()
            
            // Menambahkan fungsi untuk tombol plus dan minus
            btCartPlus.setOnClickListener {
                item.quantity++
                textView10.text = item.quantity.toString()
            }
            
            btCartMin.setOnClickListener {
                if (item.quantity > 1) {
                    item.quantity--
                    textView10.text = item.quantity.toString()
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}