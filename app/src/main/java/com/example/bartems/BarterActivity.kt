package com.example.bartems

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class BarterActivity : AppCompatActivity() {

    companion object {
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.barter_page)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Ambil data intent
        val barterProductId = intent.getStringExtra("BARTER_PRODUCT_ID") ?: ""
        val barterProductName = intent.getStringExtra("BARTER_PRODUCT_NAME") ?: ""
        val barterProductImage = intent.getStringExtra("BARTER_PRODUCT_IMAGE") ?: ""
        val selectedProductId = intent.getStringExtra("SELECTED_PRODUCT_ID") ?: ""
        val selectedProductName = intent.getStringExtra("SELECTED_PRODUCT_NAME") ?: ""
        val selectedProductImage = intent.getStringExtra("SELECTED_PRODUCT_IMAGE_URL") ?: ""
        val ownQuantity = intent.getIntExtra("QUANTITY_OWN", 0)
        val otherQuantity = intent.getIntExtra("QUANTITY_OTHER", 0)

        // Debugging data intent
        Log.d("BarterActivity", "Intent Data:")
        Log.d("BarterActivity", "BARTER_PRODUCT_ID: $barterProductId")
        Log.d("BarterActivity", "BARTER_PRODUCT_NAME: $barterProductName")
        Log.d("BarterActivity", "BARTER_PRODUCT_IMAGE: $barterProductImage")
        Log.d("BarterActivity", "SELECTED_PRODUCT_ID: $selectedProductId")
        Log.d("BarterActivity", "SELECTED_PRODUCT_NAME: $selectedProductName")
        Log.d("BarterActivity", "SELECTED_PRODUCT_IMAGE: $selectedProductImage")
        Log.d("BarterActivity", "Own Quantity: $ownQuantity, Other Quantity: $otherQuantity")

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
        val ownQuantityTextView = findViewById<TextView>(R.id.textView_own_quantity)
        val otherQuantityTextView = findViewById<TextView>(R.id.textView_other_quantity)

        // Set jumlah produk
        ownQuantityTextView.text = "Jumlah Anda: $ownQuantity"
        otherQuantityTextView.text = "Jumlah Produk Lain: $otherQuantity"

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

                    // Set nama produk barter dan alamat
                    barterProductNameTextView.text = barterProductName
                    addressTextView.text = barterProductAddress
                    Glide.with(this).load(barterProductImage).into(barterProductImageView)

                    // Ambil nama pemilik produk dari koleksi users
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
                                    Log.e("BarterActivity", "User ID tidak ditemukan")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("BarterActivity", "Gagal memuat data user: ${e.message}")
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("BarterActivity", "Gagal memuat produk barter: ${e.message}")
            }
        // Tombol kembali
        backButton.setOnClickListener {
            Log.d("BarterActivity", "Tombol kembali ditekan")
            onBackPressed()
        }

        // Tombol konfirmasi barter
        confirmButton.setOnClickListener {
            // Ambil userId pengguna yang login
            val currentUserId = auth.currentUser?.uid ?: run {
                Toast.makeText(this, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ambil nama pengguna saat ini
            val currentUserName = auth.currentUser?.displayName ?: "Pengguna Tidak Diketahui"

            // Siapkan data untuk disimpan
            val barterHistoryData = mapOf(
                "barterProductId" to barterProductId,
                "barterProductName" to barterProductName,
                "barterProductImage" to barterProductImage,
                "barterProductOwner" to barterProductSellerTextView.text.toString(),
                "selectedProductId" to selectedProductId,
                "selectedProductName" to selectedProductName,
                "selectedProductImage" to selectedProductImage,
                "selectedProductOwner" to currentUserName, // Nama pengguna yang login
                "userId" to currentUserId, // User ID pengguna yang login
                "address" to addressTextView.text.toString(),
                "timestamp" to System.currentTimeMillis()
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

                    // Tampilkan notifikasi dan lanjut ke HomeActivity
                    if (checkNotificationPermission()) {
                        showNotification("Produk Barter", "Produk Anda berhasil ditukar!")
                        navigateToHomeActivity() // Change this to navigate to HomeActivity
                    } else {
                        requestNotificationPermission()
                    }
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

    private fun checkNotificationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            NOTIFICATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "barter_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Barter Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.icon_notif)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
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
        NotificationManagerCompat.from(this).notify(1, notification)
    }

    private fun navigateToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }
}
