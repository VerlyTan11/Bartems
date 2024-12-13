package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.Context

class BarterActivity : AppCompatActivity() {

    companion object {
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
        const val COLLECTION_ITEMS = "items"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_BARTER_HISTORY = "barter_history"
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.barter_page)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val barterProductId = intent.getStringExtra("BARTER_PRODUCT_ID") ?: ""
        val barterProductName = intent.getStringExtra("BARTER_PRODUCT_NAME") ?: ""
        val barterProductImage = intent.getStringExtra("BARTER_PRODUCT_IMAGE") ?: ""
        val selectedProductId = intent.getStringExtra("SELECTED_PRODUCT_ID") ?: ""
        val selectedProductName = intent.getStringExtra("SELECTED_PRODUCT_NAME") ?: ""
        val selectedProductImage = intent.getStringExtra("SELECTED_PRODUCT_IMAGE_URL") ?: ""
        val selectedProductUserId = intent.getStringExtra("SELECTED_PRODUCT_USER_ID") ?: ""
        val ownQuantity = intent.getIntExtra("QUANTITY_OWN", 0)
        val otherQuantity = intent.getIntExtra("QUANTITY_OTHER", 0)

        Log.d("BarterActivity", "Intent Data: barterProductId=$barterProductId, selectedProductId=$selectedProductId")

