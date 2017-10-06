/*****************************************************************************
 * Project:     ShipFast API Protection (App)
 * File:        ShipmentActivity.kt
 * Original:    Created on 2 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * The current shipment activity.
 *****************************************************************************/

package com.criticalblue.shipfast

import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.Manifest
import android.content.Intent
import android.view.View
import android.widget.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class ShipmentActivity : AppCompatActivity() {

    /** The active state of the activity */
    private var activityActive = false

    /** The current shipment */
    private var currentShipment: Shipment? = null
//        set(value) { updateState() }

    /** The location update callback */
    private lateinit var locationCallback: LocationCallback

    /** The progress bar */
    private lateinit var updateShipmentProgressBar: ProgressBar

    /** The shipment 'description' text view */
    private lateinit var descriptionTextView: TextView

    /** The shipment 'gratuity' text view */
    private lateinit var gratuityTextView: TextView

    /** The shipment 'pickup description' text view */
    private lateinit var pickupTextView: TextView

    /** The shipment 'delivery description' text view */
    private lateinit var deliveryTextView: TextView

    /** The shipment 'state' text view */
    private lateinit var stateTextView: TextView

    /** The map view */
    private lateinit var mapView: MapView

    /** The next state button */
    private lateinit var nextStateButton: Button

    /** The availability switch */
    private lateinit var availabilitySwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shipment)
        title = "Current Shipment"

        locationCallback = object: LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {

            }
        }

        updateShipmentProgressBar = findViewById(R.id.updateShipmentProgressBar)
        descriptionTextView = findViewById(R.id.shipmentDescription)
        gratuityTextView = findViewById(R.id.shipmentGratuity)
        pickupTextView = findViewById(R.id.shipmentPickup)
        deliveryTextView = findViewById(R.id.shipmentDelivery)
        stateTextView = findViewById(R.id.shipmentState)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        nextStateButton = findViewById(R.id.nextStateButton)
        nextStateButton.setOnClickListener { switch -> performAdvanceToNextState() }
        availabilitySwitch = findViewById(R.id.availabilitySwitch)
        availabilitySwitch.setOnCheckedChangeListener { switch, isChecked -> performToggleAvailability(isChecked) }
    }

    override fun onStart() {
        super.onStart()
        activityActive = true
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()

        // stop listening for location updates
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@ShipmentActivity)
//        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

        // start listening for location updates
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@ShipmentActivity)
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onStop() {
        super.onStop()
        activityActive = false
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    /**
     * Perform advancing the current shipment to the next logical state.
     */
    private fun performAdvanceToNextState() {

        currentShipment?.let {
            startProgress()
            requestShipmentStateUpdate(this@ShipmentActivity, LatLng(54.3, -4.4),
                    it.id, it.nextState, { _, isSuccessful ->

                stopProgress()
                when (it.nextState) {
                    ShipmentState.SHIPPED -> {
                        runOnUiThread {
                            Toast.makeText(this@ShipmentActivity, "Congratulations! You've shipped ${it.description}",
                                    Toast.LENGTH_LONG)
                        }
                    }
                }
                updateShipment()

                // FIXME REMOVE AFTER TESTING
                if (it.nextState == ShipmentState.SHIPPED) {
                    val intent = Intent(this@ShipmentActivity, SummaryActivity::class.java)
                    startActivity(intent)
                }
            })
        }
    }

    /**
     * Perform availability status update.
     *
     * @param isChecked the value of the switch
     */
    private fun performToggleAvailability(isChecked: Boolean) {

        if (isChecked) {
            pollForNearestShipment()
        }
    }

    /**
     * Keep polling the server until we get a shipment to work with.
     */
    private fun pollForNearestShipment() {

        Toast.makeText(this@ShipmentActivity, "Waiting for shipment...", Toast.LENGTH_SHORT).show()

        if (ActivityCompat.checkSelfPermission(this@ShipmentActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@ShipmentActivity)
            fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                location?.let {
                    startProgress()
                    requestNearestShipment(this@ShipmentActivity, it.toLatLng(), { _, shipment ->
                        stopProgress()
                        this@ShipmentActivity.currentShipment = shipment
                        runOnUiThread {
                            updateState()
                            if (shipment == null && activityActive) {
                                // FIXME tight loop and broken recursion
                                pollForNearestShipment()
                            }
                        }
                    })
                }
            }
        } else {
            ActivityCompat.requestPermissions(this@ShipmentActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 123)
            pollForNearestShipment()
        }
    }

    /**
     * Update the current shipment by requesting data from the server.
     */
    private fun updateShipment() {

        if (currentShipment == null) return
        startProgress()
        val shipmentID = currentShipment!!.id // FIXME
        requestShipment(this@ShipmentActivity, shipmentID, { _, shipment ->
            stopProgress()
            this@ShipmentActivity.currentShipment = shipment
            runOnUiThread { updateState() }
        })
    }

    /**
     * Update the state of the activity to reflect the current shipment.
     */
    private fun updateState() {

        nextStateButton.isEnabled = false

        currentShipment?.let {
            requestShipmentRoute(this, it, {_,_ -> })
            descriptionTextView.text = it.description
            gratuityTextView.text = "$${it.gratuity}"
            pickupTextView.text = it.pickupName
            deliveryTextView.text = it.deliveryName
            stateTextView.text = it.state.name

            mapView.getMapAsync { googleMap ->
                addMapMarker(googleMap, it.pickupLocation, it.pickupName, BitmapDescriptorFactory.HUE_GREEN)
                addMapMarker(googleMap, it.deliveryLocation, it.deliveryName, BitmapDescriptorFactory.HUE_RED)
                zoomMapIntoLocation(googleMap, it.pickupLocation)
            }

            nextStateButton.text = it.state.nextStateActionName
            nextStateButton.isEnabled = it.state != ShipmentState.SHIPPED
            // TODO hide button
        }
    }

    /**
     * Add a new marker with the given name at the given location to the map view.
     *
     * @param googleMap the map view
     * @param location the marker location
     * @param markerTitle the marker title
     * @param markerHue the marker hue
     */
    private fun addMapMarker(googleMap: GoogleMap, location: LatLng, markerTitle: String, markerHue: Float) {

        googleMap.addMarker(MarkerOptions().position(location).title(markerTitle).icon(BitmapDescriptorFactory.defaultMarker(markerHue)))
    }

    /**
     * Zoom the map into the given location.
     *
     * @param googleMap the map view
     * @param location the zoom location
     */
    private fun zoomMapIntoLocation(googleMap: GoogleMap, location: LatLng) {

        val cameraPosition = CameraPosition.Builder().target(location).zoom(10f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    /**
     * Start showing progress.
     */
    private fun startProgress() {
        runOnUiThread {
            updateShipmentProgressBar.visibility = View.VISIBLE
        }
    }

    /**
     * Stop showing progress.
     */
    private fun stopProgress() {
        runOnUiThread {
            updateShipmentProgressBar.visibility = View.INVISIBLE
        }
    }
}
