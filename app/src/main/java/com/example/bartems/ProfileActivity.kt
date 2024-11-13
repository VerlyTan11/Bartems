package com.example.bartems

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bartems.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var namaTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var noHpTextView: TextView
    private lateinit var imageProfileView: ImageView
    private lateinit var recyclerView: RecyclerView // RecyclerView untuk produk
    private lateinit var addButton: ImageView // Tombol tambah item
    private lateinit var gotoEditButton: ImageView // Tombol untuk mengedit profil
    private lateinit var backButton: ImageView // Tombol back
    private lateinit var menuButton: ImageView // Tombol menu titik tiga

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
        imageProfileView = findViewById(R.id.image_profile)
        recyclerView = findViewById(R.id.recyclerView_product_images)
        addButton = findViewById(R.id.add_item)
        gotoEditButton = findViewById(R.id.gotoedit)
        backButton = findViewById(R.id.back_profile)  // Tombol back
        menuButton = findViewById(R.id.menu)  // Tombol menu titik tiga

        // Atur RecyclerView dengan GridLayoutManager untuk 2 kolom
        val gridLayoutManager = GridLayoutManager(this, 2) // 2 kolom
        recyclerView.layoutManager = gridLayoutManager

        // Ambil data pengguna dari Firestore
        getUserData()

        // Load semua gambar produk pengguna
        loadUserProducts()

        // Tambahkan fungsi klik untuk tombol tambah item
        addButton.setOnClickListener {
            val intent = Intent(this, PostItemActivity::class.java)
            intent.putExtra("isFromProfile", true)
            startActivity(intent)
        }

        // Tambahkan fungsi klik untuk tombol edit profil
        gotoEditButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Fungsi untuk tombol back
        backButton.setOnClickListener {
            // Kembali ke HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()  // Menutup ProfileActivity
        }

        // Fungsi untuk tombol menu titik tiga (Logout / Delete Account)
        menuButton.setOnClickListener {
            showMenuDialog()
        }
    }

    // Fungsi untuk menampilkan dialog Logout atau Delete Account
    private fun showMenuDialog() {
        val options = arrayOf("Logout", "Delete Account")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih Opsi")
        builder.setItems(options) { dialog: DialogInterface, which: Int ->
            when (which) {
                0 -> logoutUser()    // Logout
                1 -> deleteUserAccount()   // Delete Account
            }
        }
        builder.show()
    }

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(this, "Anda telah logout", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun deleteUserAccount() {
        val user = auth.currentUser
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Gagal menghapus akun: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserProducts() {
        val userId = auth.currentUser?.uid
        val productList = mutableListOf<Product>()
        if (userId != null) {
            firestore.collection("items")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { snapshots ->
                    if (snapshots != null) {
                        productList.clear()
                        for (document in snapshots) {
                            // Mengambil data produk dari Firestore dan menetapkan ID produk
                            val product = document.toObject(Product::class.java)

                            // Menetapkan ID produk yang diambil dari Firestore ke objek Product
                            product?.id = document.id  // Menetapkan ID dari dokumen Firestore ke objek Product

                            // Log untuk memverifikasi ID produk
                            Log.d("ProfileActivity", "Product ID from Firestore: ${product?.id}")

                            // Pastikan ID produk tidak kosong
                            if (product?.id?.isNotEmpty() == true) {
                                productList.add(product)
                            } else {
                                Log.e("ProfileActivity", "Product ID is empty for product: ${document.id}")
                            }
                        }
                        if (productList.isNotEmpty()) {
                            // Menambahkan produk ke RecyclerView
                            val adapter = ProductImageAdapter(this, productList)
                            recyclerView.adapter = adapter
                        } else {
                            Toast.makeText(this, "Tidak ada produk untuk ditampilkan", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal mengambil data produk: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUserData() {
        val userId = auth.currentUser?.uid
        val userEmail = auth.currentUser?.email
        if (userId != null) {
            firestore.collection("users").document(userId)
                .addSnapshotListener { documentSnapshot, e ->
                    if (e != null) {
                        Toast.makeText(this, "Gagal mendengarkan perubahan: ${e.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val name = documentSnapshot.getString("name") ?: "Nama tidak tersedia"
                        val noHp = documentSnapshot.getString("phone") ?: "Nomor HP tidak tersedia"
                        val imageUrl = documentSnapshot.getString("imageUrl")

                        namaTextView.text = name
                        emailTextView.text = userEmail
                        noHpTextView.text = noHp

                        Glide.with(this)
                            .load(imageUrl ?: R.drawable.default_profile_image)
                            .placeholder(R.drawable.box)
                            .into(imageProfileView)
                    }
                }
        } else {
            Toast.makeText(this, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
        }
    }
}