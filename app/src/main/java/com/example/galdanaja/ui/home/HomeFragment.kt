package com.example.galdanaja.ui.home

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.galdanaja.GaldanItem
import com.example.galdanaja.R
import com.example.galdanaja.adapter.GaldanAdapter
import com.example.galdanaja.databinding.FragmentHomeBinding
import com.example.galdanaja.style.HorizontalMarginItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserProfile()

        val dummyList = listOf(
            GaldanItem("Salad Buah", "Rp. 3.000", R.drawable.saladbuah, R.drawable.avatars, "FamGath TMJ"),
            GaldanItem("Nasi Goreng", "Rp. 10.000", R.drawable.saladbuah, R.drawable.avatars, "TMJ Crew"),
            GaldanItem("Es Teh", "Rp. 2.000", R.drawable.saladbuah, R.drawable.avatars, "Dapur Bu RT"),
            GaldanItem("Mie Ayam", "Rp. 8.000", R.drawable.saladbuah, R.drawable.avatars, "Warga RW 5")
        )

        binding.rvGaldan.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvGaldan.adapter = GaldanAdapter(dummyList)
        val spacingInPx = (12 * Resources.getSystem().displayMetrics.density).toInt()
        binding.rvGaldan.addItemDecoration(HorizontalMarginItemDecoration(spacingInPx))
    }

    private fun loadUserProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        val profileImageUrl = document.getString("profileImage")

                        binding.tvName.text = "Hi ${name ?: "User"}"

                        Glide.with(requireContext())
                            .load(profileImageUrl)
                            .placeholder(R.drawable.avatars)
                            .error(R.drawable.avatars)
                            .into(binding.imgUserPhoto)
                    }
                }
                .addOnFailureListener {
                    // handle error
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
