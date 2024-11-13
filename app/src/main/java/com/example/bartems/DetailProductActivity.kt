package com.example.bartems

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bartems.model.Product
import com.google.firebase.firestore.FirebaseFirestore

class DetailProductActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var productImageView: ImageView
    private lateinit var productNameTextView: TextView
    private lateinit var productDescriptionTextView: TextView
    private lateinit var productWeightTextView: TextView
    private lateinit var productQuantityTextView: TextView
    private lateinit var productAddressTextView: TextView
    private lateinit var productPostalCodeTextView: TextView
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_product)

        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi UI
        productImageView = findViewById(R.id.product_image_detail)
        productNameTextView = findViewById(R.id.product_name)
        productDescriptionTextView = findViewById(R.id.product_description)
        productWeightTextView = findViewById(R.id.product_weight)
        productQuantityTextView = findViewById(R.id.product_quantity)
        productAddressTextView = findViewById(R.id.product_address)
        productPostalCodeTextView = findViewById(R.id.product_postal_code)
        backButton = findViewById(R.id.back_detail_product)

        // Ambil ID produk dari Intent
        val productId = intent.getStringExtra("PRODUCT_ID")

        if (productId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid Product ID", Toast.LENGTH_SHORT).show()
            finish() // Tutup aktivitas jika ID produk tidak valid
        } else {
            loadProductDetails(productId)
        }

        backButton.setOnClickListener {
            onBackPressed() // Kembali ke aktivitas sebelumnya
        }
    }

    private fun loadProductDetails(productId: String) {
        firestore.collection("items").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val product = document.toObject(Product::class.java)
                    if (product != null) {
                        // Set data ke UI
                        Glide.with(this)
                            .load(product.imageUrl)
                            .into(productImageView)
                        productNameTextView.text = product.namaProduk
                        productDescriptionTextView.text = product.catatan
                        productWeightTextView.text = "Weight: ${product.berat} kg"
                        productQuantityTextView.text = "Quantity: ${product.jumlah}"
                        productAddressTextView.text = "Address: ${product.alamat}"
                        productPostalCodeTextView.text = "Postal Code: ${product.kodePos}"
                    }
                } else {
                    Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengambil detail produk: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}