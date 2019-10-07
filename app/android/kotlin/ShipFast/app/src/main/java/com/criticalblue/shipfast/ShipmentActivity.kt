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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest


/** The maximum number of attempts to fetch the next shipment before reporting a failure */
const val FETCH_NEXT_SHIPMENT_ATTEMPTS = 3

const val TAG = "SHIPFAST_DEMO"

/**
 * The Shipment activity class.
 */
class ShipmentActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /** The active state of the activity */
    private var activityActive = false

    /** The current shipment */
    private var currentShipment: Shipment? = null

    /** The location request */
    private lateinit var locationRequest: LocationRequest

    /** The last known location */
    private var lastLocation: Location? = null

    /** The Google API client for location data */
    private lateinit var googleAPIClient: GoogleApiClient

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

        updateShipmentProgressBar = findViewById(R.id.updateShipmentProgressBar)
        descriptionTextView = findViewById(R.id.shipmentDescription)
        gratuityTextView = findViewById(R.id.shipmentGratuity)
        pickupTextView = findViewById(R.id.shipmentPickup)
        deliveryTextView = findViewById(R.id.shipmentDelivery)
        stateTextView = findViewById(R.id.shipmentState)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        nextStateButton = findViewById(R.id.nextStateButton)
        nextStateButton.setOnClickListener { _ -> performAdvanceToNextState() }
        availabilitySwitch = findViewById(R.id.availabilitySwitch)
        availabilitySwitch.setOnCheckedChangeListener { _, isChecked -> performToggleAvailability(isChecked) }

        locationRequest = LocationRequest
                .create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(500)
        googleAPIClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        // 51.535472, -0.104971   -> London
        // 37.441883, -122.143019 -> Palo Alto, California
        // 55.944879, -3.181546   -> Edinburgh
        mapView.getMapAsync { googleMap ->
            zoomMapIntoLocation(googleMap, LatLng(ANDROID_EMULATOR_LATITUDE, ANDROID_EMULATOR_LONGITUDE))
        }
    }

    override fun onStart() {
        googleAPIClient.connect()
        super.onStart()
        activityActive = true
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        activityActive = false
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        activityActive = true
    }

    override fun onStop() {
        googleAPIClient.disconnect()
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

    override fun onConnected(connectionHint: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this@ShipmentActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleAPIClient, locationRequest, this)
        }
        else {
            ActivityCompat.requestPermissions(this@ShipmentActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 123)
        }
    }

    override fun onLocationChanged(location: Location?) {
        lastLocation = location
    }

    override fun onConnectionSuspended(cause: Int) {
        Log.i(TAG, "---> Location update suspended: $cause")
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Log.e(TAG, "---> Location update failed: $result")
    }

    /**
     * Perform advancing the current shipment to the next logical state.
     */
    private fun performAdvanceToNextState() {

        currentShipment?.let {
            startProgress()
            requestShipmentStateUpdate(this@ShipmentActivity, LatLng(ANDROID_EMULATOR_LATITUDE, ANDROID_EMULATOR_LONGITUDE),
                    it.id, it.nextState) { _, isSuccessful ->

                stopProgress()
                when (it.nextState) {
                    ShipmentState.DELIVERED -> {
                        runOnUiThread {
                            Toast.makeText(this@ShipmentActivity, "Congratulations! You've delivered ${it.description}",
                                    Toast.LENGTH_LONG)
                        }
                    }
                }
                updateShipment()

                if (it.nextState == ShipmentState.DELIVERED) {
                    val intent = Intent(this@ShipmentActivity, SummaryActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * Perform availability status update.
     *
     * @param isChecked the value of the switch
     */
    private fun performToggleAvailability(isChecked: Boolean) {

        if (isChecked) {
            fetchNextShipment(FETCH_NEXT_SHIPMENT_ATTEMPTS)
        }
    }

    /**
     * Fetch the next shipment (will either be the active one or the next available nearest one to the current location).
     *
     * @param remainingRetries the number of remaining retries
     */
    private fun fetchNextShipment(remainingRetries: Int) {

        if (!availabilitySwitch.isChecked)
            return

        if (remainingRetries <= 0) {
            runOnUiThread {
                availabilitySwitch.isChecked = false
                updateState()
                Toast.makeText(this@ShipmentActivity, "No shipment available!", Toast.LENGTH_LONG).show()
            }
            return
        }
        else {
            runOnUiThread {
                Toast.makeText(this@ShipmentActivity, "Waiting for shipment...", Toast.LENGTH_SHORT).show()
            }
        }

        startProgress()

        requestActiveShipment(this@ShipmentActivity) { _, shipment ->
            if (shipment == null) {
                if (lastLocation == null) {
                    stopProgress()
                    Thread.sleep(1000)
                    fetchNextShipment(remainingRetries - 1)
                }
                else {
                    lastLocation?.let {
                        startProgress()
                        requestNearestShipment(this@ShipmentActivity, it.toLatLng()) { _, shipment ->
                            stopProgress()
                            this@ShipmentActivity.currentShipment = shipment
                            runOnUiThread {
                                updateState()
                            }
                            Thread.sleep(1000)
                            if (shipment == null && activityActive) {
                                fetchNextShipment(remainingRetries - 1)
                            }
                        }
                    }
                }
            }
            else {
                stopProgress()
                this@ShipmentActivity.currentShipment = shipment
                runOnUiThread {
                    updateState()
                }
            }
        }
    }

    /**
     * Update the current shipment by requesting data from the server.
     */
    private fun updateShipment() {

        currentShipment?.id?.let {
            startProgress()
            requestShipment(this@ShipmentActivity, it) { _, shipment ->
                stopProgress()
                this@ShipmentActivity.currentShipment = shipment
                runOnUiThread { updateState() }
            }
        }
    }

    /**
     * Update the state of the activity to reflect the current shipment.
     */
    private fun updateState() {

        nextStateButton.isEnabled = false
        nextStateButton.visibility = View.INVISIBLE

        currentShipment?.let {
            descriptionTextView.text = it.description
            gratuityTextView.text = "£${it.gratuity.toInt()}"
            pickupTextView.text = it.pickupName
            deliveryTextView.text = it.deliveryName
            stateTextView.text = it.state.name

            mapView.getMapAsync { googleMap ->
                addMapMarker(googleMap, it.pickupLocation, it.pickupName, BitmapDescriptorFactory.HUE_GREEN)
                addMapMarker(googleMap, it.deliveryLocation, it.deliveryName, BitmapDescriptorFactory.HUE_RED)
                addMapRoute(googleMap, it.pickupLocation, it.deliveryLocation)
                zoomMapIntoLocation(googleMap, it.pickupLocation)
            }

            nextStateButton.text = it.state.nextStateActionName
            nextStateButton.isEnabled = it.state != ShipmentState.DELIVERED
            nextStateButton.visibility = if (it.state != ShipmentState.DELIVERED) View.VISIBLE else View.INVISIBLE
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
     * Add a line to the given map which represents the route from the given start location to the given end location.
     *
     * @param googleMap the map view
     * @param routeStart the start location of the route
     * @param routeEnd the end location of the route
     */
    private fun addMapRoute(googleMap: GoogleMap, routeStart: LatLng, routeEnd: LatLng) {

        val rectOptions = PolylineOptions().add(routeStart).add(routeEnd)
        val polyline = googleMap.addPolyline(rectOptions)
        polyline.color = Color.CYAN
        polyline.width = 9.0f
        polyline.isGeodesic = true
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
