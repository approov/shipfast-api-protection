const Shipment = require('./shipment').Shipment
const SHIPMENT_STATE = require('./shipment').SHIPMENT_STATE
const MersenneTwister = require('mersenne-twister')
const Rand = new MersenneTwister(Date.now())
const log = require('./utils/logging')
const FAKER = require('faker');
const config = require('./config/server').config
const randomLocation = require('random-location')

// Define various attributes for generating sample shipment data
const MIN_GRATUITY = 0
const MAX_GRATUITY = 30
const DRIVER_COORDINATES = {
  latitude: config.DRIVER_LATITUDE,
  longitude: config.DRIVER_LONGITUDE
}

// DANGER: tweaking any of the following constant values can lead to unexpected behavior when Shipraider queries the API.
//
// The reason is that when we try to find the nearest shipment we try to find one stored in `shipments`, and if none is
//  within the MAX_SHIPMENT_DISTANCE_IN_METRES, we generate a new one, and when the MAX_TOTAL_SHIPMENT_COUNT is reached
//  we start to remove the oldest entry from `shipments` in order to protect against memory leaks.
// So if the MAX_SHIPMENT_DISTANCE_IN_METRES is to short it will be hard to find a shipment, and new ones will be
//  constantly generated, meaning that when a Shipraider asks for a batch it can have the first entries on that batch
//   already removed from `shipments`, therefore any subsequent calls to that shipmentID will fail.
const MAX_SHIPMENT_DISTANCE_IN_METRES = 20000
const TOTAL_SHIPMENTS_TO_CREATE = 100 // 10 * 5 = 50 shipments to create in each batch.
const MAX_TOTAL_SHIPMENT_COUNT = TOTAL_SHIPMENTS_TO_CREATE * 2 // 50 * 5 = 250 shipments max in memory at any given time.
const MAX_DELIVERED_SHIPMENTS = 10 // Max of last delivered shipments to return to the mobile app.

let TOTAL_SHIPMENT_COUNT = 0 // keeps track of total shipments created since server was started/re-started.
let NEXT_SHIPMENT_TO_DELETE = 0 // keeps track of the oldest shipment ID in the `shipments` object.

// The shipments object
let shipments = {}

function randomNumber(min = 0, max = Number.MAX_SAFE_INTEGER) {

    return FAKER.random.number({
        'min': min,
        'max': max
    })
}

function calculateShipmentGratuity(shipmentID) {
    let gratuity = shipmentID % 2 == 0 ? Math.floor((Rand.random() * MAX_GRATUITY) + 1) : 0
    return config.CURRENCY_SYMBOL + gratuity
}

// A function to populate this model with a collection of sample shipment data base on a given location
function populateShipments(originLatitude, originLongitude) {

    log.info("--------------------------- START ------------------------")
    log.info("MAX_TOTAL_SHIPMENT_COUNT: " + MAX_TOTAL_SHIPMENT_COUNT)
    log.info("TOTAL_SHIPMENTS_TO_CREATE: " + TOTAL_SHIPMENTS_TO_CREATE)
    log.info("TOTAL_SHIPMENT_COUNT_BEFORE: " + TOTAL_SHIPMENT_COUNT)

    for (let i = 0; i < TOTAL_SHIPMENTS_TO_CREATE; i++) {
        let shipmentID = TOTAL_SHIPMENT_COUNT // + i + 1
        let pickupName = FAKER.address.streetAddress()
        let deliveryName = FAKER.address.streetAddress()
        let description = FAKER.name.findName() + " #" + randomNumber(1000, 9999)
        let gratuity = calculateShipmentGratuity(shipmentID)

        // Get pickup random coordinates from within `MAX_SHIPMENT_DISTANCE_IN_METRES` of the `DRIVER_COORDINATES`.`
        const pickup = randomLocation.randomCirclePoint(DRIVER_COORDINATES, MAX_SHIPMENT_DISTANCE_IN_METRES)

        // Get deliver coordinates at the exactly `MAX_SHIPMENT_DISTANCE_IN_METRES * 0.3` from the pickup coordinates..
        const deliver = randomLocation.randomCircumferencePoint(pickup, MAX_SHIPMENT_DISTANCE_IN_METRES * 0.3)

        shipments[shipmentID] = new Shipment(
            shipmentID,
            description,
            gratuity,
            pickupName,
            pickup.latitude,
            pickup.longitude,
            deliveryName,
            deliver.latitude,
            deliver.longitude,
        )

        TOTAL_SHIPMENT_COUNT++

        if (Object.keys(shipments).length > MAX_TOTAL_SHIPMENT_COUNT) {

            log.warning("SHIPMENT_TO_DELETE: " + NEXT_SHIPMENT_TO_DELETE)

            // remove first element
            delete shipments[NEXT_SHIPMENT_TO_DELETE]
            NEXT_SHIPMENT_TO_DELETE++
        }
    }

    log.info("TOTAL_SHIPMENT_COUNT_AFTER: " + TOTAL_SHIPMENT_COUNT)
    log.info("--------------------------- END ------------------------")
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

    let pickup = {
        latitude: originLatitude,
        longitude: originLongitude
    }

    // Iterate through the shipments and find the one closest to our given location
    for (let shipmentID in shipments) {

        let shipment = shipments[shipmentID]

        if (shipment.getState() == SHIPMENT_STATE.READY) {

            let deliver = {
                latitude: shipment.deliveryLatitude,
                longitude: shipment.deliveryLongitude
            }

            let distance_in_metres = Math.floor(randomLocation.distance(pickup, deliver))

            if (distance_in_metres <= MAX_SHIPMENT_DISTANCE_IN_METRES) {
                shipment.setPickupDistance(formatShipmentDistance(distance_in_metres))
                return shipment
            }
        }
    }

    return undefined
}

const formatShipmentDistance = function(distance) {

    let distance_unit

    if (config.DISTANCE_IN_MILES === true) {
        distance = distance * 0.00062137 // metres to miles
        distance_unit = "miles"
    } else {
        distance = distance / 1000
        distance_unit = "kms"
    }

    formatted_distance = Math.floor(distance)

    return formatted_distance + " " + distance_unit
}

const updateShipmentState = function(shipmentID, newState) {

    // Find the shipment with the given ID
    let shipment = getShipment(shipmentID)

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

    let countDelivered = 0
    let deliveredShipments = []

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

    for (let shipmentID in shipments) {
        let shipment = shipments[shipmentID]
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
