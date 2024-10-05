package com.example.bartems

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


class PostItemActivity : AppCompatActivity() {

    private val cameraPermissionRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            // Handle the case where the permission is not granted
            // Show a message to the user
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_item)

        // Handle click event for back button
        val backPostItem = findViewById<ImageView>(R.id.back_post_item)
        backPostItem.setOnClickListener {
            val intent = Intent(this@PostItemActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Handle click event for the image view to open the camera
        val imageView = findViewById<ImageView>(R.id.gambar_product)
        imageView.setOnClickListener {
            checkCameraPermission()
        }

        // Additional code for the fragment can go here
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
        // Intent to open the camera
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }
}

