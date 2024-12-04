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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Inisialisasi komponen UI
        recyclerView = findViewById(R.id.recyclerView_history)
        backButton = findViewById(R.id.back_button)

        // Atur RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BarterHistoryAdapter(this, emptyList())
        recyclerView.adapter = adapter

        // Inisialisasi Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Load data riwayat barter
        loadHistoryData()

        // Tombol kembali
        backButton.setOnClickListener { finish() }
    }

    private fun loadHistoryData() {
        val currentUserId = auth.currentUser?.uid

        if (currentUserId != null) {
            firestore.collection("barter_history")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { snapshots ->
                    historyList.clear()
                    for (document in snapshots.documents) {
                        val history = document.toObject(BarterHistory::class.java)

                        if (history != null) {
                            // Periksa apakah pemilik produk yang dipilih perlu diubah menjadi "Milik Saya"
                            if (history.selectedProductOwner == "Pengguna Tidak Diketahui" &&
                                history.userId == currentUserId
                            ) {
                                history.selectedProductOwner = "Milik Saya"
                            }

                            Log.d("HistoryActivity", "Data ditemukan: $history")
                            historyList.add(history)
                        }
                    }

                    // Perbarui RecyclerView dengan data baru
                    if (historyList.isNotEmpty()) {
                        Log.d("HistoryActivity", "Total data: ${historyList.size}")
                        adapter.updateData(historyList)
                    } else {
                        Log.w("HistoryActivity", "Tidak ada data history ditemukan.")
                        Toast.makeText(this, "Tidak ada proses barter ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HistoryActivity", "Gagal memuat history: ${e.message}")
                    Toast.makeText(this, "Gagal memuat history: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("HistoryActivity", "User ID tidak ditemukan.")
            Toast.makeText(this, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
        }
    }
}