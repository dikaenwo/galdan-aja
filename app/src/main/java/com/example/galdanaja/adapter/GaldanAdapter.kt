package com.example.galdanaja.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.galdanaja.R
import com.example.galdanaja.ui.product.DetailProductActivity
import com.example.galdanaja.item.GaldanItem
import com.example.galdanaja.databinding.ItemRowDaftarGaldanBinding

class GaldanAdapter(private val context: Context, private val listGaldan: ArrayList<GaldanItem>) :
    RecyclerView.Adapter<GaldanAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRowDaftarGaldanBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRowDaftarGaldanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val galdan = listGaldan[position]

        with(holder.binding) {
            // Gunakan Glide untuk memuat gambar dari URL
            if (galdan.imageUrl.isNotEmpty()) {
                Glide.with(context)
                    .load(galdan.imageUrl)
                    .placeholder(R.drawable.nunu)
                    .error(R.drawable.nunu)
                    .into(imgItemPhoto)
            } else {
                imgItemPhoto.setImageResource(R.drawable.nunu)
            }
            tvItemName.text = if (galdan.name.length > 20) {
                "${galdan.name.substring(0, 20)}..."
            } else {
                galdan.name
            }

            textView5.text = "Rp.${galdan.price}"

            if (galdan.userPhotoUrl.isNotEmpty()) {
                Glide.with(context)
                    .load(galdan.userPhotoUrl)
                    .placeholder(R.drawable.avatars)
                    .error(R.drawable.avatars)
                    .into(imgUserPhoto)
            }

            textView6.text = galdan.userName

            root.setOnClickListener {
                // Log untuk debugging
                Log.d("GaldanAdapter", "=== SENDING DATA TO DETAIL ===")
                Log.d("GaldanAdapter", "Product ID: ${galdan.id}")
                Log.d("GaldanAdapter", "Product Name: ${galdan.name}")
                Log.d("GaldanAdapter", "Product Price: ${galdan.price}")
                Log.d("GaldanAdapter", "Product Stock (raw): '${galdan.stock}'")

                // Handle empty stock - set default ke 10 jika kosong
                val stockValue = if (galdan.stock.isBlank()) {
                    "10" // Default stock jika kosong
                } else {
                    galdan.stock
                }

                Log.d("GaldanAdapter", "Product Stock (processed): '$stockValue'")

                val intent = Intent(context, DetailProductActivity::class.java)
                intent.putExtra("PRODUCT_ID", galdan.id)
                intent.putExtra("PRODUCT_NAME", galdan.name)
                intent.putExtra("PRODUCT_PRICE", "Rp.${galdan.price}")
                intent.putExtra("PRODUCT_STOCK", stockValue) // Kirim stock yang sudah diproses
                intent.putExtra("PRODUCT_IMAGE_URL", galdan.imageUrl)
                intent.putExtra("PRODUCT_DESCRIPTION", galdan.description)
                intent.putExtra("PRODUCT_CATEGORY", galdan.category)
                intent.putExtra("PRODUCT_USER_ID", galdan.userId)
                intent.putExtra("PRODUCT_USER_NAME", galdan.userName)
                intent.putExtra("PRODUCT_DATE", galdan.date)

                Log.d("GaldanAdapter", "Intent created, starting activity...")
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = listGaldan.size

    // Fungsi untuk memperbarui data
    fun updateData(newList: ArrayList<GaldanItem>) {
        listGaldan.clear()
        listGaldan.addAll(newList)
        notifyDataSetChanged()
    }
}