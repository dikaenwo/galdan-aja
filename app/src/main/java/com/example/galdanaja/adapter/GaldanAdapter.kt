package com.example.galdanaja.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.galdanaja.GaldanItem
import com.example.galdanaja.databinding.ItemRowDaftarGaldanBinding

class GaldanAdapter(private val items: List<GaldanItem>) :
    RecyclerView.Adapter<GaldanAdapter.GaldanViewHolder>() {

    inner class GaldanViewHolder(val binding: ItemRowDaftarGaldanBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GaldanViewHolder {
        val binding = ItemRowDaftarGaldanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GaldanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GaldanViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvItemName.text = item.name
            textView5.text = item.price
            imgItemPhoto.setImageResource(item.imageResId)
            imgUserPhoto.setImageResource(item.userPhotoResId)
            textView6.text = item.userName
        }
    }

    override fun getItemCount(): Int = items.size
}
