package com.example.bartems

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ProductAdapter(
    private val products: List<ProductRecyclerList>,
    private val onItemClick: (ProductRecyclerList) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageButton: ImageButton = itemView.findViewById(R.id.itemImageButton)
        val textView: TextView = itemView.findViewById(R.id.itemTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_button, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        // Load gambar produk
        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            .apply(RequestOptions().placeholder(R.drawable.box).error(R.drawable.box))
            .into(holder.imageButton)

        // Tampilkan nama produk
        holder.textView.text = product.name

        // Tambahkan listener klik dengan debugging
        holder.imageButton.setOnClickListener {
            Log.d("ProductAdapter", "ImageButton clicked: ${product.name} (ID: ${product.id})")
            onItemClick(product) // Panggil callback ke HomeActivity
        }
    }

    override fun getItemCount(): Int = products.size
}