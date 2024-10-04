package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ChatActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat)

        // Handle click event untuk imageview back_chat
        val backChat = findViewById<ImageView>(R.id.back_chat)
        backChat.setOnClickListener {
            val intent = Intent(this@ChatActivity, StartChatActivity::class.java)
            startActivity(intent)
        }
    }
}