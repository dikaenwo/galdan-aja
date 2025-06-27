package com.example.galdanaja.ui.notif

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.galdanaja.adapter.NotificationAdapter
import com.example.galdanaja.databinding.FragmentNotifBinding
import com.example.galdanaja.helper.FirebaseHelper
import com.example.galdanaja.item.NotificationItem
import com.example.galdanaja.R
import com.google.firebase.firestore.Query

class NotifFragment : Fragment() {

    private var _binding: FragmentNotifBinding? = null
    private val binding get() = _binding!!

    private val notificationItems = mutableListOf<NotificationItem>()
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotifBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NotificationAdapter(notificationItems)
        binding.rvNotif.layoutManager = LinearLayoutManager(context)
        binding.rvNotif.adapter = adapter

        loadNotifications()
    }

    private fun loadNotifications() {
        val userId = FirebaseHelper.auth.currentUser?.uid ?: return

        FirebaseHelper.firestore
            .collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("time", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                notificationItems.clear()
                for (doc in documents) {
                    val title = doc.getString("title") ?: continue
                    val description = doc.getString("description") ?: ""
                    val timestamp = doc.getLong("time") ?: 0L
                    val timeAgo = getTimeAgo(timestamp)

                    val item = NotificationItem(
                        title = title,
                        description = description,
                        timeAgo = timeAgo
                    )
                    notificationItems.add(item)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal memuat notifikasi", Toast.LENGTH_SHORT).show()
                Log.e("NotifFragment", "Gagal ambil notifikasi", e)
            }
    }




    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val minutes = diff / 60000
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "Baru saja"
            minutes < 60 -> "$minutes menit lalu"
            hours < 24 -> "$hours jam lalu"
            else -> "$days hari lalu"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
