package com.example.galdanaja.ui.notif

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.galdanaja.item.NotificationItem
import com.example.galdanaja.adapter.NotificationAdapter
import com.example.galdanaja.databinding.FragmentNotifBinding

class NotifFragment : Fragment() {
    private var _binding: FragmentNotifBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotifBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Membuat data dummy untuk notifikasi
        val notificationItems = listOf(
            NotificationItem(
                "Order out of Delivery!",
                "Your food is on the move! Track your order for real-time updates.",
                "5 min ago"
            ),
            NotificationItem(
                "Order Confirmed!",
                "Great news! Your order has been confirmed and is being prepared.",
                "15 min ago"
            ),
            NotificationItem(
                "Payment Successful!",
                "Your payment has been processed successfully. Thank you for your order!",
                "30 min ago"
            ),
            NotificationItem(
                "Special Offer!",
                "Get 20% off on your next order. Use code: GALDAN20",
                "1 hour ago"
            )
        )
        
        // Setup RecyclerView
        binding.rvNotif.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = NotificationAdapter(notificationItems)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}