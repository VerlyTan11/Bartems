package com.example.bartems

data class BarterHistory(
    var id: String? = null,
    val barterProductId: String? = null,
    val barterProductImage: String? = null,
    val barterProductName: String? = null,
    var barterProductOwner: String? = null, // Nama pemilik barang barter
    var barterProductOwnerPhone: String? = null, // Nomor telepon pemilik barang barter
    val barterProductOwnerAddress: String? = null, // Alamat pemilik barang barter
    val selectedProductId: String? = null,
    val selectedProductImage: String? = null,
    val selectedProductName: String? = null,
    var selectedProductOwner: String? = null, // Nama pemilik barang yang dipilih
    var selectedProductOwnerPhone: String? = null, // Nomor telepon pemilik barang yang dipilih
    val selectedProductOwnerAddress: String? = null, // Alamat pemilik barang yang dipilih
    val quantityRequested: Int? = null,
    val quantityReceived: Int? = null,
    val timestamp: Long? = null
)