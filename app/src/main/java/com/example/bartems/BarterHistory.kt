package com.example.bartems.model

data class BarterHistory(
    var id: String? = null,
    val address: String? = null,
    val barterProductId: String? = null,
    val barterProductImage: String? = null,
    val barterProductName: String? = null,
    var barterProductOwner: String? = null, // ID pemilik barang barter
    var barterProductOwnerPhone: String? = null, // Nomor telepon pemilik barang barter
    val selectedProductId: String? = null,
    val selectedProductImage: String? = null,
    val selectedProductName: String? = null,
    var selectedProductOwner: String? = null, // ID pemilik barang yang dipilih
    var selectedProductOwnerPhone: String? = null, // Nomor telepon pemilik barang yang dipilih
    val userId: String? = null,
    val partnerUserId: String? = null,
    val timestamp: Long? = null,
    val quantityOwn: Int? = null,
    val quantityOther: Int? = null
)