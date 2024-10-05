package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuCompat

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        // Handle click event for back_profile ImageView
        val backProfile = findViewById<ImageView>(R.id.back_profile)
        backProfile.setOnClickListener {
            val intent = Intent(this@ProfileActivity, HomeActivity::class.java)
            startActivity(intent)
        }

        // Handle click event for gotoedit ImageView
        val goToEdit = findViewById<ImageView>(R.id.gotoedit)
        goToEdit.setOnClickListener {
            val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Handle click event for add_item ImageView
        val addItem = findViewById<ImageView>(R.id.add_item)
        addItem.setOnClickListener {
            val intent = Intent(this@ProfileActivity, PostItemActivity::class.java)
            intent.putExtra("isFromProfile", true) // Pass extra indicating it came from Profile
            startActivity(intent)
        }

        // Handle click event for cth_edit_product ImageView
        val editProduct = findViewById<ImageView>(R.id.cth_edit_product)
        editProduct.setOnClickListener {
            val intent = Intent(this@ProfileActivity, EditProductActivity::class.java)
            startActivity(intent)
        }

        // Handle click event for menu ImageView (three dots)
        val menu = findViewById<ImageView>(R.id.menu)
        menu.setOnClickListener { view ->
            showPopupMenu(view)
        }
    }

    // Method to show popup menu
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)

        // Force icons to be shown in the PopupMenu
        MenuCompat.setGroupDividerEnabled(popupMenu.menu, true)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    // Handle logout action
                    Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_delete_account -> {
                    // Handle delete account action
                    Toast.makeText(this, "Delete Account clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}
