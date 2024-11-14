package com.example.bartems.model

import com.google.firebase.Timestamp

data class Product(
    var id: String = "",           // ID produk
    val nama_produk: String = "",
    val catatan: String = "",
    val berat: String = "",
    val jumlah: String = "",
    val alamat: String = "",
    val kode_pos: String = "",
    val no_rumah: String? = null,
    val imageUrl: String = "",
    val userId: String = "",
    val tersedia: Boolean = true,
    val timestamp: Timestamp? = null  // Pastikan timestamp ada
)