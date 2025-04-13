package com.example.galdanaja

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class myaccount_penjualActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.myaccount_penjual)

        // Inisialisasi Spinner dan Adapter
        val spinner: Spinner = findViewById(R.id.spinner)
        val addresses = arrayOf("Address - Home", "Address - Kampus 1", "Address - Kampus 2")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, addresses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Menampilkan Toast saat item dipilih
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val selected = parent.getItemAtPosition(position).toString()
                Toast.makeText(this@myaccount_penjualActivity, selected, Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }
}
