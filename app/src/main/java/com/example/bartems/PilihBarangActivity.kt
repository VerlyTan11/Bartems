package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class PilihBarangActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_barang) // Ganti dengan nama layout Anda

        // Mendapatkan referensi ke ImageView back
        val backButton = findViewById<ImageView>(R.id.back)

        // Menambahkan OnClickListener
        backButton.setOnClickListener {
            // Menggunakan Intent untuk kembali ke OtherDetailProductActivity
            val intent = Intent(
                this@PilihBarangActivity,
                OtherDetailProductActivity::class.java
            )
            startActivity(intent)
            finish() // Menutup PilihBarangActivity jika tidak ingin kembali ke activity ini
        }
    }
}