package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class DetailProductActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_product)

        // Handle click event untuk gotobarter
        val goToBarterButton = findViewById<Button>(R.id.btn_gotobarter)
        goToBarterButton.setOnClickListener {
            val intent = Intent(this@DetailProductActivity, BarterActivity::class.java)
            startActivity(intent)
        }

        // Handle click event untuk gotochat_detail
        val goToChat = findViewById<ImageButton>(R.id.gotochat_detail)
        goToChat.setOnClickListener {
            val intent = Intent(this@DetailProductActivity, StartChatActivity::class.java)
            startActivity(intent)
        }
    }
}