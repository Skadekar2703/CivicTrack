package com.tommy.civictrack

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import android.view.MotionEvent

class MapPickerActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var confirmButton: MaterialButton
    private var marker: Marker? = null
    private var selectedPoint: GeoPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        mapView = findViewById(R.id.mapPickerView)
        confirmButton = findViewById(R.id.confirmLocationButton)

        setupMap()
        bindActions()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    private fun setupMap() {
        val initialLatitude = intent.getDoubleExtra(EXTRA_LATITUDE, DEFAULT_CITY.latitude)
        val initialLongitude = intent.getDoubleExtra(EXTRA_LONGITUDE, DEFAULT_CITY.longitude)
        val initialPoint = GeoPoint(initialLatitude, initialLongitude)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(DEFAULT_ZOOM)
        mapView.controller.setCenter(initialPoint)
        placeMarker(initialPoint)

        mapView.overlays.add(object : Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
                val event = e ?: return false
                val targetMap = mapView ?: return false
                val point = targetMap.projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                placeMarker(point)
                return true
            }
        })
    }

    private fun bindActions() {
        findViewById<MaterialButton>(R.id.cancelLocationButton).setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        confirmButton.setOnClickListener {
            val point = selectedPoint ?: return@setOnClickListener
            setResult(
                Activity.RESULT_OK,
                Intent().apply {
                    putExtra(EXTRA_LATITUDE, point.latitude)
                    putExtra(EXTRA_LONGITUDE, point.longitude)
                }
            )
            finish()
        }
    }

    private fun placeMarker(point: GeoPoint) {
        selectedPoint = point
        confirmButton.isEnabled = true

        if (marker == null) {
            marker = Marker(mapView).apply {
                icon = ContextCompat.getDrawable(this@MapPickerActivity, R.drawable.ic_marker_pothole)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Selected location"
            }
            mapView.overlays.add(marker)
        }

        marker?.position = point
        mapView.controller.animateTo(point)
        mapView.invalidate()
    }

    companion object {
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
        private val DEFAULT_CITY = GeoPoint(18.5204, 73.8567)
        private const val DEFAULT_ZOOM = 14.0
    }
}
