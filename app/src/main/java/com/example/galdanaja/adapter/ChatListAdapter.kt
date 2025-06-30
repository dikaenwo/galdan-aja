package com.example.galdanaja.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.galdanaja.R
import com.example.galdanaja.data.Chat
import com.example.galdanaja.data.User
import com.example.galdanaja.ui.chat.ChatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale

class ChatListAdapter(private val chatList: List<Chat>) :
    RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    private val db = Firebase.firestore
    private val currentUserId = Firebase.auth.currentUser?.uid

    // ViewHolder untuk menampung view dari item_chat_list.xml
    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.imageViewProfile)
        val userName: TextView = itemView.findViewById(R.id.textViewUserName)
        val lastMessage: TextView = itemView.findViewById(R.id.textViewLastMessage)
        val timestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)
        val cardUnread: CardView = itemView.findViewById(R.id.cardUnread)
        val unreadCountText: TextView = itemView.findViewById(R.id.textViewUnreadCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_list, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int = chatList.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]

        // 1. Tampilkan data chat yang sudah ada (pesan terakhir & waktu)
        holder.lastMessage.text = chat.lastMessage
        holder.timestamp.text = formatTimestamp(chat.timestamp)

        // 2. Cari ID lawan bicara
        val otherUserId = chat.participants.find { it != currentUserId }
        if (otherUserId == null) {
            // Jika karena suatu alasan tidak ditemukan, jangan lanjutkan
            holder.userName.text = "User tidak dikenal"
            return
        }

        // --- LOGIKA BARU UNTUK UNREAD COUNT ---
        val currentUserId = Firebase.auth.currentUser?.uid
        if (currentUserId != null) {
            // Ambil jumlah pesan belum dibaca untuk user saat ini
            val count = chat.unreadCount[currentUserId] ?: 0L

            if (count > 0) {
                // Jika ada pesan belum dibaca, tampilkan badge & set angkanya
                holder.cardUnread.visibility = View.VISIBLE
                holder.unreadCountText.text = count.toString()
            } else {
                // Jika tidak ada, sembunyikan badge
                holder.cardUnread.visibility = View.GONE
            }
        } else {
            // Sembunyikan jika user tidak login
            holder.cardUnread.visibility = View.GONE
        }
        // 3. Ambil data lawan bicara dari koleksi 'users'
        // Versi PERBAIKAN di onBindViewHolder dalam ChatListAdapter.kt
        db.collection("users").document(otherUserId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val otherUser = document.toObject(User::class.java)

                    holder.userName.text = otherUser?.name

                    // UBAH BARIS INI
                    Glide.with(holder.itemView.context)
                        .load(otherUser?.profileImage) // <-- Gunakan nama field yang sudah benar
                        .placeholder(R.drawable.nunu)
                        .error(R.drawable.nunu)
                        .into(holder.profileImage)

                    // Pastikan Anda juga mengirim nama field yang benar ke ChatActivity
                    holder.itemView.setOnClickListener {
                        val context = holder.itemView.context
                        val intent = Intent(context, ChatActivity::class.java).apply {
                            putExtra("CHAT_ID", chat.id)
                            putExtra("OTHER_USER_ID", otherUserId)
                            putExtra("OTHER_USER_NAME", otherUser?.name)
                            // Kirim juga URL gambar dengan key yang konsisten
                            putExtra("OTHER_USER_IMAGE_URL", otherUser?.profileImage)
                        }
                        context.startActivity(intent)
                    }
                }
            }


    }

    // Helper function untuk format waktu
    private fun formatTimestamp(date: java.util.Date?): String {
        if (date == null) return ""
        // Format sederhana, bisa dikembangkan lebih lanjut (misal: "Kemarin", "18:30")
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }
}