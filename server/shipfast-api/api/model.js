const Shipment = require('./shipment').Shipment
const SHIPMENT_STATE = require('./shipment').SHIPMENT_STATE
const MersenneTwister = require('mersenne-twister')
const Rand = new MersenneTwister(Date.now())
const log = require('./utils/logging')

// Define various attributes for generating sample shipment data
const MIN_GRATUITY = 0
const MAX_GRATUITY = 30
const LAT_LNG_OFFSET = 0.1

// Shipments
const TOTAL_SHIPMENTS_TO_CREATE = 20 // NEVER TWEAK TO BE LESS THEN 10, unless for testing purposes.
const MAX_TOTAL_SHIPMENT_COUNT = TOTAL_SHIPMENTS_TO_CREATE * 2 // 10 * 2 = 20
const MAX_DELIVERED_SHIPMENTS = TOTAL_SHIPMENTS_TO_CREATE / 2 // 10 / 2 = 5
var TOTAL_SHIPMENT_COUNT = 0
var NEXT_SHIPMENT_TO_DELETE = 0

const FAKER = require('faker');

// The shipments object
var shipments = {}

function randomNumber(min = 0, max = Number.MAX_SAFE_INTEGER) {

    return FAKER.random.number({
        'min': min,
        'max': max
    })
}

function randFloat() {

    value = Rand.random()

    if (value < 1.0) {
        return value
    }

    randFloat()
}

// Calculate and return the distance in miles between the two given points using the haversine formula
function calculateDistance(originLatitude, originLongitude, destinationLatitude, destinationLongitude) {

    var radiusEarth = 3961 // radius of Earth in miles
    var dLat = deg2rad(destinationLatitude - originLatitude)
    var dLon = deg2rad(destinationLongitude - originLongitude)
    var squareHalfChordLen = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(deg2rad(originLatitude))
        * Math.cos(deg2rad(destinationLatitude)) * Math.sin(dLon/2) * Math.sin(dLon/2)
    var angularRadDistance = 2 * Math.atan2(Math.sqrt(squareHalfChordLen), Math.sqrt(1 - squareHalfChordLen))
    var distance = radiusEarth * angularRadDistance
    return parseInt(distance)
}

// Convert the given degrees angle to radians
function deg2rad(deg) {
    return deg * (Math.PI/180)
}

// A function to populate this model with a collection of sample shipment data base on a given location
function populateShipments(originLatitude, originLongitude) {

    // Generate the sample data
    var randMultiplier = 2.0 * LAT_LNG_OFFSET
    var randOffset = LAT_LNG_OFFSET

    for (var i = 0; i < TOTAL_SHIPMENTS_TO_CREATE; i++) {
        var shipmentID = TOTAL_SHIPMENT_COUNT // + i + 1
        var pickupName = FAKER.address.streetAddress()
        var deliveryName = FAKER.address.streetAddress()
        var description = FAKER.name.findName() + " #" + randomNumber(1000, 9999)
        var gratuity = shipmentID % 2 == 0 ? Math.floor((Rand.random() * MAX_GRATUITY) + MIN_GRATUITY) : MIN_GRATUITY
        var pickupLatitude = originLatitude + ((randFloat() * randMultiplier) - randOffset)
        var pickupLongitude = originLongitude + ((randFloat() * randMultiplier) - randOffset)
        var deliveryLatitude = originLatitude + ((randFloat() * randMultiplier) - randOffset)
        var deliveryLongitude = originLongitude + ((randFloat() * randMultiplier) - randOffset)
        var shipment = new Shipment(shipmentID, description, gratuity,
            pickupName, pickupLatitude, pickupLongitude,
            deliveryName, deliveryLatitude, deliveryLongitude)
        shipments[shipmentID] = shipment

        TOTAL_SHIPMENT_COUNT++

        if (Object.keys(shipments).length > MAX_TOTAL_SHIPMENT_COUNT) {
            // remove first element
            delete shipments[NEXT_SHIPMENT_TO_DELETE]
            NEXT_SHIPMENT_TO_DELETE++
        }
    }
}

const reCalculateNearestShipment = function(originLatitude, originLongitude) {

    populateShipments(originLatitude, originLongitude)

    return findNearestShipment(originLatitude, originLongitude)
}

// A function to calculate and return the nearest shipment to a given location
const calculateNearestShipment = function(originLatitude, originLongitude) {

    // Ensure we've populated the model with some sample data for this session
    if (Object.keys(shipments).length == 0) {
        populateShipments(originLatitude, originLongitude)
    }

    nearestShipment = findNearestShipment(originLatitude, originLongitude)

    if (!nearestShipment) {
        return reCalculateNearestShipment(originLatitude, originLongitude)
    }

    return nearestShipment
}

function findNearestShipment(originLatitude, originLongitude) {

    // Iterate through the shipments and find the one closest to our given location
    var nearestShipment
    var maxDistance = 15

    Object.entries(shipments).forEach(
        ([shipmentID, shipment]) => {
            if (shipment.getState() == SHIPMENT_STATE.READY) {
                var distance = calculateDistance(originLatitude, originLongitude,
                    shipment.getPickupLatitude(), shipment.getPickupLongitude())

                if (distance < maxDistance) {
                    maxDistance = distance
                    shipment.setPickupDistance(distance)
                    nearestShipment = shipment
                }
            }
        }
    )

    return nearestShipment
}

const updateShipmentState = function(shipmentID, newState) {

    // Find the shipment with the given ID
    var shipment = getShipment(shipmentID)

    if (!shipment) {
      log.error("\nNo shipment found for ID " + shipmentID + "\n")
      return false
    }

    // Perform basic validation of the new shipment state
    if (newState <= 0
        || newState != shipment.getState() + 1
        || newState >= Object.keys(SHIPMENT_STATE).length) {
      log.error("\nShipment state invalid\n")
      return false
    }

    shipment.setState(newState)

    return true
}

// A function to calculate and return an array of shipments in a 'DELIVERED' state
const getDeliveredShipments = function() {

    var countDelivered = 0
    var deliveredShipments = []

    Object.entries(shipments).reverse().filter(
        ([shipmentID, shipment]) => {

            // We don't need to display all the delivered shipments in the mobile app.
            if (countDelivered >= MAX_DELIVERED_SHIPMENTS) {
                return false
            }

            if (shipment.getState() == SHIPMENT_STATE.DELIVERED) {
                deliveredShipments.push(shipment)
                countDelivered++
                return true
            }
        }
    )

    return deliveredShipments
}

// A function to retrieve the active shipment, if available (i.e. in an 'ACCEPTED' or 'COLLECTED' state)
const getActiveShipment = function() {

    for (var shipmentID in shipments) {
        var shipment = shipments[shipmentID]
        if (shipment.getState() == SHIPMENT_STATE.ACCEPTED
         || shipment.getState() == SHIPMENT_STATE.COLLECTED) {
            return shipment
        }
    }

    return undefined
}

// A function to return the shipment with the given ID (or 'undefined' if not found)
const getShipment = function(shipmentID) {
    return shipments[shipmentID]
}

// Add the model utility functions to the exports
module.exports = {
    calculateNearestShipment: calculateNearestShipment,
    reCalculateNearestShipment: reCalculateNearestShipment,
    getDeliveredShipments: getDeliveredShipments,
    getActiveShipment: getActiveShipment,
    getShipment: getShipment,
    updateShipmentState: updateShipmentState,
    SHIPMENT_STATE: SHIPMENT_STATE
}
