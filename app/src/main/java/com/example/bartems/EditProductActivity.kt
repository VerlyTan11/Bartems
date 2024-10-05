package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class EditProductActivity : AppCompatActivity() {

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

        // Handle click event untuk imageview delete icon
        val deleteIcon = findViewById<ImageView>(R.id.sampah)
        deleteIcon.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { dialog, id ->
                // Action when "Yes" is clicked
                // Implement delete logic
                // For example, finish the activity or show a message
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, id ->
                // Action when "No" is clicked
                dialog.dismiss()
            }
        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }
}