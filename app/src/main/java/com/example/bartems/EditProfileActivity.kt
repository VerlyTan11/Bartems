package com.example.bartems

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var loadingAnimation: LottieAnimationView
    // Declare EditText for name, phone, and email
    private lateinit var nameEditText: TextInputEditText
    private lateinit var phoneEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var addressEditText: TextInputEditText

    // Variable for image URI
    private var imageUri: Uri? = null

    // Constant for image pick
    private val IMAGE_PICK_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile)
        // Initialize address input field
        addressEditText = findViewById(R.id.alamat_user_input)
        loadingAnimation = findViewById(R.id.loadingAnimation)
        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize UI elements
        nameEditText = findViewById<TextInputLayout>(R.id.nama_user).editText as TextInputEditText
        phoneEditText = findViewById<TextInputLayout>(R.id.telp_user).editText as TextInputEditText
        emailEditText = findViewById<TextInputLayout>(R.id.jumlah).editText as TextInputEditText

        // Set up back button click listener
        val backEditProfile = findViewById<ImageView>(R.id.back_edit_profile)
        backEditProfile.setOnClickListener {
            startActivity(Intent(this@EditProfileActivity, ProfileActivity::class.java))
            finish() // Close EditProfileActivity
        }

        // Set up save button click listener
        val saveButton = findViewById<Button>(R.id.btn_simpan_user)
        saveButton.setOnClickListener {
            if (imageUri != null) {
                uploadImageToFirebaseStorage(imageUri!!)
            } else {
                updateUserProfile(null) // Call method without image
            }
        }

        // Set up select image button click listener
        val selectImageButton = findViewById<Button>(R.id.btn_pilih_gambar)
        selectImageButton.setOnClickListener {
            selectImage()
        }

        // Inside EditProfileActivity, when navigating to PostItemActivity
        val pilihDariMapButton = findViewById<TextView>(R.id.gotomap)
        pilihDariMapButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("caller", "EditProfileActivity")
            startActivityForResult(intent, 1001) // For getting address from map
        }

        // Load existing user data
        loadUserData()

    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        userId?.let {
            firestore.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: ""
                        val phone = document.getString("phone") ?: ""
                        val imageUrl = document.getString("imageUrl") // Ambil URL gambar
                        val address = document.getString("address") ?: "" // Ambil alamat

                        // Isi data pada field input
                        nameEditText.setText(name)
                        phoneEditText.setText(phone)

                        // Dapatkan email dari Firebase Authentication
                        val email = auth.currentUser?.email ?: ""
                        emailEditText.setText(email)

                        // Disable editing untuk email
                        emailEditText.isEnabled = false // Disable editing

                        // Set alamat di field input
                        addressEditText.setText(address) // Set alamat yang ada di Firebase

                        // Memuat gambar jika ada URL gambar
                        val imageView = findViewById<ImageView>(R.id.gambar_user)
                        if (imageUrl != null) {
                            Glide.with(this).load(imageUrl).into(imageView)
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun updateUserProfile(imageUrl: String?) {
        val userId = auth.currentUser?.uid
        val name = nameEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()

        userId?.let {
            val userUpdates = mutableMapOf<String, Any>()

            if (name.isNotEmpty()) {
                userUpdates["name"] = name
            }
            if (phone.isNotEmpty()) {
                userUpdates["phone"] = phone
            }
            if (!imageUrl.isNullOrEmpty()) {
                userUpdates["imageUrl"] = imageUrl
            }
            if (!address.isNullOrEmpty()) {
                userUpdates["address"] = address
            }

            if (userUpdates.isNotEmpty()) {
                firestore.collection("users").document(it).set(userUpdates, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)  // Indicate success
                        finish()  // Close EditProfileActivity
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "No fields to update", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val storageReference = FirebaseStorage.getInstance().getReference("user_images/$userId/profile_image.jpg")

        // Log untuk debugging
        Log.d("EditProfileActivity", "User ID: $userId")
        Log.d("EditProfileActivity", "Image URI: $imageUri")

        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    updateUserProfile(imageUrl) // Call updateUserProfile with image URL
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error getting download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Get the returned address data
            val selectedAddress = data?.getStringExtra("selectedAddress")

            // Update the address input field with the selected address
            addressEditText.setText(selectedAddress)

            // Optionally, you can show a confirmation or message
            Toast.makeText(this, "Address updated successfully!", Toast.LENGTH_SHORT).show()
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