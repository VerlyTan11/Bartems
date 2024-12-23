package com.example.bartems

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textfield.TextInputEditText
import java.io.IOException
import java.util.Locale

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var backIcon: ImageView
    private lateinit var confirmButton: Button
    private lateinit var pilihLokasiText: TextView
    private lateinit var pilihText: TextView
    private lateinit var alamatInput: EditText
    private lateinit var geocoder: Geocoder
    private lateinit var searchIcon: ImageView
    private lateinit var fullAddressText: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Initialize views
        backIcon = findViewById(R.id.back)
        confirmButton = findViewById(R.id.confirm_button)
        pilihLokasiText = findViewById(R.id.text_pilih_loct)
        pilihText = findViewById(R.id.text_pilih)
        alamatInput = findViewById(R.id.text_input_alamat)
        searchIcon = findViewById(R.id.icon_search)
        fullAddressText = findViewById(R.id.text_full_address) // Initialize the new TextView
        geocoder = Geocoder(this, Locale.getDefault())
        // Initialize EditText variables to correspond to the fields in your layout
        val nameEditText = findViewById<TextInputEditText>(R.id.nama_user) // Example ID from your XML
        val phoneEditText = findViewById<TextInputEditText>(R.id.telp_user) // Example ID from your XML
        val emailEditText = findViewById<TextInputEditText>(R.id.jumlah) // Example ID from your XML


        // Set up map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this) ?: Log.e("MapActivity", "SupportMapFragment is null")

        // Back button click listener
        backIcon.setOnClickListener {
            finish() // Ends the current activity
        }

        // Set up search icon listener
        searchIcon.setOnClickListener {
            val address = alamatInput.text.toString()
            if (address.isNotEmpty()) {
                moveToAddress(address)
            }
        }

        // Confirm button action if needed
        // Dalam onCreate() MapActivity
        // Ketika button diklik di MapActivity
        confirmButton.setOnClickListener {
            val selectedAddress = fullAddressText.text.toString() // Full address selected
            val inputAddress = alamatInput.text.toString() // Address input by the user

            // Check if the intent came from PostItemActivity or EditProfileActivity
            val caller = intent.getStringExtra("caller") // This identifies where the intent came from

            if (caller == "PostItemActivity" || caller == "EditProfileActivity" || caller == "EditDetailProductActivity" ) {
                // For PostItemActivity, handle the product data
                if (selectedAddress.isNotEmpty() || inputAddress.isNotEmpty()) {
                    val resultIntent = Intent()

                    // Send back the selected and inputted address
                    resultIntent.putExtra("selectedAddress", selectedAddress) // Selected address
                    resultIntent.putExtra("inputAddress", inputAddress) // User input address

                    // Send back the product data from PostItemActivity
                    resultIntent.putExtra("nama_produk", intent.getStringExtra("nama_produk"))
                    resultIntent.putExtra("catatan", intent.getStringExtra("catatan"))
                    resultIntent.putExtra("jumlah", intent.getStringExtra("jumlah"))
                    resultIntent.putExtra("berat", intent.getStringExtra("berat"))
                    resultIntent.putExtra("alamat", intent.getStringExtra("alamat"))
                    resultIntent.putExtra("no_rumah", intent.getStringExtra("no_rumah"))
                    resultIntent.putExtra("kode_pos", intent.getStringExtra("kode_pos"))

                    // Return data to PostItemActivity
                    setResult(RESULT_OK, resultIntent)
                    finish() // Close MapActivity and return to PostItemActivity
                } else {
                    Toast.makeText(this, "Silakan pilih lokasi terlebih dahulu", Toast.LENGTH_SHORT).show()
                }
            } else if (caller == "EditProfileActivity") {
                // For EditProfileActivity, handle the user profile data
                val name = intent.getStringExtra("name") ?: ""
                val phone = intent.getStringExtra("phone") ?: ""
                val email = intent.getStringExtra("email") ?: ""

                // Ensure the name, phone, and email fields are not empty
                if (name.isNotEmpty() && phone.isNotEmpty() && email.isNotEmpty()) {
                    val resultIntent = Intent()

                    // Send back the updated user profile data
                    resultIntent.putExtra("name", name)
                    resultIntent.putExtra("phone", phone)
                    resultIntent.putExtra("email", email)

                    // Send the selected or inputted address back to EditProfileActivity
                    resultIntent.putExtra("selectedAddress", selectedAddress)
                    resultIntent.putExtra("inputAddress", inputAddress)

                    // Return the updated profile data to EditProfileActivity
                    setResult(RESULT_OK, resultIntent)
                    finish() // Close MapActivity and return to EditProfileActivity
                } else {
                    Toast.makeText(this, "Nama, No Telp, dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Handle cases where the intent is neither from PostItemActivity nor EditProfileActivity
                Toast.makeText(this, "Error: Unknown caller", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Set initial location (e.g., Jakarta)
        val initialLocation = LatLng(-6.200000, 106.816666)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))

        // Set a marker on map click
        map.setOnMapClickListener { latLng ->
            map.clear()
            map.addMarker(MarkerOptions().position(latLng).title("Selected Location"))

            // Fetch and display address
            val address = getAddressFromLatLng(latLng)
            if (address != null) {
                alamatInput.setText(address)
            } else {
                Toast.makeText(this, "Alamat tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAddressFromLatLng(latLng: LatLng): String? {
        return try {
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val address: Address = addresses[0]
                val fullAddress = address.getAddressLine(0) // Full address

                // Display the full address in the TextView
                fullAddressText.text = "$fullAddress"
                fullAddressText.visibility = TextView.VISIBLE

                fullAddress
            } else {
                null
            }
        } catch (e: IOException) {
            Log.e("MapActivity", "Geocoder failed", e)
            null
        }
    }

    private fun moveToAddress(address: String) {
        try {
            val addresses: MutableList<Address>? = geocoder.getFromLocationName(address, 1)
            if (addresses!!.isNotEmpty()) {
                val location = addresses[0]
                val latLng = LatLng(location.latitude, location.longitude)
                map.clear()
                map.addMarker(MarkerOptions().position(latLng).title("Alamat yang dimasukkan"))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                // Update the new TextView with the full address
                val fullAddress = addresses[0].getAddressLine(0)
                fullAddressText.text = "$fullAddress"
                fullAddressText.visibility = TextView.VISIBLE
            } else {
                Toast.makeText(this, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Log.e("MapActivity", "Geocoder failed", e)
            Toast.makeText(this, "Terjadi kesalahan saat mencari alamat", Toast.LENGTH_SHORT).show()
        }
    }
}
