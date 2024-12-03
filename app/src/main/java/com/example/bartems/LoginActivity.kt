package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.airbnb.lottie.LottieAnimationView

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loadingAnimation: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Inisialisasi LottieAnimationView untuk loading
        loadingAnimation = findViewById(R.id.loadingAnimation)

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

        // Tampilkan animasi loading
        showLoadingAnimation()

        // Login dengan Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Sembunyikan animasi loading setelah login selesai
                hideLoadingAnimation()

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

    // Fungsi untuk menampilkan animasi loading
    private fun showLoadingAnimation() {
        loadingAnimation.visibility = View.VISIBLE
        loadingAnimation.playAnimation()  // Memulai animasi
    }

    // Fungsi untuk menyembunyikan animasi loading
    private fun hideLoadingAnimation() {
        loadingAnimation.cancelAnimation()  // Menghentikan animasi
        loadingAnimation.visibility = View.GONE  // Menyembunyikan animasi
    }
}
