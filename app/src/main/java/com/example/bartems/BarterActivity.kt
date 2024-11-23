package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class BarterActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.barter_page)

        firestore = FirebaseFirestore.getInstance()

        // Ambil data intent
        val barterProductId = intent.getStringExtra("BARTER_PRODUCT_ID") ?: ""
        val barterProductName = intent.getStringExtra("BARTER_PRODUCT_NAME") ?: ""
        val barterProductImage = intent.getStringExtra("BARTER_PRODUCT_IMAGE") ?: ""
        val selectedProductId = intent.getStringExtra("SELECTED_PRODUCT_ID") ?: ""
        val selectedProductName = intent.getStringExtra("SELECTED_PRODUCT_NAME") ?: ""
        val selectedProductImage = intent.getStringExtra("SELECTED_PRODUCT_IMAGE") ?: ""

        // Debugging data intent
        Log.d("BarterActivity", "Intent Data:")
        Log.d("BarterActivity", "BARTER_PRODUCT_ID: $barterProductId")
        Log.d("BarterActivity", "BARTER_PRODUCT_NAME: $barterProductName")
        Log.d("BarterActivity", "BARTER_PRODUCT_IMAGE: $barterProductImage")
        Log.d("BarterActivity", "SELECTED_PRODUCT_ID: $selectedProductId")
        Log.d("BarterActivity", "SELECTED_PRODUCT_NAME: $selectedProductName")
        Log.d("BarterActivity", "SELECTED_PRODUCT_IMAGE: $selectedProductImage")

        if (barterProductId.isEmpty() || selectedProductId.isEmpty()) {
            Log.e("BarterActivity", "Data produk tidak lengkap")
            Toast.makeText(this, "Data produk tidak lengkap", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Komponen UI
        val backButton = findViewById<ImageView>(R.id.back_barter)
        val confirmButton = findViewById<Button>(R.id.btn_confirm_barter)
        val selectedProductImageView = findViewById<ImageView>(R.id.imageView20)
        val barterProductImageView = findViewById<ImageView>(R.id.imageView22)
        val selectedProductNameTextView = findViewById<TextView>(R.id.textView22)
        val barterProductNameTextView = findViewById<TextView>(R.id.textView26)
        val selectedProductSellerTextView = findViewById<TextView>(R.id.textView23)
        val barterProductSellerTextView = findViewById<TextView>(R.id.textView27)
        val addressTextView = findViewById<TextView>(R.id.textView32)

        // Set produk Anda
        selectedProductNameTextView.text = selectedProductName
        Glide.with(this).load(selectedProductImage).into(selectedProductImageView)
        selectedProductSellerTextView.text = "Anda"

        // Debugging produk Anda
        Log.d("BarterActivity", "Produk Anda berhasil di-set: $selectedProductName")

        // Ambil detail produk barter dari Firestore
        firestore.collection("items").document(barterProductId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userId = document.getString("userId") ?: ""
                    val barterProductAddress = document.getString("alamat") ?: "Alamat tidak tersedia"

                    // Set nama produk
                    barterProductNameTextView.text = barterProductName
                    addressTextView.text = barterProductAddress
                    Glide.with(this).load(barterProductImage).into(barterProductImageView)

                    // Ambil nama pemilik produk dari koleksi `users`
                    if (userId.isNotEmpty()) {
                        firestore.collection("users").document(userId)
                            .get()
                            .addOnSuccessListener { userDocument ->
                                if (userDocument.exists()) {
                                    val barterProductSeller = userDocument.getString("name") ?: "Penjual tidak diketahui"
                                    barterProductSellerTextView.text = barterProductSeller
                                    Log.d("BarterActivity", "Produk barter berhasil dimuat: $barterProductName, Penjual=$barterProductSeller")
                                } else {
                                    barterProductSellerTextView.text = "Penjual tidak diketahui"
                                    Log.e("BarterActivity", "User ID tidak ditemukan di database")
                                }
                            }
                            .addOnFailureListener { exception ->
                                barterProductSellerTextView.text = "Penjual tidak diketahui"
                                Log.e("BarterActivity", "Gagal memuat data pengguna: ${exception.message}")
                            }
                    } else {
                        barterProductSellerTextView.text = "Penjual tidak diketahui"
                        Log.e("BarterActivity", "User ID tidak tersedia di dokumen produk")
                    }
                } else {
                    Log.e("BarterActivity", "Produk barter tidak ditemukan di database")
                    Toast.makeText(this, "Produk barter tidak ditemukan di database", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("BarterActivity", "Gagal memuat data produk barter: ${exception.message}")
                Toast.makeText(this, "Gagal memuat data produk barter: ${exception.message}", Toast.LENGTH_SHORT).show()
                finish()
            }

        // Tombol kembali
        backButton.setOnClickListener {
            Log.d("BarterActivity", "Tombol kembali ditekan")
            onBackPressed()
        }

        // Tombol konfirmasi barter
        confirmButton.setOnClickListener {
            // Siapkan data untuk disimpan
            val barterHistoryData = mapOf(
                "barterProductId" to barterProductId,
                "barterProductName" to barterProductName,
                "barterProductImage" to barterProductImage,
                "barterProductOwner" to barterProductSellerTextView.text.toString(),
                "selectedProductId" to selectedProductId,
                "selectedProductName" to selectedProductName,
                "selectedProductImage" to selectedProductImage,
                "selectedProductOwner" to "Anda",
                "address" to addressTextView.text.toString(),
                "timestamp" to System.currentTimeMillis() // Waktu transaksi
            )

            // Simpan data ke Firestore
            firestore.collection("barter_history")
                .add(barterHistoryData)
                .addOnSuccessListener { documentReference ->
                    Log.d("BarterActivity", "Barter history berhasil disimpan dengan ID: ${documentReference.id}")

                    // Kirim data ke DoneFragment
                    val fragment = DoneFragment()
                    val bundle = Bundle().apply {
                        putString("barterProductName", barterProductName)
                        putString("selectedProductName", selectedProductName)
                        putString("barterProductImage", barterProductImage)
                        putString("selectedProductImage", selectedProductImage)
                    }
                    fragment.arguments = bundle

                    // Tampilkan DoneFragment
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                .addOnFailureListener { exception ->
                    Log.e("BarterActivity", "Gagal menyimpan barter history: ${exception.message}")
                    Toast.makeText(this, "Gagal menyimpan transaksi barter. Coba lagi.", Toast.LENGTH_SHORT).show()
                }
        }

        // Tombol ke peta
        val goToMapButton = findViewById<ImageView>(R.id.gotomap_barter)
        goToMapButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra("ADDRESS", addressTextView.text.toString())
            }
            Log.d("BarterActivity", "Navigating to MapActivity with address: ${addressTextView.text}")
            startActivity(intent)
        }
    }
}