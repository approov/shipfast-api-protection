/*****************************************************************************
 * Project:     ShipFast API Protection (App)
 * File:        Shipment.kt
 * Original:    Created on 3 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * The class which represents a shipment.
 *****************************************************************************/

package com.criticalblue.shipfast

import com.google.android.gms.maps.model.LatLng

/**
 * The enumeration of various states a shipment may be in
 */
enum class ShipmentState(val nextStateActionName: String) {
    READY("Accept"),
    ACCEPTED("Collect"),
    COLLECTED("Deliver"),
    DELIVERED("")
}

/**
 * The class to represent a shipment.
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
class Shipment(val id: Int, val description: String, val gratuity: Double,
               val pickupName: String, val pickupLocation: LatLng,
               val deliveryName: String, val deliveryLocation: LatLng,
               var state: ShipmentState) {

    override fun toString(): String {
        return "Shipment(id=$id, description='$description', gratuity=$gratuity," +
                "pickupName='$pickupName', pickupLocation=$pickupLocation," +
                "deliveryName='$deliveryName', deliveryLocation=$deliveryLocation," +
                "state=$state)"
    }

    val nextState: ShipmentState
        get() {
            when (state) {
                ShipmentState.DELIVERED -> return ShipmentState.DELIVERED
                else -> return ShipmentState.values()[state.ordinal + 1]
            }
        }
}