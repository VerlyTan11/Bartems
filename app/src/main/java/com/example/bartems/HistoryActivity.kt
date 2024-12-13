package com.example.bartems

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bartems.model.BarterHistory
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

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
            // Query pertama: untuk riwayat di mana currentUserId adalah userId
            val userHistoryQuery = firestore.collection("barter_history")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()

            // Query kedua: untuk riwayat di mana currentUserId adalah partnerUserId
            val partnerHistoryQuery = firestore.collection("barter_history")
                .whereEqualTo("partnerUserId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()

            // Jalankan kedua query secara bersamaan
            Tasks.whenAllComplete(userHistoryQuery, partnerHistoryQuery).addOnSuccessListener { tasks ->
                historyList.clear()

                // Gabungkan hasil kedua query
                tasks.forEach { task ->
                    if (task.isSuccessful) {
                        val snapshots = task.result as? QuerySnapshot
                        snapshots?.forEach { document ->
                            val history = document.toObject(BarterHistory::class.java)
                            if (history != null && historyList.none { it.id == document.id }) {
                                history.id = document.id
                                historyList.add(history)
                            }
                        }
                    }
                }

                // Update UI dengan data terbaru
                if (historyList.isNotEmpty()) {
                    adapter.updateData(historyList)
                } else {
                    Toast.makeText(this, "Tidak ada riwayat barter ditemukan", Toast.LENGTH_SHORT).show()
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