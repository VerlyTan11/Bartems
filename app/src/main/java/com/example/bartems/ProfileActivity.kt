package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var namaTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var noHpTextView: TextView
    private lateinit var imageProfileView: ImageView // Menyimpan referensi untuk ImageView profil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        // Inisialisasi FirebaseAuth dan Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi TextViews dan ImageView untuk menampilkan data pengguna
        namaTextView = findViewById(R.id.textView5)
        emailTextView = findViewById(R.id.textView9)
        noHpTextView = findViewById(R.id.textView8)
        imageProfileView = findViewById(R.id.image_profile) // Inisialisasi ImageView profil

        // Ambil data pengguna dari Firestore
        getUserData()

        // Handle click event for back_profile ImageView
        val backProfile = findViewById<ImageView>(R.id.back_profile)
        backProfile.setOnClickListener {
            val intent = Intent(this@ProfileActivity, HomeActivity::class.java)
            intent.putExtra("USER_NAME", namaTextView.text.toString())
            startActivity(intent)
        }

        // Handle click event for gotoedit ImageView
        val goToEdit = findViewById<ImageView>(R.id.gotoedit)
        goToEdit.setOnClickListener {
            val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java)
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE)
        }

        // Handle click event for add_item ImageView
        val addItem = findViewById<ImageView>(R.id.add_item)
        addItem.setOnClickListener {
            val intent = Intent(this@ProfileActivity, PostItemActivity::class.java)
            intent.putExtra("isFromProfile", true)
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

    // Method to get user data from Firestore
    private fun getUserData() {
        val userId = auth.currentUser?.uid
        val userEmail = auth.currentUser?.email // Ambil email dari FirebaseAuth
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Ambil data dari dokumen Firestore
                        val name = document.getString("name") ?: "Nama tidak tersedia"
                        val noHp = document.getString("phone") ?: "Nomor HP tidak tersedia"
                        val imageUrl = document.getString("imageUrl")

                        // Tampilkan data di TextView
                        namaTextView.text = name
                        emailTextView.text = userEmail // Tampilkan email dari FirebaseAuth
                        noHpTextView.text = noHp

                        // Tampilkan gambar profil
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.box)
                            .into(imageProfileView)
                    } else {
                        Toast.makeText(this, "Dokumen tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal mendapatkan data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
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
                    logoutUser()
                    true
                }
                R.id.action_delete_account -> {
                    deleteUserAccount()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish() // Menutup ProfileActivity
    }

    private fun deleteUserAccount() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).delete()
                .addOnSuccessListener {
                    auth.currentUser?.delete()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, RegisterActivity::class.java))
                            finish() // Menutup ProfileActivity
                        } else {
                            Toast.makeText(this, "Gagal menghapus akun: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal menghapus data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_PROFILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refresh user data after returning from EditProfileActivity
                getUserData()
            }
        }
    }

    companion object {
        private const val EDIT_PROFILE_REQUEST_CODE = 1001
    }
}