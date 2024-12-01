package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class JumlahBarangActivity : AppCompatActivity() {

    private lateinit var productName: TextView
    private lateinit var inputQuantityOwn: EditText
    private lateinit var inputQuantityOther: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.jumlah_barang)

        productName = findViewById(R.id.text_selected_product_name)
        inputQuantityOwn = findViewById(R.id.input_quantity_own)
        inputQuantityOther = findViewById(R.id.input_quantity_other)
        btnSubmit = findViewById(R.id.btn_submit_quantity)

        val selectedProductId = intent.getStringExtra("SELECTED_PRODUCT_ID") ?: ""
        val selectedProductName = intent.getStringExtra("SELECTED_PRODUCT_NAME") ?: ""
        val barterProductId = intent.getStringExtra("BARTER_PRODUCT_ID") ?: ""
        val barterProductName = intent.getStringExtra("BARTER_PRODUCT_NAME") ?: ""

        productName.text = "Produk: $selectedProductName"

        btnSubmit.setOnClickListener {
            val ownQuantity = inputQuantityOwn.text.toString().toIntOrNull()
            val otherQuantity = inputQuantityOther.text.toString().toIntOrNull()

            if (ownQuantity == null || otherQuantity == null) {
                Toast.makeText(this, "Masukkan jumlah valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (ownQuantity < 0 || otherQuantity < 0) {
                Toast.makeText(this, "Jumlah tidak boleh negatif!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Contoh validasi stok (dummy, sesuaikan dengan implementasi backend)
            val ownStock = 10 // Misalnya dari database
            val otherStock = 5 // Misalnya dari database

            if (ownQuantity > ownStock || otherQuantity > otherStock) {
                Toast.makeText(this, "Jumlah melebihi stok tersedia!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, BarterActivity::class.java).apply {
                putExtra("BARTER_PRODUCT_ID", barterProductId)
                putExtra("BARTER_PRODUCT_NAME", barterProductName)
                putExtra("SELECTED_PRODUCT_ID", selectedProductId)
                putExtra("SELECTED_PRODUCT_NAME", selectedProductName)
                putExtra("OWN_QUANTITY", ownQuantity)
                putExtra("OTHER_QUANTITY", otherQuantity)
            }
            startActivity(intent)
        }
    }
}
