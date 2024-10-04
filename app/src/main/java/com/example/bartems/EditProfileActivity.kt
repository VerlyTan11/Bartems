package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile)

        // Handle click event untuk imageview back_edit_profile
        val backEditProfile = findViewById<ImageView>(R.id.back_edit_profile)
        backEditProfile.setOnClickListener {
            val intent = Intent(this@EditProfileActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        //buat fragment buat berhasil simpan : btn_simpan_user
    }
}