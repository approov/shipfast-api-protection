/*
Copyright (C) 2020 CriticalBlue Ltd.

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

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
import androidx.core.content.ContextCompat
import com.criticalblue.shipfast.api.*
import com.criticalblue.shipfast.config.*
import com.criticalblue.shipfast.dto.Shipment
import com.criticalblue.shipfast.dto.ShipmentResponse
import com.criticalblue.shipfast.dto.ShipmentState
import com.criticalblue.shipfast.utils.ViewShow
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.*

const val TAG = "SHIPFAST_APP"

/**
 * The Shipment activity class.
 */
class ShipmentActivity : AppCompatActivity(), OnMapReadyCallback {

    /** The active state of the activity */
    private var activityActive = false

    /** The current shipment */
    private var currentShipment: Shipment? = null

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

    /** The next state button */
    private lateinit var nextStateButton: Button

    /** The availability switch */
    private lateinit var availabilitySwitch: Switch

    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            this.lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        setContentView(R.layout.activity_shipment)
        title = "Current Shipment"

        updateShipmentProgressBar = findViewById(R.id.updateShipmentProgressBar)
        descriptionTextView = findViewById(R.id.shipmentDescription)
        gratuityTextView = findViewById(R.id.shipmentGratuity)
        pickupTextView = findViewById(R.id.shipmentPickup)
        deliveryTextView = findViewById(R.id.shipmentDelivery)
        stateTextView = findViewById(R.id.shipmentState)
        nextStateButton = findViewById(R.id.nextStateButton)
        nextStateButton.setOnClickListener { _ -> performAdvanceToNextState(API_REQUEST_ATTEMPTS) }
        availabilitySwitch = findViewById(R.id.availabilitySwitch)
        availabilitySwitch.setOnCheckedChangeListener { _, isChecked -> performToggleAvailability(isChecked) }

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Build the map.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap?) {
        this.map = map

        // Prompt the user for permission.
        getLocationPermission()

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }


    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    Log.i(TAG, "Start task to get current device location")
                    if (task.isSuccessful) {
                        Log.i(TAG, "Finished task to get the current device location")
                        // Set the map's camera position to the current location of the device.
                        this.lastKnownLocation = task.result
                        if (this.lastKnownLocation != null) {
                            Log.i(TAG, "Set the current device location on the map.")
                            this.addCurrentLocationToMap(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                        } else {
                            Log.i(TAG, "Last known location is null. Using defaults for Driver Coordinates.")
                            this.addCurrentLocationToMap(DRIVER_LATITUDE, DRIVER_LONGITUDE)
                            this.map?.uiSettings?.isMyLocationButtonEnabled = false
                        }

                    } else {
                        Log.i(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        this.addCurrentLocationToMap(DRIVER_LATITUDE, DRIVER_LONGITUDE)
                        this.map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun addCurrentLocationToMap(latitude: Double, longitude: Double) {
        Log.i(TAG, "Added to map the last known location: ${latitude}, ${longitude}")

        this.map?.addMarker(
                MarkerOptions()
                        .position(LatLng(latitude, longitude))
                        .title("Driver Coordinates: ${latitude}, ${longitude}")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

        )

        this.map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(latitude, longitude), DEFAULT_ZOOM.toFloat()))
    }
    
    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private fun updateLocationUI() {
        if (this.map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                this.map?.isMyLocationEnabled = true
                this.map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                this.map?.isMyLocationEnabled = false
                this.map?.uiSettings?.isMyLocationButtonEnabled = false
                this.lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    override fun onStart() {
        super.onStart()
        activityActive = true
    }

    override fun onPause() {
        super.onPause()
        activityActive = false
    }

    override fun onResume() {
        super.onResume()
        activityActive = true
    }

    override fun onStop() {
        super.onStop()
        activityActive = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        this.map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, this.lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    private fun handleRemainingRetries(remainingRetries: Int, message: String): Boolean {

        if (remainingRetries <= 0) {

            stopProgress()

            runOnUiThread {
                availabilitySwitch.isChecked = false
                ViewShow.warning(findViewById(R.id.shipmentState), message)
            }

            return true
        } else {
            Thread.sleep(API_REQUEST_RETRY_SLEEP_MILLESECONDS.toLong())
            return false
        }
    }

    /**
     * Perform advancing the current shipment to the next logical state.
     */
    private fun performAdvanceToNextState(remainingRetries: Int) {

        currentShipment?.let {

            startProgress()

            if (handleRemainingRetries(remainingRetries, "Unable to update the shipment state!")) {
                return
            }

            RestAPI.requestShipmentStateUpdate(this@ShipmentActivity, LatLng(this.lastKnownLocation!!.latitude, this.lastKnownLocation!!.longitude),
                    it.id, it.nextState) { shipmentResponse: ShipmentResponse ->

                stopProgress()

                if (shipmentResponse.hasApproovTransientError()) {
                    Log.i(TAG, shipmentResponse.errorMessage())
                    runOnUiThread {
                        ViewShow.error(findViewById(R.id.shipmentState), shipmentResponse.errorMessage())
                    }
                    performAdvanceToNextState(remainingRetries - 1)
                    return@requestShipmentStateUpdate
                }

                if (shipmentResponse.hasApproovFatalError()) {
                    Log.i(TAG, shipmentResponse.errorMessage())
                    runOnUiThread {
                        ViewShow.error(findViewById(R.id.shipmentState), shipmentResponse.errorMessage())
                    }
                    return@requestShipmentStateUpdate
                }

                if (shipmentResponse.isNotOk()) {
                    Log.i(TAG, shipmentResponse.errorMessage())
                    runOnUiThread{
                        ViewShow.warning(findViewById(R.id.shipmentState), "Retrying to update shipment state.")
                    }
                    performAdvanceToNextState(remainingRetries - 1)
                    return@requestShipmentStateUpdate
                }

                fetchShipmentUpdate(API_REQUEST_ATTEMPTS)

                when (it.nextState) {
                    ShipmentState.DELIVERED -> {
                        // Hack to wait for the previous message to be displayed.
                        Thread.sleep(SNACKBAR_THREAD_SLEAP_MILLESECONDS.toLong())
                        runOnUiThread {
                            ViewShow.success(findViewById(R.id.shipmentState), "You've delivered to: ${it.description}")
                        }

                        // Hack to run only the intent after the user had a chance to see the success message.
                        Thread.sleep(SNACKBAR_THREAD_SLEAP_MILLESECONDS.toLong())
                        val intent = Intent(this@ShipmentActivity, SummaryActivity::class.java)
                        startActivity(intent)
                    }
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
            fetchShipment(API_REQUEST_ATTEMPTS)
        }
    }

    /**
     * Fetch the next shipment (will either be the active one or the next available nearest one to the current location).
     *
     * @param remainingRetries the number of remaining retries
     */
    private fun fetchShipment(remainingRetries: Int) {

        if (!availabilitySwitch.isChecked) {
            return
        }

        if (handleRemainingRetries(remainingRetries, "Unable to fetch a Shipment!")) {
            return
        }

        startProgress()

        RestAPI.requestActiveShipment(this@ShipmentActivity) { shipmentResponse: ShipmentResponse ->

            if (shipmentResponse.isOk()) {

                if (shipmentResponse.hasNoData()) {
                    runOnUiThread{
                        ViewShow.info(findViewById(R.id.shipmentState), "No Active Shipment. Fetching nearest Shipment.")
                    }
                    this.fetchNearestShipment(API_REQUEST_ATTEMPTS)
                    return@requestActiveShipment
                }

                stopProgress()

                runOnUiThread {
                    updateState(shipmentResponse.get())
                }

                return@requestActiveShipment
            }

            if (shipmentResponse.hasApproovTransientError()) {
                Log.i(TAG, shipmentResponse.errorMessage())
                runOnUiThread{
                    ViewShow.error(findViewById(R.id.shipmentState), shipmentResponse.errorMessage())
                }
                fetchShipment(remainingRetries - 1)
                return@requestActiveShipment
            }

            if (shipmentResponse.hasApproovFatalError()) {
                showFatalError(findViewById(R.id.shipmentState), shipmentResponse.errorMessage())
                return@requestActiveShipment
            }

            if (shipmentResponse.isNotOk()) {
                Log.i(TAG, shipmentResponse.errorMessage())
                runOnUiThread {
                    ViewShow.info(findViewById(R.id.shipmentState), "Retrying to fetch the active shipment.")
                }
                fetchShipment(remainingRetries - 1)
                return@requestActiveShipment
            }

            stopProgress()
        }
    }

    fun fetchNearestShipment(remainingRetries: Int) {

        if (!availabilitySwitch.isChecked) {
            return
        }

        if (handleRemainingRetries(remainingRetries, "Unable to fetch the nearest Shipment!")) {
            return
        }

        this.lastKnownLocation?.let {

            RestAPI.requestNearestShipment(this@ShipmentActivity, it.toLatLng()) { shipmentResponse: ShipmentResponse ->

                if (shipmentResponse.isOk()) {

                    if (shipmentResponse.hasNoData()) {
                        runOnUiThread {
                            ViewShow.error(findViewById(R.id.shipmentState), "No nearest Shipment available.")
                        }
                        return@requestNearestShipment
                    }

                    stopProgress()

                    runOnUiThread {
                        updateState(shipmentResponse.get())
                    }

                    return@requestNearestShipment
                }

                if (shipmentResponse.hasApproovTransientError()) {
                    Log.i(TAG, shipmentResponse.errorMessage())
                    runOnUiThread{
                        ViewShow.error(findViewById(R.id.shipmentState), shipmentResponse.errorMessage())
                    }
                    fetchNearestShipment(remainingRetries - 1)
                    return@requestNearestShipment
                }

                if (shipmentResponse.hasApproovFatalError()) {
                    Log.i(TAG, shipmentResponse.errorMessage())
                    runOnUiThread{
                        ViewShow.error(findViewById(R.id.shipmentState), shipmentResponse.errorMessage())
                    }
                    return@requestNearestShipment
                }

                if (shipmentResponse.isNotOk()) {
                    Log.i(TAG, shipmentResponse.errorMessage())
                    runOnUiThread{
                        ViewShow.info(findViewById(R.id.shipmentState), "Retrying to fetch the nearest shipment.")
                    }
                    fetchNearestShipment(remainingRetries - 1)
                    return@requestNearestShipment
                }
            }
        }

        stopProgress()

        return
    }

    /**
     * Update the current shipment by requesting data from the server.
     */
    private fun fetchShipmentUpdate(remainingRetries: Int) {

        if (handleRemainingRetries(remainingRetries, "Unable to fetch updated Shipment!")) {
            return
        }

        currentShipment?.id?.let {
            startProgress()
            RestAPI.requestShipment(this@ShipmentActivity, it) { shipmentResponse: ShipmentResponse ->

                if (shipmentResponse.isOk()) {

                    if (shipmentResponse.hasNoData()) {
                        runOnUiThread{
                            ViewShow.error(findViewById(R.id.shipmentState), "No updated Shipment to fetch!")
                        }
                        return@requestShipment
                    }

                    stopProgress()

                    val shipment: Shipment? = shipmentResponse.get()

                    runOnUiThread {
                        ViewShow.success(findViewById(R.id.shipmentState), "Shipment state update to: ${shipment?.state}")
                        updateState(shipment)
                    }

                    return@requestShipment
                }

                if (shipmentResponse.hasApproovTransientError()) {
                    Log.i(TAG, shipmentResponse.errorMessage())
                    runOnUiThread{
                        ViewShow.error(findViewById(R.id.shipmentState), shipmentResponse.errorMessage())
                    }
                    fetchShipmentUpdate(remainingRetries - 1)
                    return@requestShipment
                }

                if (shipmentResponse.hasApproovFatalError()) {
                    Log.i(TAG, shipmentResponse.errorMessage())
                    runOnUiThread{
                        ViewShow.error(findViewById(R.id.shipmentState), shipmentResponse.errorMessage())
                    }
                    return@requestShipment
                }

                if (shipmentResponse.isNotOk()) {
                    Log.i(TAG, shipmentResponse.errorMessage())
                    runOnUiThread {
                        ViewShow.error(findViewById(R.id.shipmentState), "Retrying to fetch shipment update.")
                    }
                    fetchShipmentUpdate(remainingRetries - 1)
                    return@requestShipment
                }
            }
        }
    }

    /**
     * Update the state of the activity to reflect the current shipment.
     */
    private fun updateState(currentShipment: Shipment?) {

        this@ShipmentActivity.currentShipment = currentShipment

        nextStateButton.isEnabled = false
        nextStateButton.visibility = View.INVISIBLE

        currentShipment?.let {
            descriptionTextView.text = it.description
            gratuityTextView.text = it.gratuity
            pickupTextView.text = it.pickupName
            deliveryTextView.text = it.deliveryName
            stateTextView.text = it.state.name

            this.map?.let { googleMap ->
                googleMap.addMarker(MarkerOptions()
                        .position(it.pickupLocation)
                        .title("Pickup from: ${it.pickupName}")
                        .snippet("${it.pickupLocation.latitude}, ${it.pickupLocation.longitude}")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .zIndex(1.0f)
                )

                googleMap.addMarker(MarkerOptions()
                        .position(it.deliveryLocation)
                        .title("Delivery to: ${it.deliveryName}")
                        .snippet("${it.deliveryLocation.latitude}, ${it.deliveryLocation.longitude}")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .zIndex(1.0f)
                )

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

    /**
     * Allows Location objects to be converted to LatLng objects.
     *
     * @return the LatLng value
     */
    fun Location.toLatLng(): LatLng {
        return LatLng(this.latitude, this.longitude)
    }

    private fun showFatalError(view: View, errorMessage: String) {
        Log.e(TAG,errorMessage)
        stopProgress()
        runOnUiThread{
            availabilitySwitch.isChecked = false
            ViewShow.error(view, errorMessage)
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 10
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }
}
