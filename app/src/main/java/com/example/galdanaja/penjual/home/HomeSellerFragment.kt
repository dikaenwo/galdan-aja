package com.example.galdanaja.penjual.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.galdanaja.adapter.GaldanAdapter // <-- Gunakan adapter yang sama
import com.example.galdanaja.databinding.FragmentHomeSellerBinding
import com.example.galdanaja.item.GaldanItem // <-- Gunakan item yang sama
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeSellerFragment : Fragment() {

    private var _binding: FragmentHomeSellerBinding? = null
    private val binding get() = _binding!!

    private lateinit var galdanAdapter: GaldanAdapter
    private val galdanList = ArrayList<GaldanItem>()
    private val db = Firebase.firestore

    private var currentSellerName: String = "Penjual"
    private var currentSellerPhotoUrl: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeSellerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        //loadSellerProducts()
        loadCurrentSellerInfo()
        loadSellerDashboardData() // Fungsi untuk memuat data dashboard (opsional)
    }

    private fun setupRecyclerView() {
        // Inisialisasi adapter dengan list kosong
        galdanAdapter = GaldanAdapter(requireContext(), galdanList)
        binding.rvProducts.apply {
            // Layout manager sudah diatur di XML, tapi bisa juga di sini
            // layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = galdanAdapter
        }
    }

    private fun loadSellerDashboardData() {

        val totalPenghasilan = 70000 // contoh data dari firestore
        val jumlahTerjual = 15      // contoh data dari firestore
        val totalProduk = 5         // contoh data dari firestore

        // Update UI
        binding.tvEarningsAmount.text = "Rp ${totalPenghasilan}"
        binding.tvSoldCount.text = jumlahTerjual.toString()
        binding.tvTotalProductsCount.text = totalProduk.toString()
    }

    // Tambahkan fungsi ini di dalam HomeSellerFragment.kt

    // Di dalam HomeSellerFragment.kt

    private fun loadSellerProducts() {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return

        db.collection("products")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { result ->
                val newProductList = ArrayList<GaldanItem>()
                for (document in result) {
                    // Saat membuat objek GaldanItem...
                    val product = GaldanItem(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        price = document.get("price")?.toString() ?: "0",
                        stock = document.get("stock")?.toString() ?: "0",
                        imageUrl = document.getString("imageUrl") ?: "",
                        category = document.getString("category") ?: "",
                        description = document.getString("description") ?: "",
                        date = document.getString("date") ?: "",
                        userId = document.getString("userId") ?: "",

                        // ...GUNAKAN INFO PENJUAL YANG SUDAH KITA AMBIL
                        userName = currentSellerName,
                        userPhotoUrl = currentSellerPhotoUrl
                    )
                    newProductList.add(product)
                }
                galdanAdapter.updateData(newProductList)
            }
            .addOnFailureListener { exception ->
                Log.w("HomeSellerFragment", "Error getting documents.", exception)
            }
    }

    // Tambahkan fungsi ini di dalam HomeSellerFragment.kt

    private fun loadCurrentSellerInfo() {
        val currentUserId = Firebase.auth.currentUser?.uid
        if (currentUserId == null) {
            // Jika user tidak login, langsung coba muat produk tanpa info user
            loadSellerProducts()
            return
        }

        db.collection("users").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Simpan nama dan foto ke variabel yang sudah kita siapkan
                    currentSellerName = document.getString("name") ?: "Penjual"
                    currentSellerPhotoUrl = document.getString("profileImage") ?: ""
                } else {
                    Log.d("HomeSellerFragment", "Profil user tidak ditemukan")
                }
                // Setelah info user didapat, BARU kita muat produknya
                loadSellerProducts()
            }
            .addOnFailureListener { exception ->
                Log.w("HomeSellerFragment", "Gagal mengambil info user.", exception)
                // Walaupun gagal, tetap coba muat produknya
                loadSellerProducts()
            }
    }

    // ... (kode lainnya)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}