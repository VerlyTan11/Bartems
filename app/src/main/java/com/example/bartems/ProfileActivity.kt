package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        // Handle click event untuk imageview back_profile
        val backProfile = findViewById<ImageView>(R.id.back_profile)
        backProfile.setOnClickListener {
            val intent = Intent(this@ProfileActivity, HomeActivity::class.java)
            startActivity(intent)
        }

        // Handle click event untuk imageview go_to_edit profile
        val goToEdit = findViewById<ImageView>(R.id.gotoedit)
        goToEdit.setOnClickListener {
            val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Handle click event untuk imageview add_item
        val addItem = findViewById<ImageView>(R.id.add_item)
        addItem.setOnClickListener {
            val intent = Intent(this@ProfileActivity, PostItemActivity::class.java)
            startActivity(intent)
        }

        // Handle click event untuk imageview cth_edit_product
        val EditProduct = findViewById<ImageView>(R.id.cth_edit_product)
        EditProduct.setOnClickListener {
            val intent = Intent(this@ProfileActivity, EditProductActivity::class.java)
            startActivity(intent)
        }

        //tambahin fragment untuk click event menu
    }
}