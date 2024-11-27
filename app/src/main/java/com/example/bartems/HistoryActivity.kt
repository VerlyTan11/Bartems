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
    private val historyList = mutableListOf<BarterHistory>()
    private lateinit var adapter: BarterHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.recyclerView_history)
        backButton = findViewById(R.id.back_button)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BarterHistoryAdapter(this, emptyList()) // Initialize with empty data
        recyclerView.adapter = adapter

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadHistoryData()

        backButton.setOnClickListener {
            finish()
        }
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
                            Log.d("HistoryActivity", "Data ditemukan: $history")
                            historyList.add(history)
                        }
                    }

                    if (historyList.isNotEmpty()) {
                        Log.d("HistoryActivity", "Total data: ${historyList.size}")
                        adapter.updateData(historyList) // Update the adapter
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