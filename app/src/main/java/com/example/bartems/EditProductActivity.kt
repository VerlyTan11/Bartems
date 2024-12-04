package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bartems.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProductActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var productImageView: ImageView
    private lateinit var productNameTextView: TextView
    private lateinit var productDescriptionTextView: TextView
    private lateinit var productWeightTextView: TextView
    private lateinit var productQuantityTextView: TextView
    private lateinit var productAddressTextView: TextView
    private lateinit var productPostalCodeTextView: TextView
    private lateinit var userAvatar: ImageView
    private lateinit var userName: TextView
    private lateinit var userPhone: TextView
    private lateinit var backButton: ImageView
    private lateinit var editButton: Button
    private lateinit var trashButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_product)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Inisialisasi komponen UI
        productImageView = findViewById(R.id.product_image_detail)
        productNameTextView = findViewById(R.id.product_name)
        productDescriptionTextView = findViewById(R.id.product_description)
        productWeightTextView = findViewById(R.id.product_weight)
        productQuantityTextView = findViewById(R.id.product_quantity)
        productAddressTextView = findViewById(R.id.product_address)
        productPostalCodeTextView = findViewById(R.id.product_postal_code)
        userAvatar = findViewById(R.id.user_avatar)
        userName = findViewById(R.id.user_name)
        userPhone = findViewById(R.id.user_phone)
        backButton = findViewById(R.id.back_detail_product)
        editButton = findViewById(R.id.btn_Edit)
        trashButton = findViewById(R.id.trash_icon)

        // Ambil ID produk dari Intent
        val productId = intent.getStringExtra("PRODUCT_ID")

        if (productId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid Product ID", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            loadProductDetails(productId)
        }

        backButton.setOnClickListener {
            onBackPressed()
        }

        editButton.setOnClickListener {
            val intent = Intent(this, EditDetailProductActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }

        trashButton.setOnClickListener {
            productId?.let { id -> deleteProduct(id) }
        }
    }

    private fun loadProductDetails(productId: String) {
        firestore.collection("items").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val product = document.toObject(Product::class.java)
                    if (product != null) {
                        Glide.with(this).load(product.imageUrl).into(productImageView)
                        productNameTextView.text = product.nama_produk ?: "Nama Produk Tidak Tersedia"
                        productDescriptionTextView.text = product.catatan ?: "Deskripsi Tidak Tersedia"
                        productWeightTextView.text = "Berat: ${product.berat ?: "N/A"} kg"
                        productQuantityTextView.text = "Kuantitas: ${product.jumlah ?: "N/A"}"
                        productAddressTextView.text =
                            "Alamat: ${product.alamat ?: ""}, No: ${product.no_rumah ?: ""}"
                        productPostalCodeTextView.text = "Kode Pos: ${product.kode_pos ?: "Kode Pos Tidak Tersedia"}"

                        // Ambil data pengguna berdasarkan userId
                        loadUserDetails(product.userId)
                    }
                } else {
                    Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengambil detail produk: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserDetails(userId: String?) {
        if (userId.isNullOrEmpty()) {
            userName.text = "Nama User Tidak Tersedia"
            userPhone.text = "Nomor HP Tidak Tersedia"
            userAvatar.setImageResource(R.drawable.default_profile_image)
            return
        }

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Nama User Tidak Tersedia"
                    val phone = document.getString("phone") ?: "Nomor HP Tidak Tersedia"
                    val profileImageUrl = document.getString("imageUrl")

                    userName.text = name
                    userPhone.text = phone
                    Glide.with(this)
                        .load(profileImageUrl ?: R.drawable.default_profile_image)
                        .placeholder(R.drawable.default_profile_image)
                        .into(userAvatar)
                } else {
                    userName.text = "Nama User Tidak Tersedia"
                    userPhone.text = "Nomor HP Tidak Tersedia"
                    userAvatar.setImageResource(R.drawable.default_profile_image)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengambil data user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteProduct(productId: String) {
        firestore.collection("items").document(productId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus produk: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}