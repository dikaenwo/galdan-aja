package com.example.galdanaja.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.galdanaja.CartItem
import com.example.galdanaja.R
import com.example.galdanaja.adapter.CartAdapter
import com.example.galdanaja.databinding.FragmentCartBinding

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Membuat data dummy
        val cartItems = listOf(
            CartItem("Salad Buah", "Rp.12000", R.drawable.saladbuah, 2),
            CartItem("Nasi Goreng", "Rp.15000", R.drawable.saladbuah, 1),
            CartItem("Es Teh", "Rp.5000", R.drawable.saladbuah, 3)
        )
        
        // Setup RecyclerView
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CartAdapter(cartItems)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}