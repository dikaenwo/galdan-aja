package com.example.galdanaja.ui.home

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.galdanaja.item.GaldanItem
import com.example.galdanaja.R
import com.example.galdanaja.adapter.GaldanAdapter
import com.example.galdanaja.databinding.FragmentHomeBinding
import com.example.galdanaja.style.HorizontalMarginItemDecoration
import com.example.galdanaja.ui.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var galdanAdapter: GaldanAdapter
    private val productsList = ArrayList<GaldanItem>()
    private var currentCategory: String? = null

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
        setupRecyclerView()
        setupCategoryButtons()
        loadProductsFromFirebase(null) // Load semua produk awalnya
        
        // Tambahkan click listener untuk foto profil
        binding.imgUserPhoto.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }
        
        // Setup search functionality
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchProducts(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    loadProductsFromFirebase(currentCategory)
                }
                return true
            }
        })
    }
    
    private fun setupRecyclerView() {
        galdanAdapter = GaldanAdapter(requireContext(), productsList)
        binding.rvGaldan.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvGaldan.adapter = galdanAdapter
        val spacingInPx = (12 * Resources.getSystem().displayMetrics.density).toInt()
        binding.rvGaldan.addItemDecoration(HorizontalMarginItemDecoration(spacingInPx))
    }
    
    private fun setupCategoryButtons() {
        binding.btnCtgAll.setOnClickListener {
            binding.btnCtgAll.isEnabled = false
            binding.btnCtgMkn.isEnabled = true
            binding.btnCtgMnm.isEnabled = true
            currentCategory = null
            loadProductsFromFirebase(null)
        }
        
        binding.btnCtgMkn.setOnClickListener {
            binding.btnCtgAll.isEnabled = true
            binding.btnCtgMkn.isEnabled = false
            binding.btnCtgMnm.isEnabled = true
            currentCategory = "Makanan"
            loadProductsFromFirebase("Makanan")
        }
        
        binding.btnCtgMnm.setOnClickListener {
            binding.btnCtgAll.isEnabled = true
            binding.btnCtgMkn.isEnabled = true
            binding.btnCtgMnm.isEnabled = false
            currentCategory = "Minuman"
            loadProductsFromFirebase("Minuman")
        }
    }

    private fun loadProductsFromFirebase(category: String?) {
        val db = FirebaseFirestore.getInstance()
        val productsRef = db.collection("products")
        
        // Buat query dasar
        var query: Query = productsRef.orderBy("createdAt", Query.Direction.DESCENDING)
        
        // Filter berdasarkan kategori jika ada
        if (!category.isNullOrEmpty()) {
            query = query.whereEqualTo("category", category)
        }
        
        query.get().addOnSuccessListener { documents ->
            val newProductsList = ArrayList<GaldanItem>()
            
            if (documents.isEmpty) {
                // Tidak ada produk
                galdanAdapter.updateData(newProductsList)
                return@addOnSuccessListener
            }
            
            // Hitung jumlah dokumen untuk tracking
            val totalDocuments = documents.size()
            var processedDocuments = 0
            
            for (document in documents) {
                val productId = document.id
                val name = document.getString("name") ?: ""
                val price = document.getLong("price")?.toString() ?: "0"
                val imageUrl = document.getString("imageUrl") ?: ""
                val category = document.getString("category") ?: ""
                val description = document.getString("description") ?: ""
                val date = document.getString("date") ?: ""
                val userId = document.getString("userId") ?: ""
                
                // Dapatkan informasi user
                if (userId.isNotEmpty()) {
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            val userName = userDoc.getString("name") ?: "User"
                            val userPhotoUrl = userDoc.getString("profileImage") ?: ""
                            
                            val product = GaldanItem(
                                id = productId,
                                name = name,
                                price = price,
                                imageUrl = imageUrl,
                                category = category,
                                description = description,
                                date = date,
                                userId = userId,
                                userName = userName,
                                userPhotoUrl = userPhotoUrl
                            )
                            
                            newProductsList.add(product)
                            
                            // Update adapter ketika semua dokumen telah diproses
                            processedDocuments++
                            if (processedDocuments == totalDocuments) {
                                galdanAdapter.updateData(newProductsList)
                            }
                        }
                        .addOnFailureListener {
                            // Jika gagal mendapatkan info user, tetap tambahkan produk
                            val product = GaldanItem(
                                id = productId,
                                name = name,
                                price = price,
                                imageUrl = imageUrl,
                                category = category,
                                description = description,
                                date = date,
                                userId = userId
                            )
                            
                            newProductsList.add(product)
                            
                            // Update adapter ketika semua dokumen telah diproses
                            processedDocuments++
                            if (processedDocuments == totalDocuments) {
                                galdanAdapter.updateData(newProductsList)
                            }
                        }
                } else {
                    // Jika tidak ada userId, tambahkan produk tanpa info user
                    val product = GaldanItem(
                        id = productId,
                        name = name,
                        price = price,
                        imageUrl = imageUrl,
                        category = category,
                        description = description,
                        date = date
                    )
                    
                    newProductsList.add(product)
                    
                    // Update adapter ketika semua dokumen telah diproses
                    processedDocuments++
                    if (processedDocuments == totalDocuments) {
                        galdanAdapter.updateData(newProductsList)
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Error loading products: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun searchProducts(query: String) {
        val db = FirebaseFirestore.getInstance()
        val productsRef = db.collection("products")
        
        // Gunakan whereGreaterThanOrEqualTo dan whereLessThanOrEqualTo untuk mencari nama produk
        // yang dimulai dengan query (ini adalah cara sederhana untuk mencari, tidak sempurna)
        productsRef
            .orderBy("name")
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                val searchResults = ArrayList<GaldanItem>()
                
                if (documents.isEmpty) {
                    galdanAdapter.updateData(searchResults)
                    return@addOnSuccessListener
                }
                
                val totalDocuments = documents.size()
                var processedDocuments = 0
                
                for (document in documents) {
                    val productId = document.id
                    val name = document.getString("name") ?: ""
                    val price = document.getLong("price")?.toString() ?: "0"
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val category = document.getString("category") ?: ""
                    val description = document.getString("description") ?: ""
                    val date = document.getString("date") ?: ""
                    val userId = document.getString("userId") ?: ""
                    
                    // Filter berdasarkan kategori jika ada
                    if (currentCategory != null && category != currentCategory) {
                        processedDocuments++
                        if (processedDocuments == totalDocuments && searchResults.isNotEmpty()) {
                            galdanAdapter.updateData(searchResults)
                        }
                        continue
                    }
                    
                    // Dapatkan informasi user
                    if (userId.isNotEmpty()) {
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { userDoc ->
                                val userName = userDoc.getString("name") ?: "User"
                                val userPhotoUrl = userDoc.getString("profileImage") ?: ""
                                
                                val product = GaldanItem(
                                    id = productId,
                                    name = name,
                                    price = price,
                                    imageUrl = imageUrl,
                                    category = category,
                                    description = description,
                                    date = date,
                                    userId = userId,
                                    userName = userName,
                                    userPhotoUrl = userPhotoUrl
                                )
                                
                                searchResults.add(product)
                                
                                processedDocuments++
                                if (processedDocuments == totalDocuments) {
                                    galdanAdapter.updateData(searchResults)
                                }
                            }
                            .addOnFailureListener {
                                val product = GaldanItem(
                                    id = productId,
                                    name = name,
                                    price = price,
                                    imageUrl = imageUrl,
                                    category = category,
                                    description = description,
                                    date = date,
                                    userId = userId
                                )
                                
                                searchResults.add(product)
                                
                                processedDocuments++
                                if (processedDocuments == totalDocuments) {
                                    galdanAdapter.updateData(searchResults)
                                }
                            }
                    } else {
                        val product = GaldanItem(
                            id = productId,
                            name = name,
                            price = price,
                            imageUrl = imageUrl,
                            category = category,
                            description = description,
                            date = date
                        )
                        
                        searchResults.add(product)
                        
                        processedDocuments++
                        if (processedDocuments == totalDocuments) {
                            galdanAdapter.updateData(searchResults)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error searching products: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
    
        if (currentUser != null) {
            // Cek apakah user login dengan Google
            var isGoogleUser = false
            for (profile in currentUser.providerData) {
                if (profile.providerId == GoogleAuthProvider.PROVIDER_ID) {
                    isGoogleUser = true
                    break
                }
            }
    
            // Selalu cek Firestore terlebih dahulu
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Data ada di Firestore, gunakan data ini
                        val name = document.getString("name")
                        val profileImageUrl = document.getString("profileImage")
    
                        binding.tvName.text = "Hi ${name ?: "User"}"
    
                        Glide.with(requireContext())
                            .load(profileImageUrl)
                            .placeholder(R.drawable.avatars)
                            .error(R.drawable.avatars)
                            .into(binding.imgUserPhoto)
                    } else if (isGoogleUser) {
                        // Data tidak ada di Firestore, tapi user login dengan Google
                        // Gunakan data dari Google Auth
                        val name = currentUser.displayName
                        val profileImageUrl = currentUser.photoUrl?.toString()
    
                        binding.tvName.text = "Hi ${name ?: "User"}"
    
                        if (profileImageUrl != null) {
                            Glide.with(requireContext())
                                .load(profileImageUrl)
                                .placeholder(R.drawable.avatars)
                                .error(R.drawable.avatars)
                                .into(binding.imgUserPhoto)
                        }
                    } else {
                        // Fallback untuk user non-Google tanpa data Firestore
                        val fallbackName = currentUser.displayName ?: "User"
                        binding.tvName.text = "Hi $fallbackName"
    
                        if (currentUser.photoUrl != null) {
                            Glide.with(requireContext())
                                .load(currentUser.photoUrl)
                                .placeholder(R.drawable.avatars)
                                .error(R.drawable.avatars)
                                .into(binding.imgUserPhoto)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failure - tampilkan data dari Auth sebagai fallback
                    val fallbackName = currentUser.displayName ?: "User"
                    binding.tvName.text = "Hi $fallbackName"
    
                    if (currentUser.photoUrl != null) {
                        Glide.with(requireContext())
                            .load(currentUser.photoUrl)
                            .placeholder(R.drawable.avatars)
                            .error(R.drawable.avatars)
                            .into(binding.imgUserPhoto)
                    }
    
                    Toast.makeText(context, "Error loading profile data", Toast.LENGTH_SHORT).show()
                }
        } else {
            // User tidak login atau null
            binding.tvName.text = "Hi User"
            binding.imgUserPhoto.setImageResource(R.drawable.avatars)
        }
    }

    override fun onResume() {
        super.onResume()
        // Muat ulang data profil dan produk setiap kali fragment menjadi visible
        loadUserProfile()
        loadProductsFromFirebase(currentCategory)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}