        if (barterProductId.isEmpty() || selectedProductId.isEmpty()) {
            Log.e("BarterActivity", "Data produk tidak lengkap")
            Toast.makeText(this, "Data produk tidak lengkap", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val backButton = findViewById<ImageView>(R.id.back_barter)
        val confirmButton = findViewById<Button>(R.id.btn_confirm_barter)
        val selectedProductImageView = findViewById<ImageView>(R.id.imageView20)
        val barterProductImageView = findViewById<ImageView>(R.id.imageView22)
        val selectedProductNameTextView = findViewById<TextView>(R.id.textView24)
        val barterProductNameTextView = findViewById<TextView>(R.id.textView20)
        val selectedProductSellerTextView = findViewById<TextView>(R.id.textView25)
        val barterProductSellerTextView = findViewById<TextView>(R.id.textView21)
        val addressTextView = findViewById<TextView>(R.id.textView32)
        val ownQuantityTextView = findViewById<TextView>(R.id.textView_own_quantity)
        val otherQuantityTextView = findViewById<TextView>(R.id.textView_other_quantity)

        ownQuantityTextView.text = "Jumlah: $ownQuantity"
        otherQuantityTextView.text = "Jumlah Produk Lain: $otherQuantity"

        selectedProductNameTextView.text = "Produk: $selectedProductName"
        Glide.with(this).load(selectedProductImage).into(selectedProductImageView)

        val currentUserId = auth.currentUser?.uid ?: ""

        // Ambil informasi pemilik barang yang dipilih
        firestore.collection(COLLECTION_USERS).document(selectedProductUserId)
            .get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    val selectedProductSeller = userDocument.getString("name") ?: "Pemilik Tidak Diketahui"
                    selectedProductSellerTextView.text = "Pemilik: $selectedProductSeller"
                } else {
                    selectedProductSellerTextView.text = "Pemilik: Tidak Diketahui"
                    Log.e("BarterActivity", "Pengguna tidak ditemukan untuk selectedProductUserId=$selectedProductUserId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("BarterActivity", "Gagal mengambil data pemilik barang yang dipilih: ${e.message}")
                selectedProductSellerTextView.text = "Pemilik: Gagal Dimuat"
            }

        // Ambil informasi pemilik barang barter
        firestore.collection(COLLECTION_ITEMS).document(barterProductId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val barterProductUserId = document.getString("userId") ?: ""
                    val barterProductAddress = document.getString("alamat") ?: "Alamat tidak tersedia"

                    barterProductNameTextView.text = "Produk: $barterProductName"
                    addressTextView.text = barterProductAddress
                    Glide.with(this).load(barterProductImage).into(barterProductImageView)

                    Log.d("BarterActivity", "Barter Product User ID: $barterProductUserId")

                    if (barterProductUserId.isNotEmpty()) {
                        firestore.collection(COLLECTION_USERS).document(barterProductUserId)
                            .get()
                            .addOnSuccessListener { userDocument ->
                                if (userDocument.exists()) {
                                    val barterProductSeller = userDocument.getString("name") ?: "Pemilik Tidak Diketahui"
                                    barterProductSellerTextView.text = "Pemilik: $barterProductSeller"
                                } else {
                                    barterProductSellerTextView.text = "Pemilik: Tidak Diketahui"
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("BarterActivity", "Gagal mengambil data pengguna untuk barterProductUserId: ${e.message}")
                                barterProductSellerTextView.text = "Pemilik: Gagal Dimuat"
                            }
                    } else {
                        barterProductSellerTextView.text = "Pemilik: Tidak Diketahui"
                        Log.e("BarterActivity", "User ID pemilik barang barter tidak ditemukan.")
                    }
                } else {
                    Log.e("BarterActivity", "Dokumen barang barter tidak ditemukan.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("BarterActivity", "Gagal memuat produk barter: ${e.message}")
            }

        backButton.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                onBackPressed()
            }
        }

        confirmButton.setOnClickListener {
            if (currentUserId.isEmpty()) {
                Toast.makeText(this, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firestore.collection(COLLECTION_ITEMS).document(barterProductId)
                .get()
                .addOnSuccessListener { barterProductDocument ->
                    if (barterProductDocument.exists()) {
                        val barterProductAvailableQuantity = (barterProductDocument.get("jumlah") as? Long)?.toInt() ?: 0

                        firestore.collection(COLLECTION_ITEMS).document(selectedProductId)
                            .get()
                            .addOnSuccessListener { selectedProductDocument ->
                                if (selectedProductDocument.exists()) {
                                    val selectedProductAvailableQuantity = (selectedProductDocument.get("jumlah") as? Long)?.toInt() ?: 0

                                    if (ownQuantity <= selectedProductAvailableQuantity && otherQuantity <= barterProductAvailableQuantity) {
                                        val updatedSelectedProductQuantity = selectedProductAvailableQuantity - ownQuantity
                                        val updatedBarterProductQuantity = barterProductAvailableQuantity - otherQuantity

                                        firestore.collection(COLLECTION_ITEMS).document(selectedProductId)
                                            .update("jumlah", updatedSelectedProductQuantity)
                                            .addOnSuccessListener {
                                                firestore.collection(COLLECTION_ITEMS).document(barterProductId)
                                                    .update("jumlah", updatedBarterProductQuantity)
                                                    .addOnSuccessListener {
                                                        val barterHistoryData = mapOf(
                                                            "barterProductId" to barterProductId,
                                                            "barterProductName" to barterProductName,
                                                            "barterProductImage" to barterProductImage,
                                                            "barterProductOwner" to barterProductSellerTextView.text.toString(),
                                                            "selectedProductId" to selectedProductId,
                                                            "selectedProductName" to selectedProductName,
                                                            "selectedProductImage" to selectedProductImage,
                                                            "selectedProductOwner" to selectedProductSellerTextView.text.toString(),
                                                            "userId" to currentUserId,
                                                            "address" to addressTextView.text.toString(),
                                                            "quantityOwn" to ownQuantity,
                                                            "quantityOther" to otherQuantity,
                                                            "timestamp" to System.currentTimeMillis()
                                                        )

                                                        firestore.collection(COLLECTION_BARTER_HISTORY)
                                                            .add(barterHistoryData)
                                                            .addOnSuccessListener {
                                                                val fragment = DoneFragment().apply {
                                                                    arguments = Bundle().apply {
                                                                        putString("barterProductName", barterProductName)
                                                                        putString("selectedProductName", selectedProductName)
                                                                        putString("barterProductImage", barterProductImage)
                                                                        putString("selectedProductImage", selectedProductImage)
                                                                    }
                                                                }

                                                                supportFragmentManager.beginTransaction()
                                                                    .replace(R.id.fragment_container, fragment)
                                                                    .addToBackStack(null)
                                                                    .commit()

                                                                if (checkNotificationPermission()) {
                                                                    showNotification("Produk Barter", "Produk Anda berhasil ditukar!")
                                                                    navigateToHomeActivity()
                                                                } else {
                                                                    requestNotificationPermission()
                                                                }
                                                            }
                                                    }
                                            }
                                    } else {
                                        Toast.makeText(this, "Jumlah produk tidak mencukupi untuk barter", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                    }
                }
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
            return
        }
        NotificationManagerCompat.from(this).notify(1, notification)
    }

    private fun navigateToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}