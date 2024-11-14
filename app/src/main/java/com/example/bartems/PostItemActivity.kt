package com.example.bartems

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class PostItemActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    private val cameraPermissionRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            imageUri = result.data?.data
            findViewById<ImageView>(R.id.gambar_product).setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_item)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val selectedAddress = intent.getStringExtra("selectedAddress")
        val alamatEditText = findViewById<TextInputLayout>(R.id.alamat)
        selectedAddress?.let {
            alamatEditText.editText?.setText(it)
        }

        findViewById<ImageView>(R.id.back_post_item).setOnClickListener {
            val intent = if (intent.hasExtra("isFromProfile") && intent.getBooleanExtra("isFromProfile", false)) {
                Intent(this@PostItemActivity, ProfileActivity::class.java)
            } else {
                Intent(this@PostItemActivity, HomeActivity::class.java)
            }
            startActivity(intent)
            finish()
        }

        findViewById<TextView>(R.id.gotomap).setOnClickListener {
            startActivityForResult(Intent(this, MapActivity::class.java), MAP_REQUEST_CODE)
        }

        findViewById<ImageView>(R.id.gambar_product).setOnClickListener {
            showImageSourceOptions()
        }

        findViewById<Button>(R.id.btn_simpan_edit_product).setOnClickListener {
            if (imageUri != null) {
                saveItemWithImage(imageUri!!)
            } else {
                Toast.makeText(this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showImageSourceOptions() {
        val options = arrayOf("Ambil Foto", "Pilih dari Galeri")
        android.app.AlertDialog.Builder(this)
            .setTitle("Pilih Sumber Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            cameraPermissionRequestLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun saveItemWithImage(imageUri: Uri) {
        uploadImageToStorage(imageUri) { imageUrl ->
            if (imageUrl != null) {
                saveItemToFirestore(imageUrl)
            } else {
                Toast.makeText(this, "Gagal mendapatkan URL gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToStorage(imageUri: Uri, callback: (String?) -> Unit) {
        val storageRef = storage.reference.child("product_images/${UUID.randomUUID()}.jpg")
        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengunggah gambar", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    private fun saveItemToFirestore(imageUrl: String) {
        val namaProduk = findViewById<TextInputLayout>(R.id.nama_produk).editText?.text.toString()
        val catatan = findViewById<TextInputLayout>(R.id.catatan).editText?.text.toString()
        val berat = findViewById<TextInputLayout>(R.id.berat).editText?.text.toString()
        val jumlah = findViewById<TextInputLayout>(R.id.jumlah).editText?.text.toString()
        val alamat = findViewById<TextInputLayout>(R.id.alamat).editText?.text.toString()
        val noRumah = findViewById<TextInputLayout>(R.id.no_rumah).editText?.text.toString()
        val kodePos = findViewById<TextInputLayout>(R.id.kode_pos).editText?.text.toString()

        val itemData = hashMapOf(
            "nama_produk" to namaProduk,
            "catatan" to catatan,
            "berat" to berat,
            "jumlah" to jumlah,
            "alamat" to alamat,
            "no_rumah" to noRumah,
            "kode_pos" to kodePos,
            "userId" to auth.currentUser?.uid,
            "imageUrl" to imageUrl,
            "timestamp" to Timestamp.now()  // Pastikan timestamp disertakan
        )

        firestore.collection("items")
            .add(itemData) // Gunakan .add() hanya untuk menambah produk baru
            .addOnSuccessListener {
                Toast.makeText(this, "Item berhasil disimpan", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("isFromProfile", true)  // Jika perlu mengirim data tambahan
                startActivity(intent)
                finish()  // Menutup PostItemActivity agar tidak kembali ke halaman ini
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageView = findViewById<ImageView>(R.id.gambar_product)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val photo = data.extras?.get("data") as? Bitmap
            imageView.setImageBitmap(photo)
            imageUri = getImageUriFromBitmap(photo)
        } else if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK) {
            val latitude = data?.getDoubleExtra("latitude", 0.0)
            val longitude = data?.getDoubleExtra("longitude", 0.0)
            Toast.makeText(this, "Selected location: $latitude, $longitude", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap?): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "TempImage", null)
        return Uri.parse(path)
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val MAP_REQUEST_CODE = 200
    }
}