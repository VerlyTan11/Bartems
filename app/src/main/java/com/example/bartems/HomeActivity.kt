package com.example.bartems

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var loadingIndicator: View
    private lateinit var loadingAnimation: LottieAnimationView
    private val productRecyclerList = mutableListOf<ProductRecyclerList>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        db = FirebaseFirestore.getInstance()

        // Inisialisasi UI
        val searchTextInput: TextInputEditText = findViewById(R.id.textInputEditText)
        recyclerView = findViewById(R.id.recyclerView)
        loadingIndicator = findViewById(R.id.loading_indicator)
        loadingAnimation = findViewById(R.id.loadingAnimation)
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

        val buttonMulaiBarter: Button = findViewById(R.id.button4)
        buttonMulaiBarter.setOnClickListener {
            startActivity(Intent(this, PostItemActivity::class.java))
        }

        fetchUserName()
        setupSearchListener(searchTextInput)
        setupProfileNavigation()
        setupAddItemNavigation()
        fetchProducts()
    }

    @SuppressLint("SetTextI18n")
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

    private fun setupProfileNavigation() {
        val goToProfileButton: ImageButton = findViewById(R.id.gotoprofile)
        goToProfileButton.setOnClickListener {
            showLoadingAnimation() // Tampilkan animasi loading
            goToProfileWithDelay() // Pindah setelah animasi selesai
        }
    }

    private fun goToProfileWithDelay() {
        val delayTime: Long = 1500 // Waktu tunda (1,5 detik)
        recyclerView.postDelayed({
            // Pastikan animasi berhenti sebelum pindah
            hideLoadingAnimation()
            startActivity(Intent(this, ProfileActivity::class.java))
        }, delayTime)
    }

    private fun showLoadingAnimation() {
        loadingAnimation.visibility = View.VISIBLE
        loadingAnimation.playAnimation()
    }

    private fun hideLoadingAnimation() {
        loadingAnimation.cancelAnimation()
        loadingAnimation.visibility = View.GONE
    }

    private fun setupAddItemNavigation() {
        val addItemButton: ImageView = findViewById(R.id.add_item)
        addItemButton.setOnClickListener {
            startActivity(Intent(this, PostItemActivity::class.java))
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchProducts() {
        Log.d("HomeActivity", "Fetching products from Firestore...")
        showLoadingAnimation() // Tampilkan animasi loading

        db.collection("items")
            .get()
            .addOnSuccessListener { documents ->
                productRecyclerList.clear()

                for (document in documents) {
                    val id = document.id
                    val name = document.getString("nama_produk") ?: "Nama produk tidak tersedia"
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val quantity = document.getLong("jumlah") ?: 0

                    if (quantity > 0) {
                        // Tambahkan barang dengan kuantitas > 0 ke daftar
                        productRecyclerList.add(ProductRecyclerList(id, name, imageUrl))
                    } else {
                        // Hapus barang dengan kuantitas 0 dari Firestore
                        db.collection("items").document(id).delete()
                            .addOnSuccessListener {
                                Log.d("HomeActivity", "Produk dengan ID $id dihapus karena kuantitas habis.")
                            }
                            .addOnFailureListener { e ->
                                Log.e("HomeActivity", "Gagal menghapus produk dengan ID $id: ${e.message}")
                            }
                    }
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal memuat produk: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                hideLoadingAnimation() // Pastikan animasi berhenti setelah proses selesai (sukses atau gagal)
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchProduct(query: String) {
        Log.d("HomeActivity", "Searching for products with query: $query")
        showLoadingAnimation() // Tampilkan animasi loading

        db.collection("items")
            .get()
            .addOnSuccessListener { documents ->
                productRecyclerList.clear()

                for (document in documents) {
                    val id = document.id
                    val name = document.getString("nama_produk") ?: "Nama produk tidak tersedia"
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val quantity = document.getLong("jumlah") ?: 0

                    if (quantity > 0 && name.lowercase(Locale.ROOT).contains(query)) {
                        // Tambahkan barang yang sesuai query dan kuantitas > 0
                        productRecyclerList.add(ProductRecyclerList(id, name, imageUrl))
                    } else if (quantity <= 0) {
                        // Hapus barang dengan kuantitas 0 dari Firestore
                        db.collection("items").document(id).delete()
                            .addOnSuccessListener {
                                Log.d("HomeActivity", "Produk dengan ID $id dihapus karena kuantitas habis.")
                            }
                            .addOnFailureListener { e ->
                                Log.e("HomeActivity", "Gagal menghapus produk dengan ID $id: ${e.message}")
                            }
                    }
                }

                if (productRecyclerList.isEmpty()) {
                    Toast.makeText(this, "Tidak ada hasil pencarian untuk \"$query\"", Toast.LENGTH_SHORT).show()
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal mencari produk: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                hideLoadingAnimation() // Pastikan animasi berhenti setelah proses selesai (sukses atau gagal)
            }
    }
}