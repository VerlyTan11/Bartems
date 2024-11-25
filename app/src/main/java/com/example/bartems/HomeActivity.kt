package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var loadingIndicator: View
    private val productRecyclerList = mutableListOf<ProductRecyclerList>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        db = FirebaseFirestore.getInstance()

        // Inisialisasi UI
        val searchTextInput: TextInputEditText = findViewById(R.id.textInputEditText)
        recyclerView = findViewById(R.id.recyclerView)
        loadingIndicator = findViewById(R.id.loading_indicator)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Inisialisasi Adapter
        adapter = ProductAdapter(productRecyclerList) { product ->
            Log.d("HomeActivity", "Navigating to product detail: ${product.name} (ID: ${product.id})")

            if (product.id.isNotBlank()) {
                val intent = Intent(this, DetailProductActivity::class.java).apply {
                    putExtra("PRODUCT_ID", product.id)
                    putExtra("PRODUCT_NAME", product.name)
                    putExtra("PRODUCT_IMAGE_URL", product.imageUrl)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "ID produk tidak valid", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerView.adapter = adapter

        fetchUserName()
        setupSearchListener(searchTextInput)
        setupProfileNavigation()
        setupAddItemNavigation()
        fetchProducts()
    }

    private fun fetchUserName() {
        val userNameTextView: TextView = findViewById(R.id.textView16)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name") ?: "Nama tidak ditemukan"
                    userNameTextView.text = name
                }
                .addOnFailureListener {
                    userNameTextView.text = "Error mengambil nama"
                }
        } else {
            userNameTextView.text = "User ID tidak ditemukan"
        }
    }

    private fun setupSearchListener(searchTextInput: TextInputEditText) {
        searchTextInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase(Locale.ROOT)
                if (query.isNotEmpty()) {
                    searchProduct(query)
                } else {
                    // Jika input kosong, tampilkan semua produk
                    fetchProducts()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun resetSearch() {
        productRecyclerList.clear()
        adapter.notifyDataSetChanged() // Bersihkan data di RecyclerView
    }

    private fun setupProfileNavigation() {
        val goToProfileButton: ImageButton = findViewById(R.id.gotoprofile)
        goToProfileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupAddItemNavigation() {
        val addItemButton: ImageView = findViewById(R.id.add_item)
        addItemButton.setOnClickListener {
            startActivity(Intent(this, PostItemActivity::class.java))
        }
    }

    private fun fetchProducts() {
        Log.d("HomeActivity", "Fetching products from Firestore...")
        loadingIndicator.visibility = View.VISIBLE

        db.collection("items")
            .get()
            .addOnSuccessListener { documents ->
                loadingIndicator.visibility = View.GONE
                productRecyclerList.clear()

                for (document in documents) {
                    val id = document.id
                    val name = document.getString("nama_produk") ?: "Nama produk tidak tersedia"
                    val imageUrl = document.getString("imageUrl") ?: ""

                    if (name.isNotBlank() && id.isNotBlank()) {
                        productRecyclerList.add(ProductRecyclerList(id, name, imageUrl))
                    }
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                loadingIndicator.visibility = View.GONE
                Toast.makeText(this, "Gagal memuat produk: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun searchProduct(query: String) {
        Log.d("HomeActivity", "Searching for products with query: $query")
        loadingIndicator.visibility = View.VISIBLE

        db.collection("items")
            .get()
            .addOnSuccessListener { documents ->
                loadingIndicator.visibility = View.GONE
                productRecyclerList.clear()

                for (document in documents) {
                    val id = document.id
                    val name = document.getString("nama_produk") ?: "Nama produk tidak tersedia"
                    val imageUrl = document.getString("imageUrl") ?: ""

                    // Case Insensitive Matching
                    if (name.lowercase(Locale.ROOT).contains(query)) {
                        productRecyclerList.add(ProductRecyclerList(id, name, imageUrl))
                    }
                }

                if (productRecyclerList.isEmpty()) {
                    Toast.makeText(this, "Tidak ada hasil pencarian untuk \"$query\"", Toast.LENGTH_SHORT).show()
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                loadingIndicator.visibility = View.GONE
                Toast.makeText(this, "Gagal mencari produk: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
