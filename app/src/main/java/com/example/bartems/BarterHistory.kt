package com.example.bartems.model

data class BarterHistory(
    val id: String? = null, // ID dokumen Firestore
    val address: String? = null, // Alamat transaksi
    val barterProductId: String? = null, // ID produk barter
    val barterProductImage: String? = null, // URL gambar produk barter
    val barterProductName: String? = null, // Nama produk barter
    val barterProductOwner: String? = null, // Pemilik produk barter
    val selectedProductId: String? = null, // ID produk yang dipilih
    val selectedProductImage: String? = null, // URL gambar produk yang dipilih
    val selectedProductName: String? = null, // Nama produk yang dipilih
    val selectedProductOwner: String? = null, // Pemilik produk yang dipilih (diisi dengan userId)
    val userId: String? = null, // User ID pemilik transaksi (pengguna yang login)
    val timestamp: Long? = null // Waktu transaksi
)