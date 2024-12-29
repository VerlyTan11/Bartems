package com.example.bartems

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductImageAdapter(
    private val context: Context,
    private val productList: List<Product>
) : RecyclerView.Adapter<ProductImageAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.product_image)

        init {
            // Menangani klik pada item
            itemView.setOnClickListener {
                val product = productList[adapterPosition]
                val productId = product.id

                Log.d("ProductImageAdapter", "Product ID: $productId")
                if (productId.isNotEmpty()) {
                    // Membuka EditProductActivity dan mengirimkan productId
                    val intent = Intent(context, EditProductActivity::class.java)
                    intent.putExtra("PRODUCT_ID", productId) // Mengirimkan ID produk ke EditProductActivity
                    context.startActivity(intent)
                } else {
                    Log.e("ProductImageAdapter", "Product ID is empty or null!")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        Glide.with(context)
            .load(product.imageUrl)
            .into(holder.productImage)
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}