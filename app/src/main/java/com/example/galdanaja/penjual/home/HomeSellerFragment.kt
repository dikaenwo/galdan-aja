package com.example.galdanaja.penjual.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.galdanaja.R
import com.example.galdanaja.adapter.GaldanAdapter // <-- Gunakan adapter yang sama
import com.example.galdanaja.databinding.FragmentHomeSellerBinding
import com.example.galdanaja.helper.FirebaseHelper
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
    private var totalEarnings: Long = 0L // Variabel baru untuk validasi


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
        loadCurrentSellerInfo() // Ini untuk memuat daftar produk di bawah

        // PANGGIL FUNGSI INI UNTUK MENG-UPDATE KARTU DASHBOARD
        loadSellerDashboardData()

        binding.btnWithdraw.setOnClickListener {
            showWithdrawDialog()
        }
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

    // Di dalam class HomeSellerFragment.kt

    // Di dalam class HomeSellerFragment.kt

    private fun loadSellerDashboardData() {
        val currentUserId = FirebaseHelper.auth.currentUser?.uid ?: return
        val db = FirebaseHelper.firestore

        // --- 1. MENGHITUNG SALDO AKHIR DARI SEMUA TRANSAKSI (PEMASUKAN & PENGELUARAN) ---
        db.collection("users").document(currentUserId)
            .collection("transactions") // <-- Ambil SEMUA transaksi
            .get()
            .addOnSuccessListener { transactionDocuments ->
                var currentBalance = 0L // Mulai dari 0
                for (doc in transactionDocuments) {
                    // Langsung jumlahkan. Pemasukan (credit) akan menambah,
                    // pengeluaran (debit) akan mengurangi karena nilainya negatif.
                    currentBalance += doc.getLong("amount") ?: 0L
                }

                // Simpan saldo saat ini untuk validasi penarikan
                this.totalEarnings = currentBalance

                // Update UI Saldo (Penghasilan)
                val formattedBalance = "Rp ${String.format("%,d", currentBalance).replace(',', '.')}"
                binding.tvEarningsAmount.text = formattedBalance
            }
            .addOnFailureListener {
                binding.tvEarningsAmount.text = "Rp 0"
                Log.e("HomeSellerFragment", "Gagal mengambil data transaksi.", it)
            }

        // --- 2. MENGHITUNG PRODUK TERJUAL (logika ini tetap sama) ---
        db.collection("orders")
            .whereEqualTo("sellerId", currentUserId)
            .whereEqualTo("status", "completed")
            .get()
            .addOnSuccessListener { orderDocuments ->
                var totalItemsSold = 0L
                for (doc in orderDocuments) {
                    val items = doc.get("items") as? List<Map<String, Any>>
                    items?.forEach { item ->
                        totalItemsSold += item["quantity"] as? Long ?: 0L
                    }
                }
                binding.tvSoldCount.text = totalItemsSold.toString()
            }
            .addOnFailureListener {
                binding.tvSoldCount.text = "0"
            }

        // --- 3. MENGHITUNG TOTAL PRODUK (logika ini tetap sama) ---
        db.collection("products")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { productDocuments ->
                binding.tvTotalProductsCount.text = productDocuments.size().toString()
            }
            .addOnFailureListener {
                binding.tvTotalProductsCount.text = "0"
            }
    }


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

    // Tambahkan fungsi ini di dalam HomeSellerFragment.kt
    private fun showWithdrawDialog() {
        // Inflate layout kustom untuk dialog
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_withdraw, null)

        // Inisialisasi view di dalam dialog
        val spinnerBanks = dialogView.findViewById<android.widget.Spinner>(R.id.spinner_banks)
        val etAccountNumber = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_account_number)
        val etAmount = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_amount)

        // Siapkan daftar bank untuk Spinner
        val banks = arrayOf("BCA", "Mandiri", "BNI", "BRI", "CIMB Niaga", "Lainnya")
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, banks)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBanks.adapter = adapter

        // Buat dan tampilkan AlertDialog
        AlertDialog.Builder(requireContext())
            .setTitle("Ajukan Penarikan Dana")
            .setView(dialogView)
            .setPositiveButton("Ajukan") { dialog, _ ->
                val selectedBank = spinnerBanks.selectedItem.toString()
                val accountNumber = etAccountNumber.text.toString().trim()
                val amountString = etAmount.text.toString().trim()

                // Validasi input
                if (accountNumber.isEmpty() || amountString.isEmpty()) {
                    Toast.makeText(requireContext(), "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val amountToWithdraw = amountString.toLong()
                if (amountToWithdraw <= 0) {
                    Toast.makeText(requireContext(), "Nominal harus lebih dari 0", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Validasi saldo
                if (amountToWithdraw > totalEarnings) {
                    Toast.makeText(requireContext(), "Saldo tidak mencukupi untuk penarikan!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Jika semua valid, proses permintaan
                processWithdrawalRequest(selectedBank, accountNumber, amountToWithdraw)
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // Tambahkan fungsi ini di dalam HomeSellerFragment.kt
    private fun processWithdrawalRequest(bankName: String, accountNumber: String, amount: Long) {
        val currentUser = FirebaseHelper.auth.currentUser ?: return
        val db = FirebaseHelper.firestore

        // Data yang akan disimpan di Firestore
        val requestData = hashMapOf(
            "userId" to currentUser.uid,
            "userName" to (currentUser.displayName ?: "Tanpa Nama"),
            "amount" to amount,
            "bankName" to bankName,
            "accountNumber" to accountNumber,
            "status" to "pending", // Status awal: menunggu diproses admin
            "requestedAt" to System.currentTimeMillis()
        )

        // Simpan ke koleksi 'withdrawalRequests'
        db.collection("withdrawalRequests")
            .add(requestData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Permintaan penarikan berhasil diajukan.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal mengajukan permintaan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ... (kode lainnya)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}