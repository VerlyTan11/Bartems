package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class BarterActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.barter_page)

        // Handle click event untuk imageview back_barter
        val backBarter = findViewById<ImageView>(R.id.back_barter)
        backBarter.setOnClickListener {
            val intent = Intent(this@BarterActivity, DetailProductActivity::class.java)
            startActivity(intent)
        }

        //tambahin buat munculin fragment berhasil kalau btn_confirm_barter ditekan
    }
}