package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        //Handle click event untuk textview gotosignup
        val goToSignUp = findViewById<TextView>(R.id.gotosignup)
        goToSignUp.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        //Handle click event untuk button login
        val btnLogin = findViewById<Button>(R.id.btn_login)
        btnLogin.setOnClickListener {
            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}