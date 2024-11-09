package com.example.bartems

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore

class PostItemActivity : AppCompatActivity() {

    private val cameraPermissionRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            // Tangani kasus ketika izin tidak diberikan
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_item)

        // Ambil data alamat yang diterima dari MapActivity
        val selectedAddress = intent.getStringExtra("selectedAddress")
        val alamatEditText = findViewById<TextInputLayout>(R.id.alamat) // Field alamat di layout PostItemActivity

        // Jika alamat tidak null, tampilkan di EditText
        selectedAddress?.let {
            alamatEditText.editText?.setText(it)
        }

        // Inisialisasi Firestore
        val firestore = FirebaseFirestore.getInstance()

        // Tangani event klik untuk tombol kembali
        val backPostItem = findViewById<ImageView>(R.id.back_post_item)
        backPostItem.setOnClickListener {
            val intent = intent
            if (intent.hasExtra("isFromProfile") && intent.getBooleanExtra("isFromProfile", false)) {
                startActivity(Intent(this@PostItemActivity, ProfileActivity::class.java))
            } else {
                startActivity(Intent(this@PostItemActivity, HomeActivity::class.java))
            }
            finish()
        }

        val goToMap = findViewById<TextView>(R.id.gotomap)
        goToMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivityForResult(intent, MAP_REQUEST_CODE)
        }

        // Tangani event klik untuk membuka kamera
        val imageView = findViewById<ImageView>(R.id.gambar_product)
        imageView.setOnClickListener {
            checkCameraPermission()
        }

        // Tangani event klik untuk tombol Posting
        val btnSimpanEditProduct = findViewById<Button>(R.id.btn_simpan_edit_product)
        btnSimpanEditProduct.setOnClickListener {
            saveItemToFirestore(firestore)
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionRequestLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    private fun saveItemToFirestore(firestore: FirebaseFirestore) {
        // Ambil TextInputEditText dari masing-masing TextInputLayout
        val namaProduk = findViewById<TextInputLayout>(R.id.nama_produk).editText?.text.toString()
        val catatan = findViewById<TextInputLayout>(R.id.catatan).editText?.text.toString()
        val berat = findViewById<TextInputLayout>(R.id.berat).editText?.text.toString()
        val jumlah = findViewById<TextInputLayout>(R.id.jumlah).editText?.text.toString()
        val alamat = findViewById<TextInputLayout>(R.id.alamat).editText?.text.toString()
        val noRumah = findViewById<TextInputLayout>(R.id.no_rumah).editText?.text.toString()
        val kodePos = findViewById<TextInputLayout>(R.id.kode_pos).editText?.text.toString()

        // Buat objek untuk disimpan ke Firestore
        val itemData = hashMapOf(
            "nama_produk" to namaProduk,
            "catatan" to catatan,
            "berat" to berat,
            "jumlah" to jumlah,
            "alamat" to alamat,
            "no_rumah" to noRumah,
            "kode_pos" to kodePos
        )

        // Simpan data ke Firestore
        firestore.collection("items")
            .add(itemData)
            .addOnSuccessListener { documentReference ->
                // Setelah berhasil menyimpan, arahkan ke ProfileActivity
                val intent = Intent(this@PostItemActivity, ProfileActivity::class.java)
                startActivity(intent)
                finish() // Tutup PostItemActivity jika tidak diperlukan lagi
            }
            .addOnFailureListener { e ->
                // Tangani kegagalan saat menyimpan
                Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK) {
            val latitude = data?.getDoubleExtra("latitude", 0.0)
            val longitude = data?.getDoubleExtra("longitude", 0.0)

            // Tampilkan atau simpan koordinat lokasi yang dipilih
            Toast.makeText(this, "Selected location: $latitude, $longitude", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val MAP_REQUEST_CODE = 200
    }
}