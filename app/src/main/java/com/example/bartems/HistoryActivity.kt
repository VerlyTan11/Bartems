package com.example.bartems

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bartems.model.BarterHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: ImageView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: BarterHistoryAdapter
    private val historyList = mutableListOf<BarterHistory>()
    private val transactionSet = mutableSetOf<String>() // Set untuk menyimpan ID unik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Inisialisasi UI
        recyclerView = findViewById(R.id.recyclerView_history)
        backButton = findViewById(R.id.back_button)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BarterHistoryAdapter(this, emptyList())
        recyclerView.adapter = adapter

        // Inisialisasi Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Load data riwayat barter
        loadHistoryData()

        backButton.setOnClickListener { finish() }
    }

    private fun loadHistoryData() {
        val currentUserId = auth.currentUser?.uid

        if (currentUserId != null) {
            // Query riwayat berdasarkan userIds yang mengandung currentUserId
            val historyQuery = firestore.collection("barter_history")
                .whereArrayContains("userIds", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()

            historyQuery.addOnSuccessListener { querySnapshot ->
                historyList.clear()
                transactionSet.clear() // Reset set transaksi untuk mencegah duplikasi

                querySnapshot?.forEach { document ->
                    val transactionId = document.getString("transactionId") ?: document.id
                    if (!transactionSet.contains(transactionId)) {
                        transactionSet.add(transactionId) // Tambahkan ID transaksi ke set

                        val barterProductOwnerId = document.getString("barterProductOwnerId") ?: ""
                        val selectedProductOwnerId = document.getString("selectedProductOwnerId") ?: ""

                        val history = BarterHistory(
                            id = document.id,
                            barterProductId = document.getString("barterProductId"),
                            barterProductImage = document.getString("barterProductImage"),
                            barterProductName = document.getString("barterProductName"),
                            barterProductOwner = null, // Akan diambil dari Firestore
                            barterProductOwnerPhone = document.getString("barterProductOwnerPhone"),
                            barterProductOwnerAddress = document.getString("barterProductOwnerAddress"),
                            selectedProductId = document.getString("selectedProductId"),
                            selectedProductImage = document.getString("selectedProductImage"),
                            selectedProductName = document.getString("selectedProductName"),
                            selectedProductOwner = null, // Akan diambil dari Firestore
                            selectedProductOwnerPhone = document.getString("selectedProductOwnerPhone"),
                            selectedProductOwnerAddress = document.getString("selectedProductOwnerAddress"),
                            quantityRequested = document.getLong("quantityRequested")?.toInt(),
                            quantityReceived = document.getLong("quantityReceived")?.toInt(),
                            timestamp = document.getLong("timestamp")
                        )

                        // Ambil nama pemilik berdasarkan ID dari koleksi users
                        firestore.collection("users").document(barterProductOwnerId)
                            .get()
                            .addOnSuccessListener { barterOwnerDoc ->
                                if (barterOwnerDoc.exists()) {
                                    history.barterProductOwner = barterOwnerDoc.getString("name")
                                }
                                firestore.collection("users")
                                    .document(selectedProductOwnerId)
                                    .get()
                                    .addOnSuccessListener { selectedOwnerDoc ->
                                        if (selectedOwnerDoc.exists()) {
                                            history.selectedProductOwner =
                                                selectedOwnerDoc.getString("name")
                                        }

                                        historyList.add(history)

                                        // Perbarui UI setelah semua data diambil
                                        if (historyList.size == transactionSet.size) {
                                            historyList.sortByDescending { it.timestamp }
                                            adapter.updateData(historyList)
                                        }
                                    }
                            }
                    }
                }

                // Periksa apakah historyList kosong
                if (historyList.isEmpty()) {
                    Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Log.e("HistoryActivity", "Gagal memuat riwayat: ${e.message}")
                Toast.makeText(this, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
        }
    }
}