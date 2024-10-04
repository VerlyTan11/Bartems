package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        //Handle click event untuk textview gotologin
        val goToLogin = findViewById<TextView>(R.id.gotologin)
        goToLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        //Handle click event untuk button login
        val btnLogin = findViewById<Button>(R.id.btn_regist)
        btnLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}