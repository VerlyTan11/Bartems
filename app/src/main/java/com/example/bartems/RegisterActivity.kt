package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    // Inisialisasi FirebaseAuth dan Firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        // Inisialisasi FirebaseAuth dan Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Handle click event untuk TextView goToLogin
        val goToLogin = findViewById<TextView>(R.id.gotologin)
        goToLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        // Handle click event untuk Button Register
        val btnRegister = findViewById<Button>(R.id.btn_regist)
        btnRegister.setOnClickListener {
            // Ambil data dari TextInputEditText
            val name = findViewById<TextInputEditText>(R.id.nama_edittext).text.toString()
            val phone = findViewById<TextInputEditText>(R.id.phone_regist_edittext).text.toString()
            val email = findViewById<TextInputEditText>(R.id.email_regist_edittext).text.toString()
            val password = findViewById<TextInputEditText>(R.id.password_regist_edittext).text.toString()
            val confirmPassword = findViewById<TextInputEditText>(R.id.confirm_password_edittext).text.toString()

            // Panggil fungsi registerUser
            registerUser(name, phone, email, password, confirmPassword)
        }
    }

    // Fungsi untuk mendaftarkan user ke Firebase Authentication
    private fun registerUser(name: String, phone: String, email: String, password: String, confirmPassword: String) {
        // Validasi input
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Password dan Confirm Password harus sama", Toast.LENGTH_SHORT).show()
            return
        }

        // Mendaftarkan user dengan Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Pendaftaran berhasil, simpan data tambahan ke Firestore
                    val userId = auth.currentUser?.uid // Dapatkan ID pengguna yang baru dibuat
                    val user = hashMapOf(
                        "name" to name,
                        "phone" to phone,
                        "email" to email
                    )

                    // Simpan data ke Firestore
                    firestore.collection("users")
                        .document(userId ?: "")
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Pendaftaran dan penyimpanan data berhasil!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Jika gagal, tampilkan pesan kesalahan
                    Toast.makeText(this, "Pendaftaran gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}