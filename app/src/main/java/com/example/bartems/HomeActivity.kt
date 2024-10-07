package com.example.bartems

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Ambil nama pengguna dari Intent, jika ada
        val userName = intent.getStringExtra("USER_NAME") // Ambil nama pengguna dari Intent
        val userNameTextView = findViewById<TextView>(R.id.textView16)

        if (userName != null) {
            userNameTextView.text = userName // Setel teks ke TextView jika nama ada
        } else {
            // Ambil nama pengguna dari database jika tidak ada dari Intent
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                FirebaseDatabase.getInstance().getReference("Users").child(userId).get().addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {
                        val name = dataSnapshot.child("name").value.toString() // Ambil nama dari database
                        userNameTextView.text = name // Setel teks ke TextView
                    } else {
                        // Tindakan jika data tidak ada
                        userNameTextView.text = "Nama tidak ditemukan" // Atau tindakan lain
                    }
                }.addOnFailureListener {
                    // Tindakan jika gagal mengambil data
                    userNameTextView.text = "Error mengambil nama"
                }
            } else {
                userNameTextView.text = "User ID tidak ditemukan"
            }
        }

        // Handle click event untuk ImageButton gotoprofile
        val goToProfileButton = findViewById<ImageButton>(R.id.gotoprofile)
        goToProfileButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Handle click event for add_item ImageView
        val addItem = findViewById<ImageView>(R.id.add_item)
        addItem.setOnClickListener {
            val intent = Intent(this@HomeActivity, PostItemActivity::class.java)
            startActivity(intent)
        }

        // Handle click event untuk ImageButton cth_produk
        val cthProduct = findViewById<ImageButton>(R.id.cth_produk)
        cthProduct.setOnClickListener {
            val intent = Intent(this@HomeActivity, DetailProductActivity::class.java)
            intent.putExtra("USER_NAME", userNameTextView.text.toString()) // Kirim nama pengguna ke DetailProductActivity
            startActivity(intent)
        }
    }
}