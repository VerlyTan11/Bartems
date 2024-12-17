package com.example.bartems

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class PostItemActivity : AppCompatActivity() {

    private lateinit var loadingAnimation: LottieAnimationView
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

    private var savedInputValues: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_item)
        loadingAnimation = findViewById(R.id.loadingAnimation)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Retrieve the address from the intent
        val selectedAddress = intent.getStringExtra("selectedAddress")

        val alamatEditText = findViewById<TextInputLayout>(R.id.alamat)

        // Set the address in the EditText
        if (!selectedAddress.isNullOrEmpty()) {
            alamatEditText.editText?.setText(selectedAddress)
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
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("caller", "PostItemActivity")
            // Add other extras as needed


            // Mengirimkan data inputan ke MapActivity
            intent.putExtra("nama_produk", findViewById<TextInputLayout>(R.id.nama_produk).editText?.text.toString())
            intent.putExtra("catatan", findViewById<TextInputLayout>(R.id.catatan).editText?.text.toString())
            intent.putExtra("jumlah", findViewById<TextInputLayout>(R.id.jumlah).editText?.text.toString())
            intent.putExtra("berat", findViewById<TextInputLayout>(R.id.berat).editText?.text.toString())
            intent.putExtra("alamat", findViewById<TextInputLayout>(R.id.alamat).editText?.text.toString())
            intent.putExtra("no_rumah", findViewById<TextInputLayout>(R.id.no_rumah).editText?.text.toString())
            intent.putExtra("kode_pos", findViewById<TextInputLayout>(R.id.kode_pos).editText?.text.toString())

            // Kirimkan alamat yang sudah dipilih sebelumnya (jika ada)
            val selectedAddress = findViewById<TextInputLayout>(R.id.alamat).editText?.text.toString()
            intent.putExtra("selectedAddress", selectedAddress)

            startActivityForResult(intent, MAP_REQUEST_CODE)
        }


        findViewById<ImageView>(R.id.gambar_product).setOnClickListener {
            showImageSourceOptions()
        }

        findViewById<Button>(R.id.btn_simpan_edit_product).setOnClickListener {
            if (imageUri != null) {
                showLoadingAnimation()
                saveItemWithImage(imageUri!!)
            } else {
                Toast.makeText(this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("nama_produk", findViewById<TextInputLayout>(R.id.nama_produk).editText?.text.toString())
        outState.putString("catatan", findViewById<TextInputLayout>(R.id.catatan).editText?.text.toString())
        outState.putString("jumlah", findViewById<TextInputLayout>(R.id.jumlah).editText?.text.toString())
        outState.putString("berat", findViewById<TextInputLayout>(R.id.berat).editText?.text.toString())
        outState.putString("alamat", findViewById<TextInputLayout>(R.id.alamat).editText?.text.toString())
        outState.putString("no_rumah", findViewById<TextInputLayout>(R.id.no_rumah).editText?.text.toString())
        outState.putString("kode_pos", findViewById<TextInputLayout>(R.id.kode_pos).editText?.text.toString())
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        findViewById<TextInputLayout>(R.id.nama_produk).editText?.setText(savedInstanceState.getString("nama_produk"))
        findViewById<TextInputLayout>(R.id.catatan).editText?.setText(savedInstanceState.getString("catatan"))
        findViewById<TextInputLayout>(R.id.jumlah).editText?.setText(savedInstanceState.getString("jumlah"))
        findViewById<TextInputLayout>(R.id.berat).editText?.setText(savedInstanceState.getString("berat"))
        findViewById<TextInputLayout>(R.id.alamat).editText?.setText(savedInstanceState.getString("alamat"))
        findViewById<TextInputLayout>(R.id.no_rumah).editText?.setText(savedInstanceState.getString("no_rumah"))
        findViewById<TextInputLayout>(R.id.kode_pos).editText?.setText(savedInstanceState.getString("kode_pos"))
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
        val jumlahString = findViewById<TextInputLayout>(R.id.jumlah).editText?.text.toString()
        val alamat = findViewById<TextInputLayout>(R.id.alamat).editText?.text.toString()
        val noRumah = findViewById<TextInputLayout>(R.id.no_rumah).editText?.text.toString()
        val kodePos = findViewById<TextInputLayout>(R.id.kode_pos).editText?.text.toString()

        // Convert jumlah to an integer. If it's invalid, show a toast and return.
        val jumlah: Int = try {
            jumlahString.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Jumlah harus berupa angka", Toast.LENGTH_SHORT).show()
            return
        }

        val itemData = hashMapOf(
            "nama_produk" to namaProduk,
            "catatan" to catatan,
            "berat" to berat,
            "jumlah" to jumlah,  // Now it's an integer
            "alamat" to alamat,
            "no_rumah" to noRumah,
            "kode_pos" to kodePos,
            "userId" to auth.currentUser?.uid,
            "imageUrl" to imageUrl,
            "timestamp" to Timestamp.now()  // Ensure timestamp is included
        )

        firestore.collection("items")
            .add(itemData) // Use .add() to create a new product
            .addOnSuccessListener {
                Toast.makeText(this, "Item berhasil disimpan", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("isFromProfile", true)  // If you need to pass additional data
                startActivity(intent)
                finish()  // Close PostItemActivity so the user cannot return here
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Memproses hasil yang dikirimkan kembali dari MapActivity
    // Memproses hasil yang dikirimkan kembali dari MapActivity
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

        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Ambil data dari MapActivity
            val selectedAddress = data.getStringExtra("selectedAddress")
            val inputAddress = data.getStringExtra("inputAddress")

            // Ambil data lainnya
            val namaProduk = data.getStringExtra("nama_produk")
            val catatan = data.getStringExtra("catatan")
            val jumlah = data.getStringExtra("jumlah")
            val berat = data.getStringExtra("berat")
            val alamat = data.getStringExtra("alamat")
            val noRumah = data.getStringExtra("no_rumah")
            val kodePos = data.getStringExtra("kode_pos")

            // Set kembali nilai inputan ke dalam field
            findViewById<TextInputLayout>(R.id.nama_produk).editText?.setText(namaProduk)
            findViewById<TextInputLayout>(R.id.catatan).editText?.setText(catatan)
            findViewById<TextInputLayout>(R.id.jumlah).editText?.setText(jumlah)
            findViewById<TextInputLayout>(R.id.berat).editText?.setText(berat)
            findViewById<TextInputLayout>(R.id.alamat).editText?.setText(alamat)
            findViewById<TextInputLayout>(R.id.no_rumah).editText?.setText(noRumah)
            findViewById<TextInputLayout>(R.id.kode_pos).editText?.setText(kodePos)

            // Update alamat dengan alamat yang dipilih atau diinputkan
            findViewById<TextInputLayout>(R.id.alamat).editText?.setText(selectedAddress ?: inputAddress)
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