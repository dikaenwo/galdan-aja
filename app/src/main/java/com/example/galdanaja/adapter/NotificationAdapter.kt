package com.example.galdanaja.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.galdanaja.item.NotificationItem
import com.example.galdanaja.databinding.ItemRowPushNotificationBinding

class NotificationAdapter(private val items: List<NotificationItem>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(val binding: ItemRowPushNotificationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemRowPushNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvPnTitle.text = item.title
            tvPnDesc.text = item.description
            textView16.text = item.timeAgo
            logoPn.setImageResource(item.logoResId)
        }
    }

    override fun getItemCount(): Int = items.size
}