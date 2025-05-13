package com.example.galdanaja.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
            imgItemPhoto.setImageResource(galdan.photo)
            tvItemName.text = galdan.name
            textView5.text = "Rp.${galdan.price}"
            
            // Set click listener untuk item
            root.setOnClickListener {
                val intent = Intent(context, DetailProductActivity::class.java)
                // Tambahkan data yang diperlukan ke intent
                intent.putExtra("PRODUCT_NAME", galdan.name)
                intent.putExtra("PRODUCT_PRICE", "Rp.${galdan.price}")
                intent.putExtra("PRODUCT_PHOTO", galdan.photo)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = listGaldan.size
}
