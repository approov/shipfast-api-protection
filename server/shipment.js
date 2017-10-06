/*****************************************************************************
 * Project:     ShipFast API Protection (Server)
 * File:        shipment.js
 * Original:    Created on 29 Sept 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 * 
 * This file contains the definition of the Shipment class.
 *****************************************************************************/

 // The various states a Shipment may be in
 const SHIPMENT_STATE = {
    READY: 0,
    ACCEPTED: 1,
    COLLECTED: 2,
    SHIPPED: 3
}

// The Shipment class
class Shipment {
    constructor(id, description, gratuity,
            pickupName, pickupLatitude, pickupLongitude,
            deliveryName, deliveryLatitude, deliveryLongitude) {
        this.id = id // The unique ID of the shipment (integer)
        this.description = description // The brief description of the shipment (string)
        this.gratuity = gratuity // The gratuity associated with the shipment (float)
        this.pickupName = pickupName // The name of the shipment's pickup location (string)
        this.pickupLatitude = pickupLatitude // The location latitude of the shipment's pickup location (float)
        this.pickupLongitude = pickupLongitude // The location longitude of the shipment's pickup location (float)
        this.deliveryName = deliveryName // The name of the shipment's delivery location (string)
        this.deliveryLatitude = deliveryLatitude // The location latitude of the shipment's delivery location (float)
        this.deliveryLongitude = deliveryLongitude // The location longitude of the shipment's delivery location (float)
        this.state = SHIPMENT_STATE.READY // The current state of the shipment (SHIPMENT_STATE)
    }

    getID() {
        return this.id
    }

    getDescription() {
        return this.description
    }

    getGratuity() {
        return this.gratuity
    }

    getPickupName() {
        return this.pickupName
    }
    
    getPickupLatitude() {
        return this.pickupLatitude
    }
    
    getPickupLongitude() {
        return this.pickupLongitude
    }
    
    getDeliveryName() {
        return this.deliveryName
    }
    
    getDeliveryLatitude() {
        return this.deliveryLatitude
    }
    
    getDeliveryLongitude() {
        return this.deliveryLongitude
    }
    
    getState() {
        return this.state
    }

    setState(newState) {
        this.state = newState
    }
}

// Add the state and class to exports
module.exports = {
    SHIPMENT_STATE,
    Shipment    
}