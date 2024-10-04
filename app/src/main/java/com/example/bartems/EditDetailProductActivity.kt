package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class EditDetailProductActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_detail_product)

        // Handle click event untuk imageview back_edit_product
        val backEditProduct = findViewById<ImageView>(R.id.back_edit_product)
        backEditProduct.setOnClickListener {
            val intent = Intent(this@EditDetailProductActivity, EditProductActivity::class.java)
            startActivity(intent)
        }

        //tambahin fragment untuk maps

        //tambahin fragment untuk berhasil posting
    }
}