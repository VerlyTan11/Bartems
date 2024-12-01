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

class PilihBarangActivity : AppCompatActivity(), JumlahBarangFragment.OnQuantityInputListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: ImageView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: PilihBarangAdapter

    private var selectedProduct: Product? = null
    private var barterProductId: String? = null
    private var barterProductName: String? = null
    private var barterProductImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pilih_barang)

        // Inisialisasi Firestore dan FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Ambil data barter dari Intent
        barterProductId = intent.getStringExtra("BARTER_PRODUCT_ID")
        barterProductName = intent.getStringExtra("BARTER_PRODUCT_NAME")
        barterProductImage = intent.getStringExtra("BARTER_PRODUCT_IMAGE")

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
        selectedProduct = product
        if (barterProductId.isNullOrEmpty() || barterProductName.isNullOrEmpty() || barterProductImage.isNullOrEmpty()) {
            Toast.makeText(this, "Data produk barter tidak lengkap", Toast.LENGTH_SHORT).show()
            return
        }

        // Navigasi ke fragment jumlah barang
        supportFragmentManager.beginTransaction().apply {
            replace(
                R.id.fragment_container, // FrameLayout di layout PilihBarang
                JumlahBarangFragment.newInstance(product.nama_produk)
            )
            addToBackStack(null)
            commit() // Gunakan commit() dengan FragmentTransaction
        }
    }

    override fun onQuantityInput(quantityOwn: Int, quantityOther: Int) {
        val selectedProduct = selectedProduct ?: return

        // Kirim data ke BarterActivity
        val intent = Intent(this, BarterActivity::class.java).apply {
            putExtra("BARTER_PRODUCT_ID", barterProductId)
            putExtra("BARTER_PRODUCT_NAME", barterProductName)
            putExtra("BARTER_PRODUCT_IMAGE", barterProductImage)
            putExtra("SELECTED_PRODUCT_ID", selectedProduct.id)
            putExtra("SELECTED_PRODUCT_NAME", selectedProduct.nama_produk)
            putExtra("SELECTED_PRODUCT_IMAGE_URL", selectedProduct.imageUrl)
            putExtra("QUANTITY_OWN", quantityOwn)  // Kirim quantityOwn
            putExtra("QUANTITY_OTHER", quantityOther)  // Kirim quantityOther
        }
        startActivity(intent)
    }
}