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
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var namaTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var noHpTextView: TextView
    private lateinit var imageProfileView: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: ImageView
    private lateinit var gotoEditButton: ImageView
    private lateinit var backButton: ImageView
    private lateinit var menuButton: ImageView
    private lateinit var historyButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        namaTextView = findViewById(R.id.textView5)
        emailTextView = findViewById(R.id.textView9)
        noHpTextView = findViewById(R.id.textView8)
        imageProfileView = findViewById(R.id.image_profile)
        recyclerView = findViewById(R.id.recyclerView_product_images)
        addButton = findViewById(R.id.add_item)
        gotoEditButton = findViewById(R.id.gotoedit)
        backButton = findViewById(R.id.back_profile)
        menuButton = findViewById(R.id.menu)
        historyButton = findViewById(R.id.history)

        recyclerView.layoutManager = GridLayoutManager(this, 2)

        getUserData()
        loadUserProducts()

//        // Check if the address was passed from EditProfileActivity
//        val selectedAddress = intent.getStringExtra("selectedAddress")
//        val inputAddress = intent.getStringExtra("inputAddress")

        // Find the TextView to display the address
//        val alamatTextView = findViewById<TextView>(R.id.alamatTextView)

//        // Display the address if available
//        if (!selectedAddress.isNullOrEmpty()) {
//            alamatTextView.text = selectedAddress
//        } else if (!inputAddress.isNullOrEmpty()) {
//            alamatTextView.text = inputAddress
//        }

        addButton.setOnClickListener {
            val intent = Intent(this, PostItemActivity::class.java)
            intent.putExtra("isFromProfile", true)
            startActivity(intent)
        }

        gotoEditButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        menuButton.setOnClickListener {
            showMenuDialog()
        }

        historyButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val selectedAddress = data?.getStringExtra("selectedAddress")
            val inputAddress = data?.getStringExtra("inputAddress")

            // Find the alamatTextView and update its text
//            val alamatTextView = findViewById<TextView>(R.id.alamatTextView)

            // Display the address if available
//            if (!selectedAddress.isNullOrEmpty()) {
//                alamatTextView.text = selectedAddress
//            } else if (!inputAddress.isNullOrEmpty()) {
//                alamatTextView.text = inputAddress
//            } else {
//                // If no address is provided, show the default message
//                alamatTextView.text = "Lokasi belum diatur"
//            }
        }
    }



    private fun showMenuDialog() {
        val options = arrayOf("Logout", "Delete Account")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih Opsi")
        builder.setItems(options) { _: DialogInterface, which: Int ->
            when (which) {
                0 -> logoutUser()
                1 -> deleteUserAccount()
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
        CoroutineScope(Dispatchers.IO).launch {
            val userId = auth.currentUser?.uid
            val productList = mutableListOf<Product>()
            if (userId != null) {
                firestore.collection("items")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { snapshots ->
                        productList.clear()
                        for (document in snapshots.documents) {
                            val product = document.toObject(Product::class.java)?.apply {
                                id = document.id
                            }
                            if (product != null && !product.id.isNullOrEmpty()) {
                                productList.add(product)
                            }
                        }

                        // Sort products by timestamp in descending order
                        productList.sortByDescending { it.timestamp }

                        runOnUiThread {
                            // Ensure adapter is set after data is fetched
                            if (productList.isNotEmpty()) {
                                val adapter = ProductImageAdapter(this@ProfileActivity, productList)
                                recyclerView.adapter = adapter
                                adapter.notifyDataSetChanged()
                            } else {
                                Log.e("ProfileActivity", "No products found.")
                                Toast.makeText(this@ProfileActivity, "Tidak ada produk untuk ditampilkan", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        runOnUiThread {
                            Log.e("ProfileActivity", "Failed to load products: ${e.message}")
                            Toast.makeText(this@ProfileActivity, "Gagal mengambil data produk: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun getUserData() {
        val userId = auth.currentUser ?.uid
        val userEmail = auth.currentUser ?.email

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
                        val imageUrl = documentSnapshot.getString("imageUrl") // Get the updated image URL

                        namaTextView.text = name
                        emailTextView.text = userEmail
                        noHpTextView.text = noHp

                        // Load the image into the ImageView
                        Glide.with(this)
                            .load(imageUrl ?: R.drawable.default_profile_image) // Use a default image if imageUrl is null
                            .placeholder(R.drawable.default_profile_image)
                            .into(imageProfileView)
                    }
                }
        } else {
            Toast.makeText(this, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
        }
    }

}