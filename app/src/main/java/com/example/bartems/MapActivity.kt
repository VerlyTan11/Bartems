package com.example.bartems

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        if (mapFragment != null) {
            mapFragment.getMapAsync(this)
        } else {
            Log.e("MapActivity", "SupportMapFragment is null")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Set initial location, e.g., center of the city
        val initialLocation = LatLng(-6.200000, 106.816666) // Jakarta coordinates
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))

        // Set a marker on map click
        map.setOnMapClickListener { latLng ->
            map.clear()
            map.addMarker(MarkerOptions().position(latLng).title("Selected Location"))

            // Return coordinates when the marker is set
            val intent = Intent().apply {
                putExtra("latitude", latLng.latitude)
                putExtra("longitude", latLng.longitude)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}
