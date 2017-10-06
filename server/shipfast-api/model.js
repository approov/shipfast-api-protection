/*****************************************************************************
 * Project:     ShipFast API Protection (Server)
 * File:        model.js
 * Original:    Created on 29 Sept 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 * 
 * This file contains the server business model.
 *****************************************************************************/

const Shipment = require('./shipment').Shipment
const SHIPMENT_STATE = require('./shipment').SHIPMENT_STATE
const MersenneTwister = require('mersenne-twister');
const Rand = new MersenneTwister(Date.now());

// Define various attributes for generating sample shipment data
const MIN_GRATUITY = 0
const MAX_GRATUITY = 30
const LAT_LNG_OFFSET = 0.5
const SHIPMENT_COUNT = 7
var PICKUP_LOCATIONS = [
    'ParcelFarce Depot 192',
    'Q&B DIY Centre',
    'Quidland',
    'Magnific Wines',
    'Stone & Waters Books',
    'Dobby\'s Gardens',
    'OfficeMean Suppliers'
]
var DELIVERY_LOCATIONS = [
    'Hugwarts School',
    'LetGo Land',
    '123 Fake Street',
    'Suspicious Whirring Blue Box',
    'Terok Nor Promenade',
    'Getwell Soon Hospital',
    'Llanfairpwllgwyngyll'
]
var SHIPMENT_DESCRIPTIONS = [
    'Bob\'s Consignment #58332',
    'Alice\'s Consignment #42981',
    'Eve\'s Consignment #91113',
    'Mary\'s Consignment #12125',
    'Joe\'s Consignment #17700',
    'Titus\' Consignment #77012',
    'Pete\'s Consignment #12589'
]

// The collection of shipments
var shipments = {}

// A function to shuffle an array in-place randomly
function shuffleArray(array) {
    for (var i = array.length - 1; i > 0; i--) {
        var j = Math.floor(Rand.random() * (i + 1))
        var temp = array[i]
        array[i] = array[j]
        array[j] = temp
    }
    return array
}

// A function to calculate and return the distance between two points
function calculateDistance(originLatitude, originLongitude, destinationLatitude, destinationLongitude) {
    var lat = originLatitude - destinationLatitude
    var lon = originLongitude - destinationLongitude
    return Math.abs(Math.sqrt(lat*lat + lon*lon))
}

// A function to populate this model with a collection of sample shipment data base on a given location
function populateShipments(originLatitude, originLongitude) {

    // Shuffle the pickup and delivery locations and the shipment descriptions to ensure these are different between sessions
    shuffleArray(PICKUP_LOCATIONS)
    shuffleArray(DELIVERY_LOCATIONS)
    shuffleArray(SHIPMENT_DESCRIPTIONS)

    // Generate the sample data
    for (i = 0; i < SHIPMENT_COUNT; i++) {
        var shipmentID = i + 1
        var description = SHIPMENT_DESCRIPTIONS[i]
        var gratuity = i % 2 == 0 ? Math.floor((Rand.random() * MAX_GRATUITY) + MIN_GRATUITY) : MIN_GRATUITY
        var pickupName = PICKUP_LOCATIONS[i]
        var pickupLatitude = originLatitude + ((Rand.random() * LAT_LNG_OFFSET) - LAT_LNG_OFFSET)
        var pickupLongitude = originLongitude + ((Rand.random() * LAT_LNG_OFFSET) - LAT_LNG_OFFSET)
        var deliveryName = DELIVERY_LOCATIONS[i]
        var deliveryLatitude = originLatitude + ((Rand.random() * LAT_LNG_OFFSET) - LAT_LNG_OFFSET)
        var deliveryLongitude = originLongitude + ((Rand.random() * LAT_LNG_OFFSET) - LAT_LNG_OFFSET)
        var shipment = new Shipment(shipmentID, description, gratuity,
            pickupName, pickupLatitude, pickupLongitude,
            deliveryName, deliveryLatitude, deliveryLongitude)
        shipments[shipmentID] = shipment
    }
    
    console.log('Generated Shipments:')
    console.log(shipments)
}

// A function to calculate and return the nearest shipment to a given location
const calculateNearestShipment = function(originLatitude, originLongitude) {

    // Ensure we've populated the model with some sample data for this session
    if (Object.keys(shipments).length == 0) {
        populateShipments(originLatitude, originLongitude)
    }

    // Iterate through the shipments and find the one closest to our given location
    var minDistance = Number.MAX_VALUE
    var nearestShipment
    Object.entries(shipments).forEach(
        ([shipmentID, shipment]) => {
            if (shipment.getState() == SHIPMENT_STATE.READY) {
                var distance = calculateDistance(originLatitude, originLongitude,
                    shipment.getPickupLatitude(), shipment.getPickupLongitude())
                if (distance < minDistance) {
                    minDistance = distance
                    nearestShipment = shipment
                }
            }
        }
    );
    return nearestShipment
}

// A function to calculate and return an array of shipments in a 'DELIVERED' state
const getDeliveredShipments = function() {

    var deliveredShipments = []
    Object.entries(shipments).forEach(
        ([shipmentID, shipment]) => {
            if (shipment.getState() == SHIPMENT_STATE.DELIVERED) {
                deliveredShipments.push(shipment)
            }
        }
    );
    return deliveredShipments
}

// A function to return the shipment with the given ID (or 'undefined' if not found)
const getShipment = function(shipmentID) {
    return shipments[shipmentID]
}

// Add the model utility functions to the exports
module.exports = {
    calculateNearestShipment: calculateNearestShipment,
    getDeliveredShipments: getDeliveredShipments,
    getShipment: getShipment,
    SHIPMENT_STATE: SHIPMENT_STATE
}