/*****************************************************************************
 * Project:     ShipFast API Protection (App)
 * File:        RestAPIUtils.kt
 * Original:    Created on 3 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * A class with various utilities for communicating with the backend REST API.
 *****************************************************************************/

package com.criticalblue.shipfast

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL

/** The server's base URL */
const val SERVER_BASE_URL = /*"http://192.168.0.200:3000"*/ "http://10.0.2.2:3000"
/** The authorisation request header */
const val AUTH_HEADER = "Authorization"
/** The location latitude request header */
const val LATITUDE_HEADER = "SR-Latitude"
/** The location longitude request header */
const val LONGITUDE_HEADER = "SR-Longitude"
/** The shipment state request header */
const val SHIPMENT_STATE_HEADER = "SR-State"

/**
 * Request the nearest available shipment to the given location.
 *
 * @param context the application context
 * @param originLocation the location to use when requesting the nearest available shipment
 * @param callback the callback to invoke on success or failure
 */
fun requestNearestShipment(context: Context, originLocation: LatLng, callback: (Response?, Shipment?) -> Unit) {

    val userCredentials = loadUserCredentials(context)
    val httpClient = OkHttpClient()
    val url = URL("$SERVER_BASE_URL/shipments/nearest_shipment")
    val request = Request.Builder()
            .url(url)
            .addHeader(AUTH_HEADER, "Bearer ${userCredentials.idToken}")
            .addHeader(LATITUDE_HEADER, originLocation.latitude.toString())
            .addHeader(LONGITUDE_HEADER, originLocation.longitude.toString())
            .build()
    httpClient.newCall(request).enqueue(object: Callback {
        override fun onResponse(call: Call?, response: Response?) {
            response?.let {
                if (!it.isSuccessful) {
                    callback(null, null)
                }
                else {
                    it.body()?.let {
                        val json = JSONObject(it.string())
                        val shipment = parseJSONForShipment(json)
                        callback(response, shipment)
                    }
                }
            }
        }

        override fun onFailure(call: Call?, e: IOException?) {
            callback(null, null)
        }
    })
}

/**
 * Request a list of shipments which have been delivered.
 *
 * @param context the application context
 * @param callback the callback to invoke on success or failure
 */
fun requestDeliveredShipments(context: Context, callback: (Response?, List<Shipment>?) -> Unit) {

    val userCredentials = loadUserCredentials(context)
    val httpClient = OkHttpClient()
    val url = URL("$SERVER_BASE_URL/shipments/delivered")
    val request = Request.Builder()
            .url(url)
            .addHeader(AUTH_HEADER, "Bearer ${userCredentials.idToken}")
            .build()
    httpClient.newCall(request).enqueue(object: Callback {
        override fun onResponse(call: Call?, response: Response?) {
            response?.body()?.let {
                val json = JSONArray(it.string())
                val deliveredShipments = mutableListOf<Shipment>()
                for (i in 0 until json.length()) {
                    val shipment = parseJSONForShipment(json.getJSONObject(i))
                    shipment?.let {
                        deliveredShipments.add(it)
                    }
                }
                callback(response, deliveredShipments)
            }
        }

        override fun onFailure(call: Call?, e: IOException?) {
            callback(null, null)
        }
    })
}

/**
 * Request the shipment with the given ID.
 *
 * @param context the application context
 * @param shipmentID the shipment's unique ID
 * @param callback the callback to invoke on success or failure
 */
fun requestShipment(context: Context, shipmentID: Int, callback: (Response?, Shipment?) -> Unit) {

    val userCredentials = loadUserCredentials(context)
    val httpClient = OkHttpClient()
    val url = URL("$SERVER_BASE_URL/shipments/$shipmentID")
    val request = Request.Builder()
            .url(url)
            .addHeader(AUTH_HEADER, "Bearer ${userCredentials.idToken}")
            .build()
    httpClient.newCall(request).enqueue(object: Callback {
        override fun onResponse(call: Call?, response: Response?) {
            response?.body()?.let {
                val json = JSONObject(it.string())
                val shipment = parseJSONForShipment(json)
                callback(response, shipment)
            }
        }

        override fun onFailure(call: Call?, e: IOException?) {
            callback(null, null)
        }
    })
}

