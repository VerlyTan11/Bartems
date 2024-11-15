package com.example.bartems

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.textfield.TextInputEditText
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.database.FirebaseDatabase

class HomeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var searchTextInput: TextInputEditText
    private lateinit var searchResultTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Inisialisasi Firestore
        db = FirebaseFirestore.getInstance()

        // Inisialisasi UI
        searchTextInput = findViewById(R.id.textInputEditText) // Ganti dengan ID yang sesuai
        searchResultTextView = findViewById(R.id.searchResultTextView)

        // Ambil nama pengguna dari Intent, jika ada
        val userName = intent.getStringExtra("USER_NAME")
        val userNameTextView = findViewById<TextView>(R.id.textView16)

        if (userName != null) {
            userNameTextView.text = userName
        } else {
            // Ambil nama pengguna dari database jika tidak ada dari Intent
            val userId = FirebaseAuth.getInstance().currentUser ?.uid
            if (userId != null) {
                FirebaseDatabase.getInstance().getReference("Users").child(userId).get().addOnSuccessListener { dataSnapshot ->
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
                userNameTextView.text = "User  ID tidak ditemukan"
            }
        }

        // Listener untuk TextInputEditText
        searchTextInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    searchResultTextView.visibility = View.GONE // Sembunyikan hasil pencarian saat mengetik
                    searchProduct(query)
                } else {
                    searchResultTextView.visibility = View.GONE // Sembunyikan hasil pencarian jika tidak ada input
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Handle click event untuk ImageButton gotoprofile
        val goToProfileButton = findViewById<ImageButton>(R.id.gotoprofile)
        goToProfileButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Handle click event for add_item ImageView
        val addItem = findViewById<ImageView>(R.id.add_item)
        addItem.setOnClickListener {
            val intent = Intent(this@HomeActivity, PostItemActivity::class.java)
            startActivity(intent)
        }

        // Handle click event untuk ImageButton cth_produk
        val cthProduct = findViewById<ImageButton>(R.id.cth_produk)
        cthProduct.setOnClickListener {
            val intent = Intent(this@HomeActivity, DetailProductActivity::class.java)
            intent.putExtra("USER_NAME", userNameTextView.text.toString())
            startActivity(intent)
        }
    }

    private fun searchProduct(query: String) {
        // Setel ulang teks hasil pencarian sebelum melakukan pencarian baru
        searchResultTextView.text = "" // Kosongkan teks sebelumnya
        searchResultTextView.visibility = View.GONE // Sembunyikan hasil pencarian sebelumnya

        db.collection("products") // Ganti dengan nama koleksi Anda
            .whereEqualTo("name", query) // Ganti "name" dengan nama field yang sesuai
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Tampilkan pesan tanpa menyertakan nama barang
                    searchResultTextView.text = "Produk tidak ditemukan"
                    searchResultTextView.visibility = View.VISIBLE
                } else {
                    searchResultTextView.visibility = View.GONE
                    // Lakukan sesuatu dengan hasil yang ditemukan (misalnya, tampilkan di RecyclerView)
                }
            }
            .addOnFailureListener { exception ->
                searchResultTextView.text = "Error: ${exception.message}"
                searchResultTextView.visibility = View.VISIBLE
            }
    }
}