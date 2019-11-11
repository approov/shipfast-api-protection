/*****************************************************************************
 * Project:     ShipFast API Protection (App)
 * File:        Shipment.kt
 * Original:    Created on 3 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * The class which represents a shipment.
 *****************************************************************************/

package com.criticalblue.shipfast.dto

import android.util.Log
import com.criticalblue.approov.exceptions.ApproovIOFatalException
import com.criticalblue.approov.exceptions.ApproovIOTransientException
import com.criticalblue.shipfast.TAG
import com.criticalblue.shipfast.utils.JsonParser
import com.google.android.gms.maps.model.LatLng
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * The enumeration of various states a shipment may be in
 */
enum class ShipmentState(val nextStateActionName: String) {
    READY("Accept"),
    ACCEPTED("Collect"),
    COLLECTED("Deliver"),
    DELIVERED("")
}

class ShipmentsResponse (
        private val response: Response?,
        private val exception: IOException?
) {

    private var hasTransientError: Boolean = false;
    private var hasFatalError: Boolean = false;

    /**
     * Tells where the response is a successful one or not.
     */
    private var isOk: Boolean = false

    private var hasData: Boolean = false

    /**
     * The list of delivered shipments.
     */
    private var deliveredShipments: MutableList<Shipment>? = null

    /**
     * The error message from IOException.
     */
    private var errorMessage: String = "Unknown error!"

    /**
     * Processes the injected constructor parameters during the class initialization.
     */
    init {
        if (this.exception != null) {
            this.isOk = false
            this.tryCatchApproovException(exception)
        } else {
            this.response?.let {
                if (it.isSuccessful) {
                    this.isOk = true
                    it.body()?.let {
                        buildShipmentsResponse(it.string())
                    }
                } else {
                    this.isOk = false

                    if (! response.message().isNullOrEmpty()) {
                        this.errorMessage = response.message()
                    } else {
                        this.errorMessage = "Unsuccessful Response."
                    }
                }
            }
        }
    }

    fun hasApproovTransientError(): Boolean {
        return this.hasTransientError
    }

    fun hasApproovFatalError(): Boolean {
        return this.hasFatalError
    }

    private fun tryCatchApproovException(exception: IOException) {

        try {
            throw exception
        } catch (e: SSLPeerUnverifiedException) {
            this.errorMessage = "Transient Error: Certificate pinning mismatch!"
            this.hasTransientError = true
        } catch (e: ApproovIOTransientException) {
            this.errorMessage = exception.message ?: this.errorMessage
            this.hasTransientError = true
        } catch (e: ApproovIOFatalException) {
            this.errorMessage = exception.message ?: this.errorMessage
            this.hasFatalError = true
        }
    }

    /**
     * Builds a local ShipmentResult object in order to extract the Shipment data object, and
     *  determine that the ShipmentResponse was successful.
     */
    private fun buildShipmentsResponse(bodyPayload: String) {
        Log.i(TAG, "BODY: " + bodyPayload)

        val json = JsonParser.toJSONArray(bodyPayload)

        json?.let {
            this.deliveredShipments = mutableListOf()

            for (i in 0 until it.length()) {
                val shipment = ShipmentResult(it.getJSONObject(i)).get()
                shipment?.let {
                    this.deliveredShipments?.add(it)
                }
            }
        }

        this.hasData = deliveredShipments!!.isNotEmpty()
    }

    /**
     * Tells that the response is a successful one.
     *
     * @return True when the response has a success http status code.
     */
    fun isOk(): Boolean {
        return this.isOk
    }

    /**
     * Tells that the response is a not successful one.
     *
     * @return True when the response have a http status code for a client or server error.
     */
    fun isNotOk(): Boolean {
        return !this.isOk
    }

    fun hasNoData(): Boolean {
        return !this.hasData
    }

    /**
     * Accesses the error message for the Shipment response.
     */
    fun errorMessage(): String {
        return this.errorMessage
    }


    /**
     * Get the Shipment data object.
     */
    fun get(): List<Shipment> {
        return this.deliveredShipments ?: arrayListOf()
    }
}


/**
 * This class receives the OkHttp3 Response? object and an IOException? and will build a local
 *  ShipmentResult object, from where the Shipment data object and the ShipmentResponse success
 *  status will be extracted.
 *
 * @param response  The response for the Shipment request, if available.
 * @param exception The IO exception occurred during the request/response life cycle, if any.
 */
