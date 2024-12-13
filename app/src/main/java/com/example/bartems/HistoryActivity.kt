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
                    val fetchTasks = mutableListOf<Task<DocumentSnapshot>>()

                    for (document in snapshots.documents) {
                        val history = document.toObject(BarterHistory::class.java)

                        if (history != null) {
                            // Ambil nama pemilik barang kanan (barterProductOwner)
                            val rightOwnerTask = firestore.collection("users")
                                .document(history.barterProductOwner ?: "")
                                .get()
                                .addOnSuccessListener { ownerSnapshot ->
                                    if (ownerSnapshot.exists()) {
                                        history.barterProductOwner =
                                            ownerSnapshot.getString("name") ?: "Pemilik tidak diketahui"
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("HistoryActivity", "Gagal memuat data pemilik barang kanan: ${e.message}")
                                }
                            fetchTasks.add(rightOwnerTask)

                            // Ambil nama pemilik barang kiri (selectedProductOwner)
                            val leftOwnerTask = firestore.collection("users")
                                .document(history.selectedProductOwner ?: "")
                                .get()
                                .addOnSuccessListener { ownerSnapshot ->
                                    if (ownerSnapshot.exists()) {
                                        history.selectedProductOwner =
                                            ownerSnapshot.getString("name") ?: "Pemilik tidak diketahui"
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("HistoryActivity", "Gagal memuat data pemilik barang kiri: ${e.message}")
                                }
                            fetchTasks.add(leftOwnerTask)

                            // Tambahkan ke daftar setelah semua task selesai
                            historyList.add(history)
                        }
                    }

                    // Tunggu semua task selesai sebelum memperbarui RecyclerView
                    Tasks.whenAllComplete(fetchTasks).addOnSuccessListener {
                        if (historyList.isNotEmpty()) {
                            adapter.updateData(historyList)
                        } else {
                            Log.w("HistoryActivity", "Tidak ada data history ditemukan.")
                            Toast.makeText(this, "Tidak ada proses barter ditemukan", Toast.LENGTH_SHORT).show()
                        }
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