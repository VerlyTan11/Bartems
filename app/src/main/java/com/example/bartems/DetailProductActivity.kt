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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.airbnb.lottie.LottieAnimationView
import com.example.bartems.model.Product

class DetailProductActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var loadingAnimation: LottieAnimationView  // Declare Lottie animation view

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_product)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize Lottie Animation
        loadingAnimation = findViewById(R.id.loadingAnimation)

        val currentUserId = auth.currentUser?.uid
        val productId = intent.getStringExtra("PRODUCT_ID")
        val productName = intent.getStringExtra("PRODUCT_NAME") ?: "Nama tidak tersedia"
        val productImageUrl = intent.getStringExtra("PRODUCT_IMAGE_URL") ?: ""

        Log.d(
            "DetailProductActivity",
            "Intent received: PRODUCT_ID=$productId, PRODUCT_NAME=$productName, PRODUCT_IMAGE_URL=$productImageUrl"
        )

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
        val goToBarterButton: Button = findViewById(R.id.btn_gotobarter)
        val imageView17: ImageView = findViewById(R.id.imageView17)

        productNameTextView.text = productName
        Glide.with(this)
            .load(productImageUrl)
            .apply(RequestOptions().placeholder(R.drawable.box).error(R.drawable.box))
            .into(productImageView)

        // Call fetchProductDetails with only productId
        fetchProductDetails(productId)

        // The rest of your code for handling the button clicks
        backButton.setOnClickListener { onBackPressed() }

        goToBarterButton.setOnClickListener {
            // Add code to handle Go To Barter button click
        }

        currentUserId?.let {
            fetchUserDetails(it, productUserNameTextView, productUserPhoneTextView)
        } ?: run {
            // Handle case where the user is not authenticated (currentUserId is null)
            Log.e("DetailProductActivity", "User is not authenticated")
        }
    }

    private fun showLoadingAnimation() {
        loadingAnimation.visibility = android.view.View.VISIBLE  // Make the animation visible
    }

    private fun hideLoadingAnimation() {
        loadingAnimation.visibility = android.view.View.GONE  // Hide the animation
    }

    private fun fetchProductDetails(productId: String) {
        Log.d("DetailProductActivity", "Fetching product details for ID: $productId")

        firestore.collection("items").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Convert the document to a Product object
                    val product = document.toObject(Product::class.java)
                    product?.let {
                        // Update the UI with product details
                        findViewById<TextView>(R.id.product_weight).text = "Berat: ${it.berat}"
                        findViewById<TextView>(R.id.product_quantity).text = "Kuantitas: ${it.jumlah}"
                        findViewById<TextView>(R.id.product_address).text = "Alamat: ${it.alamat}"
                        findViewById<TextView>(R.id.product_postal_code).text = "Kode Pos: ${it.kode_pos}"
                        findViewById<TextView>(R.id.textView18).text = it.catatan // Assuming this is the description
                    }
                } else {
                    Log.e("DetailProductActivity", "Product not found for ID: $productId")
                    Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DetailProductActivity", "Error fetching product details: ${exception.message}")
                Toast.makeText(this, "Gagal memuat data produk: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserDetails(
        userId: String,
        userNameTextView: TextView,
        userPhoneTextView: TextView
    ) {
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
                    val imageView17: ImageView = findViewById(R.id.imageView17)
                    if (userImageUrl != null) {
                        Glide.with(this)
                            .load(userImageUrl)
                            .apply(RequestOptions().placeholder(R.drawable.box).error(R.drawable.box))
                            .into(imageView17)
                    } else {
                        // Set default image if userImageUrl is null
                        Glide.with(this)
                            .load(R.drawable.default_profile_image)
                            .apply(RequestOptions().placeholder(R.drawable.box).error(R.drawable.box))
                            .into(imageView17)
                    }
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
