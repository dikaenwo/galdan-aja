package com.example.galdanaja

import android.content.res.Resources
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.galdanaja.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.GridLayoutManager
import com.example.galdanaja.adapter.GaldanAdapter
import com.example.galdanaja.style.HorizontalMarginItemDecoration



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadUserProfile()

        val dummyList = listOf(
            GaldanItem("Salad Buah", "Rp. 3.000", R.drawable.saladbuah, R.drawable.avatars, "FamGath TMJ"),
            GaldanItem("Nasi Goreng", "Rp. 10.000", R.drawable.saladbuah, R.drawable.avatars, "TMJ Crew"),
            GaldanItem("Es Teh", "Rp. 2.000", R.drawable.saladbuah, R.drawable.avatars, "Dapur Bu RT"),
            GaldanItem("Mie Ayam", "Rp. 8.000", R.drawable.saladbuah, R.drawable.avatars, "Warga RW 5")
        )

        binding.rvGaldan.layoutManager = GridLayoutManager(this, 2)
        binding.rvGaldan.adapter = GaldanAdapter(dummyList)
        val spacingInPx = ( 12* Resources.getSystem().displayMetrics.density).toInt()
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

                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.avatars) // gambar default kalau loading
                            .error(R.drawable.avatars)       // gambar default kalau error
                            .into(binding.imgUserPhoto)
                    }
                }
                .addOnFailureListener {
                    // handle error kalau Firestore gagal
                }
        }
    }


}
