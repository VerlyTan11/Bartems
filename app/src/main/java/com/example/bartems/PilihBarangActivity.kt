package com.example.bartems

import android.content.Intent
import android.os.Bundle
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

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recycler_view_products)
        backButton = findViewById(R.id.back_button)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = PilihBarangAdapter { product ->
            onProductSelected(product)
        }
        recyclerView.adapter = adapter

        loadUserProducts()

        backButton.setOnClickListener {
            onBackPressed()
        }
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
                    document.toObject(Product::class.java).apply {
                        id = document.id
                    }
                }
                adapter.setProducts(products)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat produk: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onProductSelected(product: Product) {
        val intent = Intent(this, BarterActivity::class.java)
        intent.putExtra("SELECTED_PRODUCT_ID", product.id)
        intent.putExtra("SELECTED_PRODUCT_NAME", product.nama_produk)
        intent.putExtra("SELECTED_PRODUCT_IMAGE", product.imageUrl)
        startActivity(intent)
    }
}