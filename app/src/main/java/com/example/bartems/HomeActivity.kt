package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Handle click event untuk ImageButton gotoprofile
        val goToProfileButton = findViewById<ImageButton>(R.id.gotoprofile)
        goToProfileButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Handle click event untuk ImageButton cth_produk
        val cthProduct = findViewById<ImageButton>(R.id.cth_produk)
        cthProduct.setOnClickListener {
            val intent = Intent(this@HomeActivity, DetailProductActivity::class.java)
            startActivity(intent)
        }
    }
}