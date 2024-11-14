package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private lateinit var editButton: Button
    private lateinit var trashButton: ImageButton
    private lateinit var availabilityButton: Button

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
        editButton = findViewById(R.id.btn_Edit)
        trashButton = findViewById(R.id.trash_icon)
        availabilityButton = findViewById(R.id.button5)

        // Ambil ID produk dari Intent
        val productId = intent.getStringExtra("PRODUCT_ID")

        if (productId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid Product ID", Toast.LENGTH_SHORT).show()
            finish() // Tutup aktivitas jika ID produk tidak valid
        } else {
            loadProductDetails(productId)
            listenToProductChanges(productId)
        }

        backButton.setOnClickListener {
            onBackPressed() // Kembali ke aktivitas sebelumnya
        }

        editButton.setOnClickListener {
            val intent = Intent(this, EditProductActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }

        trashButton.setOnClickListener {
            productId?.let { id ->
                deleteProduct(id)
            }
        }

        availabilityButton.setOnClickListener {
            productId?.let { id ->
                updateProductAvailability(id, false) // Ubah status menjadi tidak tersedia setelah konfirmasi barter
            }
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
                        productNameTextView.text = product.nama_produk ?: "Nama Produk Tidak Tersedia"
                        productDescriptionTextView.text = product.catatan ?: "Deskripsi Tidak Tersedia"
                        productWeightTextView.text = "Berat: ${product.berat ?: "N/A"} kg"
                        productQuantityTextView.text = "Kuantitas: ${product.jumlah ?: "N/A"}"
                        productAddressTextView.text = "Alamat: ${product.alamat ?: ""}, No: ${product.no_rumah ?: ""}"
                        productPostalCodeTextView.text = "Kode Pos: ${product.kode_pos ?: "Kode Pos Tidak Tersedia"}"

                        // Tampilkan status ketersediaan
                        setAvailabilityStatus(product.tersedia)
                    }
                } else {
                    Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengambil detail produk: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setAvailabilityStatus(tersedia: Boolean) {
        if (tersedia) {
            availabilityButton.text = "Tersedia"
            availabilityButton.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
        } else {
            availabilityButton.text = "Tidak Tersedia"
            availabilityButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
        }
    }

    private fun updateProductAvailability(productId: String, tersedia: Boolean) {
        firestore.collection("items").document(productId)
            .update("tersedia", tersedia)
            .addOnSuccessListener {
                Toast.makeText(this, "Status ketersediaan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                setAvailabilityStatus(tersedia) // Update UI secara langsung
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memperbarui status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteProduct(productId: String) {
        firestore.collection("items").document(productId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
                finish() // Tutup aktivitas setelah produk dihapus
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus produk: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listenToProductChanges(productId: String) {
        firestore.collection("items").document(productId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Gagal mendapatkan update: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val product = snapshot.toObject(Product::class.java)
                    if (product != null) {
                        setAvailabilityStatus(product.tersedia)
                    }
                }
            }
    }
}