/**
 * Request a state update to the shipment with the given ID.
 *
 * @param context the application context
 * @param currentLocation the location to use when updating the shipment's state
 * @param shipmentID the shipment's unique ID
 * @param newState the new state of the shipment
 * @param callback the callback to invoke on success or failure
 */
fun requestShipmentStateUpdate(context: Context, currentLocation: LatLng, shipmentID: Int, newState: ShipmentState,
                               callback: (Response?, Boolean) -> Unit) {

    val userCredentials = loadUserCredentials(context)
    val httpClient = OkHttpClient()
    val url = URL("$SERVER_BASE_URL/shipments/update_state/$shipmentID")
    val request = Request.Builder()
            .url(url)
            .method("POST", RequestBody.create(null, ByteArray(0)))
            .addHeader(AUTH_HEADER, "Bearer ${userCredentials.idToken}")
            .addHeader(LATITUDE_HEADER, currentLocation.latitude.toString())
            .addHeader(LONGITUDE_HEADER, currentLocation.longitude.toString())
            .addHeader(SHIPMENT_STATE_HEADER, newState.ordinal.toString())
            .build()
    httpClient.newCall(request).enqueue(object: Callback {
        override fun onResponse(call: Call?, response: Response?) {
            callback(response, response?.isSuccessful ?: false)
        }

        override fun onFailure(call: Call?, e: IOException?) {
            callback(null, false)
        }
    })
}

fun requestShipmentRoute(context: Context, shipment: Shipment, callback: (Response?, Boolean) -> Unit) {

    val googleAPIKey = context.packageManager.getApplicationInfo( context.packageName, PackageManager.GET_META_DATA)
            .metaData.getString("com.google.android.geo.API_KEY")
    val httpClient = OkHttpClient()
    val url = URL("https://maps.googleapis.com/maps/api/directions/json?" +
            "origin=${shipment.pickupLocation.latitude},${shipment.pickupLocation.longitude}" +
            "&destination=${shipment.deliveryLocation.latitude},${shipment.deliveryLocation.longitude}" +
            "&key=$googleAPIKey")
    val request = Request.Builder()
            .url(url)
            .build()
    httpClient.newCall(request).enqueue(object: Callback {
        override fun onResponse(call: Call?, response: Response?) {
            // TODO get waypoints
            response?.body()?.let {
                val json = JSONObject(it.string())
                Log.i("SR", "$json")
                callback(response, response?.isSuccessful ?: false)
            }
        }

        override fun onFailure(call: Call?, e: IOException?) {
            callback(null, false)
        }
    })
}

/**
 * Parse the given JSON object into a Shipment object.
 *
 * @param json the JSON representation
 * @return the shipment
 */
fun parseJSONForShipment(json: JSONObject): Shipment? {

    try {
        return Shipment(
                json.getInt("id"),
                json.getString("description"),
                json.getDouble("gratuity"),
                json.getString("pickupName"),
                LatLng(json.getDouble("pickupLatitude"), json.getDouble("pickupLongitude")),
                json.getString("deliveryName"),
                LatLng(json.getDouble("deliveryLatitude"), json.getDouble("deliveryLongitude")),
                ShipmentState.values().get(json.getInt("state")))
    }
    catch (e: JSONException) {
        return null
    }
}

/**
 * Allows Location objects to be converted to LatLng objects.
 */
fun Location.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}

/**
 * Allows LatLng objects to be converted to Location objects.
 */
fun LatLng.toLocation(): Location {
    val location = Location(LocationManager.GPS_PROVIDER)
    location.latitude = this.latitude
    location.longitude = this.longitude
    return location
}