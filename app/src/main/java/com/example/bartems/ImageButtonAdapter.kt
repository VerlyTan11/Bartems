package com.example.bartems

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageButtonAdapter(private val items: List<ProductRecyclerList>, private val onClick: (ProductRecyclerList) -> Unit) :
    RecyclerView.Adapter<ImageButtonAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageButton: ImageButton = itemView.findViewById(R.id.itemImageButton)
        val textView: TextView = itemView.findViewById(R.id.itemTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_button, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = items[position]  // Get the ProductRecyclerList object

        // Set the product name to the TextView
        holder.textView.text = product.name

        // Load the image from the URL using Glide
        Glide.with(holder.itemView.context)
            .load(product.imageUrl)  // Load the image URL
            .into(holder.imageButton)  // Set the image to the ImageButton (or ImageView, depending on your layout)

        // Set click listener to pass the product to the onClick method
        holder.imageButton.setOnClickListener {
            onClick(product)  // Pass the whole product object to the onClick method
        }
    }

    override fun getItemCount(): Int = items.size
}
