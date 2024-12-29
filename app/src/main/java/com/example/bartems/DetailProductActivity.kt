package com.example.bartems

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("DEPRECATION")
class DetailProductActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var loadingAnimation: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_product)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize Lottie Animation
        loadingAnimation = findViewById(R.id.loadingAnimation)

        val currentUserId = auth.currentUser?.uid
        val productId = intent.getStringExtra("PRODUCT_ID")
        val productName = intent.getStringExtra("PRODUCT_NAME") ?: "Nama tidak tersedia"
        val productImageUrl = intent.getStringExtra("PRODUCT_IMAGE_URL") ?: ""

        if (productId.isNullOrEmpty()) {
            Log.e("DetailProductActivity", "ERROR: PRODUCT_ID tidak ditemukan!")
            Toast.makeText(this, "ID Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Bind views
        val productImageView: ImageView = findViewById(R.id.imageView18)
        val backButton: ImageView = findViewById(R.id.back_detail_product)
        val productNameTextView: TextView = findViewById(R.id.textView15)
        val productDescriptionTextView: TextView = findViewById(R.id.textView18)
        val productUserNameTextView: TextView = findViewById(R.id.textView31)
        val productUserPhoneTextView: TextView = findViewById(R.id.textView32)
        val goToBarterButton: Button = findViewById(R.id.btn_gotobarter)

        productNameTextView.text = productName
        Glide.with(this)
            .load(productImageUrl)
            .apply(RequestOptions().placeholder(R.drawable.box).error(R.drawable.box))
            .into(productImageView)

        fetchProductDetails(productId, productDescriptionTextView, productUserNameTextView, productUserPhoneTextView) { ownerId ->
            if (currentUserId == ownerId) {
                setupEditButton(goToBarterButton, productId, productName, productImageUrl)
            } else {
                setupBarterButton(goToBarterButton, productId, productName, productImageUrl)
            }
        }

        backButton.setOnClickListener { onBackPressed() }
    }

    private fun showLoadingAnimation() {
        loadingAnimation.visibility = View.VISIBLE
    }

    private fun hideLoadingAnimation() {
        loadingAnimation.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun setupEditButton(button: Button, productId: String, productName: String, productImageUrl: String) {
        button.text = "Ke halaman edit"
        button.setOnClickListener {
            navigateWithDelay {
                val intent = Intent(this, EditProductActivity::class.java).apply {
                    putExtra("PRODUCT_ID", productId)
                    putExtra("PRODUCT_NAME", productName)
                    putExtra("PRODUCT_IMAGE_URL", productImageUrl)
                }
                startActivity(intent)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupBarterButton(button: Button, productId: String, productName: String, productImageUrl: String) {
        button.text = "Barter"
        button.setOnClickListener {
            navigateWithDelay {
                val intent = Intent(this, PilihBarangActivity::class.java).apply {
                    putExtra("BARTER_PRODUCT_ID", productId)
                    putExtra("BARTER_PRODUCT_NAME", productName)
                    putExtra("BARTER_PRODUCT_IMAGE", productImageUrl)
                }
                startActivity(intent)
            }
        }
    }

    private fun navigateWithDelay(action: () -> Unit) {
        showLoadingAnimation()
        Handler(mainLooper).postDelayed({
            action()
            hideLoadingAnimation()
        }, 1500)
    }

    @SuppressLint("SetTextI18n")
    private fun fetchProductDetails(
        productId: String,
        descriptionTextView: TextView,
        userNameTextView: TextView,
        userPhoneTextView: TextView,
        onOwnerIdFetched: (String?) -> Unit
    ) {
        firestore.collection("items").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val product = document.toObject(Product::class.java)
                    product?.let {
                        descriptionTextView.text = it.catatan
                        findViewById<TextView>(R.id.product_weight).text = "Berat: ${it.berat}"
                        findViewById<TextView>(R.id.product_quantity).text = "Kuantitas: ${it.jumlah}"
                        findViewById<TextView>(R.id.product_address).text = "Alamat: ${it.alamat}"
                        findViewById<TextView>(R.id.product_postal_code).text = "Kode Pos: ${it.kode_pos}"
                    }
                    val ownerId = document.getString("userId")
                    onOwnerIdFetched(ownerId)
                    fetchUserDetails(ownerId, userNameTextView, userPhoneTextView)
                } else {
                    Log.e("DetailProductActivity", "Product not found in Firestore for ID: $productId")
                    Toast.makeText(this, "Produk tidak ditemukan di database", Toast.LENGTH_SHORT).show()
                    descriptionTextView.text = "Deskripsi tidak ditemukan"
                    onOwnerIdFetched(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DetailProductActivity", "Error fetching product details: ${exception.message}")
                Toast.makeText(this, "Gagal memuat data produk: ${exception.message}", Toast.LENGTH_SHORT).show()
                descriptionTextView.text = "Gagal memuat deskripsi produk"
                onOwnerIdFetched(null)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchUserDetails(
        userId: String?,
        userNameTextView: TextView,
        userPhoneTextView: TextView
    ) {
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
                    val userImageUrl = document.getString("imageUrl") // Assuming the image URL is stored in the "imageUrl" field

                    Log.d("DetailProductActivity", "User details loaded: Name=$userName, Phone=$userPhone, Image URL=$userImageUrl")
                    userNameTextView.text = userName
                    userPhoneTextView.text = userPhone

                    // Load the user image into imageView17 using Glide
                    val imageView17: ImageView = findViewById(R.id.imageView17)  // Assuming this is the ImageView where the image should be displayed
                    Glide.with(this)
                        .load(userImageUrl)  // Use the fetched imageUrl
                        .apply(RequestOptions().placeholder(R.drawable.box).error(R.drawable.box))  // Placeholder in case the image fails to load
                        .into(imageView17)  // Load the image into imageView17
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
