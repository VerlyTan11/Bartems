package com.example.bartems

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var searchTextInput: TextInputEditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageButtonAdapter
    private val productList = mutableListOf<String>() // List data produk

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Inisialisasi Firestore
        db = FirebaseFirestore.getInstance()

        // Inisialisasi UI
        searchTextInput = findViewById(R.id.textInputEditText)
        recyclerView = findViewById(R.id.recyclerView)

        // Setup RecyclerView
        adapter = ImageButtonAdapter(productList) { product ->
            // Handle item click
            val intent = Intent(this@HomeActivity, DetailProductActivity::class.java)
            intent.putExtra("PRODUCT_NAME", product)
            startActivity(intent)
        }
        recyclerView.layoutManager = GridLayoutManager(this, 2) // Two columns
        recyclerView.adapter = adapter

        // Ambil nama pengguna
        fetchUserName()

        // Listener untuk pencarian
        setupSearchListener()

        // Navigasi tombol profil
        setupProfileNavigation()

        // Navigasi tambah item
        setupAddItemNavigation()

        // Ambil semua produk dari Firestore
        fetchProducts()
    }

    private fun fetchUserName() {
        val userNameTextView = findViewById<TextView>(R.id.textView16)
        val userName = intent.getStringExtra("USER_NAME")
        if (userName != null) {
            userNameTextView.text = userName
        } else {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                FirebaseDatabase.getInstance().getReference("Users").child(userId).get()
                    .addOnSuccessListener { dataSnapshot ->
                        if (dataSnapshot.exists()) {
                            val name = dataSnapshot.child("name").value.toString()
                            userNameTextView.text = name
                        } else {
                            userNameTextView.text = "Nama tidak ditemukan"
                        }
                    }.addOnFailureListener {
                        userNameTextView.text = "Error mengambil nama"
                    }
            } else {
                userNameTextView.text = "User ID tidak ditemukan"
            }
        }
    }

    private fun setupSearchListener() {
        searchTextInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    searchProduct(query)
                } else {
                    fetchProducts()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupProfileNavigation() {
        val goToProfileButton = findViewById<ImageButton>(R.id.gotoprofile)
        goToProfileButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupAddItemNavigation() {
        val addItem = findViewById<ImageView>(R.id.add_item)
        addItem.setOnClickListener {
            val intent = Intent(this@HomeActivity, PostItemActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchProducts() {
        db.collection("products") // Ganti dengan nama koleksi Anda
            .get()
            .addOnSuccessListener { documents ->
                productList.clear()
                for (document in documents) {
                    val name = document.getString("name") ?: "Tanpa Nama"
                    productList.add(name) // Store product name in the list
                }
                adapter.notifyDataSetChanged() // Notify adapter to update the RecyclerView
            }
            .addOnFailureListener {
                productList.clear()
                productList.add("Error: ${it.message}") // Add error message to list
                adapter.notifyDataSetChanged()
            }
    }

    private fun searchProduct(query: String) {
        db.collection("products") // Ganti dengan nama koleksi Anda
            .whereEqualTo("name", query)
            .get()
            .addOnSuccessListener { documents ->
                productList.clear()
                if (documents.isEmpty) {
                    productList.add("Produk tidak ditemukan") // Add message if no product found
                } else {
                    for (document in documents) {
                        val name = document.getString("name") ?: "Tanpa Nama"
                        productList.add(name)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                productList.clear()
                productList.add("Error: ${it.message}") // Add error message to list
                adapter.notifyDataSetChanged()
            }
    }
}
