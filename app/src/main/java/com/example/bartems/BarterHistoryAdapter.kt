package com.example.bartems

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bartems.model.BarterHistory
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
        val selectedProductOwner: TextView = view.findViewById(R.id.text_selected_product_owner)
        val barterProductQuantity: TextView = view.findViewById(R.id.text_barter_product_quantity) // Tambahkan referensi untuk jumlah barang barter
        val selectedProductQuantity: TextView = view.findViewById(R.id.text_selected_product_quantity) // Tambahkan referensi untuk jumlah barang yang dipilih
        val address: TextView = view.findViewById(R.id.text_address)
        val timestamp: TextView = view.findViewById(R.id.text_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_barter_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = historyList[position]

        Log.d("BarterHistoryAdapter", "Mengatur item: $history")

        // Set data gambar produk barter
        Glide.with(context)
            .load(history.barterProductImage)
            .into(holder.barterProductImage)

        // Set data gambar produk yang dipilih
        Glide.with(context)
            .load(history.selectedProductImage)
            .into(holder.selectedProductImage)

        // Set nama dan pemilik produk barter
        holder.barterProductName.text = history.barterProductName
        holder.barterProductOwner.text = history.barterProductOwner ?: "Pemilik tidak diketahui"

        // Set nama dan pemilik produk yang dipilih
        holder.selectedProductName.text = history.selectedProductName
        holder.selectedProductOwner.text = history.selectedProductOwner ?: "Pemilik tidak diketahui"

        // Set jumlah barang barter
        holder.barterProductQuantity.text = "Jumlah: ${history.quantityOther ?: 0}"

        // Set jumlah barang yang dipilih
        holder.selectedProductQuantity.text = "Jumlah: ${history.quantityOwn ?: 0}"

        // Set alamat dan waktu transaksi
        holder.address.text = "Alamat: ${history.address ?: "Tidak tersedia"}"
        holder.timestamp.text = "Tanggal: ${convertTimestampToDate(history.timestamp)}"
    }

    override fun getItemCount(): Int = historyList.size

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