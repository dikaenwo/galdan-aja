package com.example.galdanaja.adapter

import android.content.Context
import android.content.Intent
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
                // Fallback ke gambar lokal jika menggunakan data lama
                imgItemPhoto.setImageResource(R.drawable.nunu)
            }
            
            tvItemName.text = galdan.name
            textView5.text = "Rp.${galdan.price}"
            
            // Tampilkan informasi user
            if (galdan.userPhotoUrl.isNotEmpty()) {
                Glide.with(context)
                    .load(galdan.userPhotoUrl)
                    .placeholder(R.drawable.avatars)
                    .error(R.drawable.avatars)
                    .into(imgUserPhoto)
            }
            
            textView6.text = galdan.userName
            
            // Set click listener untuk item
            root.setOnClickListener {
                val intent = Intent(context, DetailProductActivity::class.java)
                // Tambahkan data yang diperlukan ke intent
                intent.putExtra("PRODUCT_ID", galdan.id)
                intent.putExtra("PRODUCT_NAME", galdan.name)
                intent.putExtra("PRODUCT_PRICE", "Rp.${galdan.price}")
                intent.putExtra("PRODUCT_STOCK", galdan.stock)  // Tambahkan stock ke intent
                intent.putExtra("PRODUCT_IMAGE_URL", galdan.imageUrl)
                intent.putExtra("PRODUCT_DESCRIPTION", galdan.description)
                intent.putExtra("PRODUCT_CATEGORY", galdan.category)
                intent.putExtra("PRODUCT_USER_ID", galdan.userId)
                intent.putExtra("PRODUCT_USER_NAME", galdan.userName)  // Tambahkan nama penjual
                intent.putExtra("PRODUCT_DATE", galdan.date)  // Tambahkan tanggal
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
