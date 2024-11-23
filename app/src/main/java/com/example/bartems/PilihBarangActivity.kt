package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bartems.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PilihBarangActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: ImageView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: PilihBarangAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pilih_barang)

        // Inisialisasi Firestore dan FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Inisialisasi UI
        recyclerView = findViewById(R.id.recycler_view_products)
        backButton = findViewById(R.id.back_button)

        // Atur RecyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = PilihBarangAdapter { product -> onProductSelected(product) }
        recyclerView.adapter = adapter

        // Load produk pengguna
        loadUserProducts()

        // Tombol kembali
        backButton.setOnClickListener { onBackPressed() }
    }

    private fun loadUserProducts() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("items")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val products = documents.mapNotNull { document ->
                    document.toObject(Product::class.java).apply { id = document.id }
                }
                adapter.setProducts(products)
                Log.d("PilihBarangActivity", "User products loaded: ${products.size} items")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat produk: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("PilihBarangActivity", "Error loading user products: ${e.message}")
            }
    }

    private fun onProductSelected(product: Product) {
        val barterProductId = intent.getStringExtra("BARTER_PRODUCT_ID") ?: ""
        val barterProductName = intent.getStringExtra("BARTER_PRODUCT_NAME") ?: ""
        val barterProductImage = intent.getStringExtra("BARTER_PRODUCT_IMAGE") ?: ""

        if (barterProductId.isEmpty() || barterProductName.isEmpty() || barterProductImage.isEmpty()) {
            Log.e("PilihBarangActivity", "Data produk barter tidak lengkap")
            Toast.makeText(this, "Data produk barter tidak lengkap", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, BarterActivity::class.java).apply {
            putExtra("BARTER_PRODUCT_ID", barterProductId)
            putExtra("BARTER_PRODUCT_NAME", barterProductName)
            putExtra("BARTER_PRODUCT_IMAGE", barterProductImage)
            putExtra("SELECTED_PRODUCT_ID", product.id)
            putExtra("SELECTED_PRODUCT_NAME", product.nama_produk)
            putExtra("SELECTED_PRODUCT_IMAGE", product.imageUrl)
        }

        Log.d("PilihBarangActivity", "Selected Product: ${product.nama_produk} (ID: ${product.id})")
        Log.d("PilihBarangActivity", "Barter Product ID: $barterProductId")
        startActivity(intent)
    }
}