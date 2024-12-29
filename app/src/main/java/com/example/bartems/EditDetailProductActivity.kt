package com.example.bartems

import android.Manifest
import android.annotation.SuppressLint
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
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

@Suppress("DEPRECATION")
class EditDetailProductActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private var imageUri: Uri? = null
    private var oldImageUrl: String = ""
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var productNameInput: TextInputLayout
    private lateinit var productQuantityInput: TextInputLayout
    private lateinit var productNoteInput: TextInputLayout
    private lateinit var productAddressInput: TextInputLayout
    private lateinit var productHouseNumberInput: TextInputLayout
    private lateinit var productPostalCodeInput: TextInputLayout
    private lateinit var productWeightInput: TextInputLayout
    private lateinit var productImageView: ImageView
    private lateinit var btnUpdateProduct: Button
    private lateinit var btnBack: ImageView
    private lateinit var btnSelectLocation: TextView

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
            productImageView.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_detail_product)
        loadingAnimation = findViewById(R.id.loadingAnimation)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        productNameInput = findViewById(R.id.nama_produk)
        productQuantityInput = findViewById(R.id.jumlah)
        productNoteInput = findViewById(R.id.catatan)
        productAddressInput = findViewById(R.id.alamat)
        productHouseNumberInput = findViewById(R.id.no_rumah)
        productPostalCodeInput = findViewById(R.id.kode_pos)
        productWeightInput = findViewById(R.id.berat)
        productImageView = findViewById(R.id.gambar_product)
        btnUpdateProduct = findViewById(R.id.btn_simpan_edit_product)
        btnBack = findViewById(R.id.back_edit_product)
        btnSelectLocation = findViewById(R.id.gotomap)

        val productId = intent.getStringExtra("PRODUCT_ID")
        if (productId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid Product ID", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            loadProductData(productId)
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnUpdateProduct.setOnClickListener {
            val productName = productNameInput.editText?.text.toString()
            val productQuantity = productQuantityInput.editText?.text.toString().toInt() // Ambil langsung sebagai Int
            val productNote = productNoteInput.editText?.text.toString()
            val address = productAddressInput.editText?.text.toString()
            val houseNumber = productHouseNumberInput.editText?.text.toString()
            val postalCode = productPostalCodeInput.editText?.text.toString()
            val weight = productWeightInput.editText?.text.toString()

            if (productId != null) {
                if (imageUri != null) {
                    updateProductWithImage(productId, productName, productQuantity, productNote, address, houseNumber, postalCode, weight, imageUri!!)
                } else {
                    updateProductWithoutImage(productId, productName, productQuantity, productNote, address, houseNumber, postalCode, weight, oldImageUrl)
                }
            }
        }

        productImageView.setOnClickListener {
            showImageSourceOptions()
        }

        btnSelectLocation.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("caller", "EditDetailProductActivity") // Pass the caller information
            startActivityForResult(intent, MAP_REQUEST_CODE)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadProductData(productId: String) {
        firestore.collection("items").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val product = document.toObject(Product::class.java)
                    if (product != null) {
                        productNameInput.editText?.setText(product.nama_produk)
                        productQuantityInput.editText?.setText(product.jumlah.toString()) // jumlah langsung sebagai Integer
                        productNoteInput.editText?.setText(product.catatan)
                        productAddressInput.editText?.setText(product.alamat)
                        productHouseNumberInput.editText?.setText(product.no_rumah ?: "")
                        productPostalCodeInput.editText?.setText(product.kode_pos)
                        productWeightInput.editText?.setText(product.berat)

                        oldImageUrl = product.imageUrl
                        if (oldImageUrl.isNotEmpty()) {
                            Glide.with(this).load(oldImageUrl).into(productImageView)
                        }
                    }
                } else {
                    Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengambil data produk: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProductWithImage(
        productId: String, productName: String, productQuantity: Int, productNote: String,
        address: String, houseNumber: String, postalCode: String, weight: String, imageUri: Uri
    ) {
        val currentUserId = auth.currentUser?.uid ?: return
        uploadImageToStorage(imageUri) { imageUrl ->
            if (imageUrl != null) {
                val updatedFields = mapOf(
                    "nama_produk" to productName,
                    "jumlah" to productQuantity,
                    "catatan" to productNote,
                    "alamat" to address,
                    "no_rumah" to houseNumber,
                    "kode_pos" to postalCode,
                    "berat" to weight,
                    "imageUrl" to imageUrl,
                    "userId" to currentUserId,
                    "tersedia" to true,
                    "timestamp" to Timestamp.now()
                )
                firestore.collection("items")
                    .document(productId)
                    .update(updatedFields)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal memperbarui produk", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Gagal mendapatkan URL gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProductWithoutImage(
        productId: String, productName: String, productQuantity: Int, productNote: String,
        address: String, houseNumber: String, postalCode: String, weight: String, imageUrl: String
    ) {
        val currentUserId = auth.currentUser?.uid ?: return

        val updatedFields = mapOf(
            "nama_produk" to productName,
            "jumlah" to productQuantity,
            "catatan" to productNote,
            "alamat" to address,
            "no_rumah" to houseNumber,
            "kode_pos" to postalCode,
            "berat" to weight,
            "imageUrl" to imageUrl,
            "userId" to currentUserId,
            "tersedia" to true,
            "timestamp" to Timestamp.now()
        )

        firestore.collection("items")
            .document(productId)
            .update(updatedFields)
            .addOnSuccessListener {
                Toast.makeText(this, "Produk berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memperbarui produk", Toast.LENGTH_SHORT).show()
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
            callback(null)
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

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
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
            val selectedAddress = data?.getStringExtra("selectedAddress")
            productAddressInput.editText?.setText(selectedAddress)
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