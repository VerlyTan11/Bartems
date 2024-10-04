package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class PostItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_item)

        // Handle click event untuk imageview back_post_item
        val backPostItem = findViewById<ImageView>(R.id.back_post_item)
        backPostItem.setOnClickListener {
            val intent = Intent(this@PostItemActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        //tambahin fragment untuk maps

        //tambahin fragment untuk berhasil posting
    }
}