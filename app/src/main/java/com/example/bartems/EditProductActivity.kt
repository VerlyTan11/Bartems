package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class EditProductActivity  : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_product)

        // Handle click event untuk button btn_confirm_edit
        val btnConfirmEdit = findViewById<Button>(R.id.btn_confirm_edit)
        btnConfirmEdit.setOnClickListener {
            val intent = Intent(this@EditProductActivity, EditDetailProductActivity::class.java)
            startActivity(intent)
        }

        // Handle click event untuk imageview back_detail_product
        val backDetailProduct = findViewById<ImageView>(R.id.back_detail_product)
        backDetailProduct.setOnClickListener {
            val intent = Intent(this@EditProductActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}