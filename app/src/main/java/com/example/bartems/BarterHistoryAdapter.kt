package com.example.bartems

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class BarterHistoryAdapter(
    private val context: Context,
    private var historyList: List<BarterHistory>
) : RecyclerView.Adapter<BarterHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val barterProductImage: ImageView = view.findViewById(R.id.image_barter_product)
        val selectedProductImage: ImageView = view.findViewById(R.id.image_selected_product)
        val barterProductName: TextView = view.findViewById(R.id.text_barter_product_name)
        val selectedProductName: TextView = view.findViewById(R.id.text_selected_product_name)
        val barterProductOwner: TextView = view.findViewById(R.id.text_barter_product_owner)
        val barterProductOwnerPhone: TextView = view.findViewById(R.id.text_barter_product_owner_phone)
        val barterProductOwnerAddress: TextView = view.findViewById(R.id.text_barter_product_owner_address) // Pastikan ID sesuai di XML
        val selectedProductOwner: TextView = view.findViewById(R.id.text_selected_product_owner)
        val selectedProductOwnerPhone: TextView = view.findViewById(R.id.text_selected_product_owner_phone)
        val selectedProductOwnerAddress: TextView = view.findViewById(R.id.text_selected_product_owner_address) // Pastikan ID sesuai di XML
        val barterProductQuantity: TextView = view.findViewById(R.id.text_barter_product_quantity)
        val selectedProductQuantity: TextView = view.findViewById(R.id.text_selected_product_quantity)
        val timestamp: TextView = view.findViewById(R.id.text_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_barter_history, parent, false)
        return HistoryViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = historyList[position]

        // Set data gambar produk
        Glide.with(context)
            .load(history.selectedProductImage)
            .into(holder.selectedProductImage)

        Glide.with(context)
            .load(history.barterProductImage)
            .into(holder.barterProductImage)

        // Set data barang selected
        holder.selectedProductName.text = "Nama Barang: ${history.selectedProductName}"
        holder.selectedProductQuantity.text = "Jumlah: ${history.quantityRequested}"
        holder.selectedProductOwner.text = "Pemilik: ${history.selectedProductOwner ?: "Tidak Diketahui"}"
        holder.selectedProductOwnerPhone.text = "No HP: ${history.selectedProductOwnerPhone ?: "Tidak Tersedia"}"
        holder.selectedProductOwnerAddress.text = "Alamat: ${history.selectedProductOwnerAddress ?: "Tidak Tersedia"}"

        // Set data barang barter
        holder.barterProductName.text = "Nama Barang: ${history.barterProductName}"
        holder.barterProductQuantity.text = "Jumlah: ${history.quantityReceived}"
        holder.barterProductOwner.text = "Pemilik: ${history.barterProductOwner ?: "Tidak Diketahui"}"
        holder.barterProductOwnerPhone.text = "No HP: ${history.barterProductOwnerPhone ?: "Tidak Tersedia"}"
        holder.barterProductOwnerAddress.text = "Alamat: ${history.barterProductOwnerAddress ?: "Tidak Tersedia"}"

        // Set data tanggal di tengah bawah
        holder.timestamp.text = "Tanggal: ${convertTimestampToDate(history.timestamp)}"
    }

    override fun getItemCount(): Int = historyList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newHistoryList: List<BarterHistory>) {
        historyList = newHistoryList
        notifyDataSetChanged()
    }

    private fun convertTimestampToDate(timestamp: Long?): String {
        return if (timestamp != null) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else {
            "Tidak diketahui"
        }
    }
}