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
        confirmButton.setOnClickListener {
            val selectedAddress = fullAddressText.text.toString() // Ambil alamat lengkap yang ditampilkan
            if (selectedAddress.isNotEmpty()) {
                val intent = Intent(this@MapActivity, PostItemActivity::class.java)
                intent.putExtra("selectedAddress", selectedAddress) // Kirimkan alamat ke PostItemActivity
                startActivity(intent)
            } else {
                Toast.makeText(this, "Silakan pilih lokasi terlebih dahulu", Toast.LENGTH_SHORT).show()
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
