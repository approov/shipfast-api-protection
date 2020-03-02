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
import android.location.LocationManager
import android.util.Log
import android.view.View
import android.widget.*
import com.criticalblue.shipfast.api.*
import com.criticalblue.shipfast.config.*
import com.criticalblue.shipfast.dto.Shipment
import com.criticalblue.shipfast.dto.ShipmentResponse
import com.criticalblue.shipfast.dto.ShipmentState
import com.criticalblue.shipfast.utils.ViewShow
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
import io.approov.framework.okhttp.ApproovService

const val TAG = "SHIPFAST_APP"

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

    companion object {
        var approovService: ApproovService? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        approovService = ApproovService(applicationContext, resources.getString(R.string.approov_config))
        approovService!!.setBindingHeader("Authorization")

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
        nextStateButton.setOnClickListener { _ -> performAdvanceToNextState(API_REQUEST_ATTEMPTS) }
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
            zoomMapIntoLocation(googleMap, LatLng(DRIVER_LATITUDE, DRIVER_LONGITUDE))
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

    override fun onSaveInstanceState(outState: Bundle) {
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

            RestAPI.requestShipmentStateUpdate(this@ShipmentActivity, LatLng(DRIVER_LATITUDE, DRIVER_LONGITUDE),
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
        }
    }

    fun fetchNearestShipment(remainingRetries: Int) {

        if (!availabilitySwitch.isChecked) {
            return
        }

        if (handleRemainingRetries(remainingRetries, "Unable to fetch the nearest Shipment!")) {
            return
        }

        lastLocation?.let {

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

    /**
     * Allows Location objects to be converted to LatLng objects.
     *
     * @return the LatLng value
     */
    fun Location.toLatLng(): LatLng {
        return LatLng(this.latitude, this.longitude)
    }

    /**
     * Allows LatLng objects to be converted to Location objects.
     *
     * @return the Location value
     */
    fun LatLng.toLocation(): Location {
        val location = Location(LocationManager.GPS_PROVIDER)
        location.latitude = this.latitude
        location.longitude = this.longitude
        return location
    }

    private fun showFatalError(view: View, errorMessage: String) {
        Log.e(TAG,errorMessage)
        stopProgress()
        runOnUiThread{
            availabilitySwitch.isChecked = false
            ViewShow.error(view, errorMessage)
        }
    }
}
