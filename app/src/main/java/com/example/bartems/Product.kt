package com.example.bartems.model

data class Product(
    var id: String = "",           // Properti untuk ID produk
    val namaProduk: String = "",
    val catatan: String = "",
    val berat: String = "",
    val jumlah: String = "",
    val alamat: String = "",
    val kodePos: String = "",
    val imageUrl: String = "",
    val userId: String = ""
)