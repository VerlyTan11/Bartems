package com.example.bartems

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import android.Manifest
import android.location.Location

class HomeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var loadingIndicator: View
    private lateinit var loadingAnimation: LottieAnimationView
    private val productRecyclerList = mutableListOf<ProductRecyclerList>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Request location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            getLastLocation()
        }

        // Initialize the spinner
        val nearMeDropdown: Spinner = findViewById(R.id.nearMeDropdown)
        val options = listOf("Show All", "Show Products Near Me")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        nearMeDropdown.adapter = adapter

        nearMeDropdown.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                if (position == 1) { // "Show Products Near Me" selected
                    if (currentLatitude != null && currentLongitude != null) {
                        fetchProductsNearMe(currentLatitude!!, currentLongitude!!)
                    } else {
                        Toast.makeText(this@HomeActivity, "Unable to get your location", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    fetchProducts() // Fetch all products
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {}
        })

        fetchUserName()
        setupSearchListener(searchTextInput)
        setupProfileNavigation()
        setupAddItemNavigation()
        fetchProducts()
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->  // Use Location class here
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                } else {
                    Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchProductsNearMe(userLatitude: Double, userLongitude: Double) {
        Log.d("HomeActivity", "Fetching nearby products from Firestore...")

        showLoadingAnimation() // Show loading animation

        db.collection("items")
            .get()
            .addOnSuccessListener { documents ->
                productRecyclerList.clear()

                for (document in documents) {
                    val id = document.id
                    val name = document.getString("nama_produk") ?: "Nama produk tidak tersedia"
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val productLatitude = document.getDouble("latitude") // Assuming latitude is stored
                    val productLongitude = document.getDouble("longitude") // Assuming longitude is stored

                    if (name.isNotBlank() && id.isNotBlank() && productLatitude != null && productLongitude != null) {
                        val distance = calculateDistance(userLatitude, userLongitude, productLatitude, productLongitude)

                        if (distance <= 20) { // 20 km radius
                            productRecyclerList.add(ProductRecyclerList(id, name, imageUrl))
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load products: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                hideLoadingAnimation() // Hide loading animation
            }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radius = 6371 // Earth's radius in kilometers

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return radius * c // Returns the distance in kilometers
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

                    if (name.isNotBlank() && id.isNotBlank()) {
                        productRecyclerList.add(ProductRecyclerList(id, name, imageUrl))
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
                Toast.makeText(this, "Gagal mencari produk: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                hideLoadingAnimation() // Pastikan animasi berhenti setelah proses selesai (sukses atau gagal)
            }
    }
}