class ShipmentResponse (
    private val response: Response?,
    private val exception: IOException?
) {

    private var hasTransientError: Boolean = false;
    private var hasFatalError: Boolean = false;

    /**
     * Tells where the response is a successful one or not.
     */
    private var isOk: Boolean = false

    private var hasData: Boolean = false

    /**
     * The data Shipment object.
     */
    private var shipment: Shipment? = null

    /**
     * The error message from IOException.
     */
    private var errorMessage: String = "Unknown error!"

    /**
     * Processes the injected constructor parameters during the class initialization.
     */
    init {
        if (this.exception != null) {
            this.isOk = false
            this.tryCatchApproovException(exception)
        } else {
            this.response?.let {
                if (it.isSuccessful) {
                    this.isOk = true
                    it.body()?.let {
                        buildShipmentResponse(it.string())
                    }
                } else {
                    this.isOk = false

                    if (! response.message().isNullOrEmpty()) {
                        this.errorMessage = response.message()
                    } else {
                        this.errorMessage = "Unsuccessful Response."
                    }
                }
            }
        }
    }

    fun hasApproovTransientError(): Boolean {
        return this.hasTransientError
    }

    fun hasApproovFatalError(): Boolean {
        return this.hasFatalError
    }

    private fun tryCatchApproovException(exception: IOException) {

        try {
            throw exception
        } catch (e: SSLPeerUnverifiedException) {
            this.errorMessage = "Transient Error: Certificate pinning mismatch!"
            this.hasTransientError = true
        } catch (e: ApproovIOTransientException) {
            this.errorMessage = exception.message ?: this.errorMessage
            this.hasTransientError = true
        } catch (e: ApproovIOFatalException) {
            this.errorMessage = exception.message ?: this.errorMessage
            this.hasFatalError = true
        }
    }

    /**
     * Builds a local ShipmentResult object in order to extract the Shipment data object, and
     *  determine that the ShipmentResponse was successful.
     */
    private fun buildShipmentResponse(bodyPayload: String) {
        val json = JsonParser.toJSONObject(bodyPayload)
        val shipmentResult = ShipmentResult(json)
        this.hasData = shipmentResult.hasData()
        this.shipment = shipmentResult.get()
    }

    /**
     * Tells that the response is a successful one.
     *
     * @return True when the response has a success http status code.
     */
    fun isOk(): Boolean {
        return this.isOk
    }

    /**
     * Tells that the response is a not successful one.
     *
     * @return True when the response have a http status code for a client or server error.
     */
    fun isNotOk(): Boolean {
        return !this.isOk
    }

    fun hasNoData(): Boolean {
        return !this.hasData
    }

    /**
     * Accesses the error message for the Shipment response.
     */
    fun errorMessage(): String {
        return this.errorMessage
    }

    /**
     * Get the Shipment data object.
     */
    fun get(): Shipment? {
        return this.shipment
    }
}


/**
 * This class accepts a JSONObject? and will extract from it the Shipment data object and if the
 *  result of parsing it was successful.
 *
 * @param json The json object from the Shipment response.
 */
class ShipmentResult (
    private val json: JSONObject?
) {

    private var hasData = false
    private var shipment: Shipment? = null

    init {
        if (this.json != null) {
            try {
                this.shipment = Shipment(
                    this.json.getInt("id"),
                    this.json.getString("description"),
                    this.json.getDouble("gratuity"),
                    this.json.getString("pickupName"),
                    LatLng(this.json.getDouble("pickupLatitude"), this.json.getDouble("pickupLongitude")),
                    this.json.getString("deliveryName"),
                    LatLng(this.json.getDouble("deliveryLatitude"), this.json.getDouble("deliveryLongitude")),
                    ShipmentState.values().get(this.json.getInt("state"))
                )

                this.hasData = true

            } catch (e: JSONException) {
                Log.e(TAG, e.message)
                this.hasData = false
            }
        }
    }

    /**
     * Tells that the we where able to build the Shipment from the given json object.
     *
     * @return True when the Shipment was built successfully.
     */
    fun hasData(): Boolean {
        return this.hasData
    }

    /**
     * Get the Shipment data object.
     */
    fun get(): Shipment? {
        return this.shipment
    }
}

/**
 * The data class to represent a shipment.
 *
 * @param id the unique shipment identifier
 * @param description the brief description of the shipment
 * @param gratuity the gratuity associated with the shipment
 * @param pickupName the name of the shipment's pickup location
 * @param pickupLocation the shipment's pickup location
 * @param deliveryName the name of the shipment's delivery location
 * @param deliveryLocation the shipment's delivery location
 * @param state the state of the shipment
 */
data class Shipment(
    val id: Int,
    val description: String,
    val gratuity: Double,
    val pickupName: String,
    val pickupLocation: LatLng,
    val deliveryName: String,
    val deliveryLocation: LatLng,
    var state: ShipmentState
) {
    /**
     * Builds a string to represent this data object.
     *
     * @return This object public data as a string.
     */
    override fun toString(): String {
        return "Shipment(id=$id, description='$description', gratuity=$gratuity," +
                "pickupName='$pickupName', pickupLocation=$pickupLocation," +
                "deliveryName='$deliveryName', deliveryLocation=$deliveryLocation," +
                "state=$state)"
    }

    /**
     * Advances the Shipment state to the next logical state.
     *
     * @return The next logical state for the Shipment.
     */
    val nextState: ShipmentState
        get() {
            return when (state) {
                ShipmentState.DELIVERED -> ShipmentState.DELIVERED
                else -> ShipmentState.values()[state.ordinal + 1]
            }
        }
}

