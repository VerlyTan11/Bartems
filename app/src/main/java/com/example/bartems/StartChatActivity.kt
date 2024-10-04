package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class StartChatActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_chat)

        // Handle click event untuk btn_confirm_chat
        val btnConfirmChat = findViewById<Button>(R.id.btn_confirm_chat)
        btnConfirmChat.setOnClickListener {
            val intent = Intent(this@StartChatActivity, ChatActivity::class.java)
            startActivity(intent)
        }

        // Handle click event untuk imageview back_from_chat
        val backFromChat = findViewById<ImageView>(R.id.back_from_chat)
        backFromChat.setOnClickListener {
            val intent = Intent(this@StartChatActivity, DetailProductActivity::class.java)
            startActivity(intent)
        }
    }
}