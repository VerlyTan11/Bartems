package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager

class BarterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.barter_page)

        // Handle click event for imageview back_barter
        val backBarter = findViewById<ImageView>(R.id.back_barter)
        backBarter.setOnClickListener {
            val intent = Intent(this@BarterActivity, DetailProductActivity::class.java)
            startActivity(intent)
        }

        // Handle click event for btn_confirm_barter
        val confirmButton = findViewById<Button>(R.id.btn_confirm_barter)
        confirmButton.setOnClickListener {
            // Show DoneFragment
            val fragment = DoneFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment) // Replace 'fragment_container' with the ID of your container
                .addToBackStack(null) // Add this transaction to the back stack
                .commit()
        }
    }
}
