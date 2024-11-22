package com.example.bartems

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bartems.model.Product

class PilihBarangAdapter(private val onItemClick: (Product) -> Unit) :
    RecyclerView.Adapter<PilihBarangAdapter.ProductViewHolder>() {

    private val productList = mutableListOf<Product>()

    fun setProducts(products: List<Product>) {
        productList.clear()
        productList.addAll(products)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_image, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = productList.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.product_image)

        fun bind(product: Product) {
            Glide.with(itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.box)
                .into(productImage)

            itemView.setOnClickListener {
                onItemClick(product)
            }
        }
    }
}