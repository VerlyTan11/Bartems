package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    // Inisialisasi FirebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Handle click event untuk TextView goToSignUp
        val goToSignUp = findViewById<TextView>(R.id.gotosignup)
        goToSignUp.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Handle click event untuk Button Login
        val btnLogin = findViewById<Button>(R.id.btn_login)
        btnLogin.setOnClickListener {
            // Ambil email dan password dari TextInputEditText
            val email = findViewById<TextInputEditText>(R.id.email_edittext).text.toString()
            val password = findViewById<TextInputEditText>(R.id.password_edittext).text.toString()

            // Panggil fungsi loginUser
            loginUser(email, password)
        }
    }

    // Fungsi untuk login user dengan email dan password
    private fun loginUser(email: String, password: String) {
        // Validasi email dan password tidak boleh kosong
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        // Login dengan Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login berhasil, arahkan ke HomeActivity
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Jika login gagal, tampilkan pesan kesalahan
                    Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}