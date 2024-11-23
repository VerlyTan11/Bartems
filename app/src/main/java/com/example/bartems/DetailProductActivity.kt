package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore

class DetailProductActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_product)

        firestore = FirebaseFirestore.getInstance()

        val productId = intent.getStringExtra("PRODUCT_ID")
        val productName = intent.getStringExtra("PRODUCT_NAME") ?: "Nama tidak tersedia"
        val productImageUrl = intent.getStringExtra("PRODUCT_IMAGE_URL") ?: ""

        Log.d("DetailProductActivity", "Intent received: PRODUCT_ID=$productId, PRODUCT_NAME=$productName, PRODUCT_IMAGE_URL=$productImageUrl")

        if (productId.isNullOrEmpty()) {
            Log.e("DetailProductActivity", "ERROR: PRODUCT_ID tidak ditemukan!")
            Toast.makeText(this, "ID Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val productImageView: ImageView = findViewById(R.id.imageView18)
        val backButton: ImageView = findViewById(R.id.back_detail_product)
        val productNameTextView: TextView = findViewById(R.id.textView15)
        val productDescriptionTextView: TextView = findViewById(R.id.textView18)
        val productUserNameTextView: TextView = findViewById(R.id.textView31)
        val productUserPhoneTextView: TextView = findViewById(R.id.textView32)
        val goToChatButton: ImageButton = findViewById(R.id.gotochat_detail)
        val goToBarterButton: Button = findViewById(R.id.btn_gotobarter)

        productNameTextView.text = productName
        Glide.with(this)
            .load(productImageUrl)
            .apply(RequestOptions().placeholder(R.drawable.box).error(R.drawable.box))
            .into(productImageView)

        fetchProductDetails(productId, productDescriptionTextView, productUserNameTextView, productUserPhoneTextView)

        backButton.setOnClickListener { onBackPressed() }

        // Navigasi ke ChatActivity
        goToChatButton.setOnClickListener {
            Log.d("DetailProductActivity", "Navigating to ChatActivity with product ID: $productId")
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("PRODUCT_ID", productId)
                putExtra("PRODUCT_NAME", productName)
            }
            startActivity(intent)
        }

        // Tambahkan data barter product saat navigasi ke PilihBarangActivity
        goToBarterButton.setOnClickListener {
            val intent = Intent(this, PilihBarangActivity::class.java).apply {
                putExtra("BARTER_PRODUCT_ID", productId) // ID produk yang ingin dibarter
                putExtra("BARTER_PRODUCT_NAME", productName) // Nama produk yang ingin dibarter
                putExtra("BARTER_PRODUCT_IMAGE", productImageUrl) // Gambar produk yang ingin dibarter
            }
            Log.d("DetailProductActivity", "Navigating to ChooseItemActivity with product ID: $productId")
            startActivity(intent)
        }
    }

    private fun fetchProductDetails(
        productId: String,
        descriptionTextView: TextView,
        userNameTextView: TextView,
        userPhoneTextView: TextView
    ) {
        Log.d("DetailProductActivity", "Fetching product details for ID: $productId")

        firestore.collection("items").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val description = document.getString("catatan") ?: "Deskripsi tidak tersedia"
                    val userId = document.getString("userId") ?: ""

                    Log.d("DetailProductActivity", "Product details loaded. Description=$description, UserID=$userId")
                    descriptionTextView.text = description

                    if (userId.isNotEmpty()) {
                        fetchUserDetails(userId, userNameTextView, userPhoneTextView)
                    } else {
                        Log.e("DetailProductActivity", "User ID is empty in product details")
                        userNameTextView.text = "Nama pengguna tidak tersedia"
                        userPhoneTextView.text = "No. HP tidak tersedia"
                    }
                } else {
                    Log.e("DetailProductActivity", "Product not found in Firestore for ID: $productId")
                    Toast.makeText(this, "Produk tidak ditemukan di database", Toast.LENGTH_SHORT).show()
                    descriptionTextView.text = "Deskripsi tidak ditemukan"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DetailProductActivity", "Error fetching product details: ${exception.message}")
                Toast.makeText(this, "Gagal memuat data produk: ${exception.message}", Toast.LENGTH_SHORT).show()
                descriptionTextView.text = "Gagal memuat deskripsi produk"
            }
    }

    private fun fetchUserDetails(userId: String?, userNameTextView: TextView, userPhoneTextView: TextView) {
        if (userId.isNullOrEmpty()) {
            Log.e("DetailProductActivity", "User ID is null or empty")
            userNameTextView.text = "Nama pengguna tidak tersedia"
            userPhoneTextView.text = "No. HP tidak tersedia"
            return
        }

        Log.d("DetailProductActivity", "Fetching user details for ID: $userId")

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userName = document.getString("name") ?: "Nama pengguna tidak tersedia"
                    val userPhone = document.getString("phone") ?: "No. HP tidak tersedia"

                    Log.d("DetailProductActivity", "User details loaded: Name=$userName, Phone=$userPhone")
                    userNameTextView.text = userName
                    userPhoneTextView.text = userPhone
                } else {
                    Log.e("DetailProductActivity", "User document not found for ID: $userId")
                    userNameTextView.text = "Nama pengguna tidak ditemukan"
                    userPhoneTextView.text = "No. HP tidak ditemukan"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DetailProductActivity", "Error fetching user details: ${exception.message}")
                userNameTextView.text = "Gagal memuat nama pengguna"
                userPhoneTextView.text = "Gagal memuat No. HP"
            }
    }
}