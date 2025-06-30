package com.example.galdanaja.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.galdanaja.R
import com.example.galdanaja.adapter.ChatListAdapter
import com.example.galdanaja.data.Chat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatListAdapter
    private val chatList = mutableListOf<Chat>()

    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout untuk fragment ini
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewChats) // Ganti dengan ID RecyclerView Anda
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inisialisasi Adapter dengan list kosong
        chatAdapter = ChatListAdapter(chatList)
        recyclerView.adapter = chatAdapter

        // Mulai mendengarkan update dari Firestore
        listenForChatUpdates()
    }

    private fun listenForChatUpdates() {
        val currentUserId = Firebase.auth.currentUser?.uid
        if (currentUserId == null) {
            Log.w("ChatFragment", "User tidak login, tidak bisa memuat chat.")
            // Mungkin tampilkan pesan "Silakan login terlebih dahulu"
            return
        }

        // Query untuk mendapatkan semua chat yang melibatkan user saat ini,
        // diurutkan berdasarkan pesan terbaru.
        db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ChatFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val newChats = mutableListOf<Chat>()
                for (doc in snapshots!!) {
                    val chat = doc.toObject(Chat::class.java)
                    newChats.add(chat)
                }

                // Update list dan beritahu adapter
                chatList.clear()
                chatList.addAll(newChats)
                chatAdapter.notifyDataSetChanged()
            }
    }
}