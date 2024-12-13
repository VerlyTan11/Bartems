package com.example.bartems.model

data class BarterHistory(
    val id: String? = null,
    val address: String? = null,
    val barterProductId: String? = null,
    val barterProductImage: String? = null,
    val barterProductName: String? = null,
    var barterProductOwner: String? = null,
    val selectedProductId: String? = null,
    val selectedProductImage: String? = null,
    val selectedProductName: String? = null,
    var selectedProductOwner: String? = null,
    val userId: String? = null,
    val partnerUserId: String? = null,
    val timestamp: Long? = null,
    val quantityOwn: Int? = null,
    val quantityOther: Int? = null
)