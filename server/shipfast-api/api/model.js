const Shipment = require('./shipment').Shipment
const SHIPMENT_STATE = require('./shipment').SHIPMENT_STATE
const MersenneTwister = require('mersenne-twister')
const Rand = new MersenneTwister(Date.now())
const log = require('./utils/logging')
const FAKER = require('faker');
const config = require('./config/server').config
const randomLocation = require('random-location')

// We will cache generated shipments by user id.
// @link https://github.com/node-cache/node-cache
const NodeCache = require( "node-cache" );
const cache = new NodeCache(
    {
        stdTTL: 7200, // 2 hours
        checkperiod: 120, // 2 minutes
        useClones: false // we get back a reference to the value in memory
    }
);

// Define various attributes for generating sample shipment data
const MIN_GRATUITY = 0
const MAX_GRATUITY = 30
const DRIVER_COORDINATES = {
  latitude: config.DRIVER_LATITUDE,
  longitude: config.DRIVER_LONGITUDE
}

// ATTENTION:
//
// Tweaking any of the following constant values will change how shipments are
// created and stored in the cache, thus affecting how many can be returned has
// being within the sweep radius used by default for when Shipraider queries the
// API.
//
// The following constant values guarantee that we always match around 10 to
// 20 shipments for each time we click in search shipments on Shipraider.
const MAX_SHIPMENT_DISTANCE_IN_METRES = 20000
const TOTAL_SHIPMENTS_TO_CREATE = 100 // 10 * 5 = 50 shipments to create in each batch.
const MAX_TOTAL_SHIPMENT_COUNT = TOTAL_SHIPMENTS_TO_CREATE * 4 // 50 * 5 = 250 shipments max in memory at any given time.

const MAX_DELIVERED_SHIPMENTS = 10 // Max of last delivered shipments to return to the mobile app.

let TOTAL_SHIPMENT_COUNT = 0 // keeps track of total shipments created since server was started/re-started.

const cache_delete = function(key) {
    return cache.del(key)
}

const cache_has = function(key) {
    return cache.has(key)
}

const cache_keys = function() {
    return cache.keys()
}

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
function populateShipments(originLatitude, originLongitude, user_uid) {

    const driver_coordinates = {
        latitude: originLatitude,
        longitude: originLongitude
    }

    let gratuity = config.CURRENCY_SYMBOL + "0"
    let shipments = {}

    if (cache.has(user_uid)) {
        shipments = cache.get(user_uid)
    }

    for (let i = 0; i < TOTAL_SHIPMENTS_TO_CREATE; i++) {
        let shipmentID = TOTAL_SHIPMENT_COUNT // + i + 1
        let pickupName = FAKER.address.streetAddress()
        let deliveryName = FAKER.address.streetAddress()
        let description = FAKER.name.findName() + " #" + randomNumber(1000, 9999)

        // For demos purposes we want the first shipments to have zero gratuity.
        if (i >= 3) {
            gratuity = calculateShipmentGratuity(shipmentID)
        }

        // Get pickup random coordinates from within `MAX_SHIPMENT_DISTANCE_IN_METRES` of the `DRIVER_COORDINATES`.`
        const pickup = randomLocation.randomCirclePoint(driver_coordinates, MAX_SHIPMENT_DISTANCE_IN_METRES * 0.5)

        // Get deliver coordinates at the exactly `MAX_SHIPMENT_DISTANCE_IN_METRES * 0.3` from the pickup coordinates..
        const deliver = randomLocation.randomCirclePoint(driver_coordinates, MAX_SHIPMENT_DISTANCE_IN_METRES * 0.5)

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

        if (Object.keys(shipments).length >= MAX_TOTAL_SHIPMENT_COUNT) {
            break
        }
    }

    cache.set(user_uid, shipments)
}

const reCalculateNearestShipment = function(originLatitude, originLongitude, user_uid) {

    populateShipments(originLatitude, originLongitude, user_uid)

    return findNearestShipment(originLatitude, originLongitude, user_uid)
}

// A function to calculate and return the nearest shipment to a given location
const calculateNearestShipment = function(originLatitude, originLongitude, user_uid, user_agent) {

    // Ensure we've populated the model with some sample data for this session
    if (!cache.has(user_uid) && user_agent.startsWith("okhttp")) {

        // Store the coordinates provided by the mobile app. This coordinates
        // will be used only to generate more shipments in a future request,
        // where we are not able to find a nearest shipment for Shipraider.
        cache.set("coordinates", {
            [user_uid]: {
                latitude: originLatitude,
                longitude: originLongitude
            }
        })

        populateShipments(originLatitude, originLongitude, user_uid)
    } else if (!cache.has(user_uid) && !user_agent.startsWith("okhttp")) {
        return {error: "Please use first the ShipFast mobile app in the same demo stage you want to try from ShipRaider."}
    }

    nearestShipment = findNearestShipment(originLatitude, originLongitude, user_uid)

    if (!nearestShipment) {

        let location

        if (user_agent.startsWith("okhttp")) {
            // For mobile app requests
            location = {
                latitude: originLatitude,
                longitude: originLongitude
            }
        } else {
            // For Shipraider requests
            location = cache.get("coordinates")[user_uid]
        }

        return reCalculateNearestShipment(location.latitude, location.ongitude, user_uid)
    }

    return nearestShipment
}

function findNearestShipment(originLatitude, originLongitude, user_uid) {

    let pickup = {
        latitude: originLatitude,
        longitude: originLongitude
    }

    let shipments = cache.get(user_uid)

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

const updateShipmentState = function(shipmentID, newState, user_uid) {

    let shipment = getShipment(shipmentID, user_uid)

    if (!shipment) {
      return {error: "No shipment found for ID " + shipmentID}
    }

    // Perform basic validation of the new shipment state
    if (newState <= 0
        || newState != shipment.getState() + 1
        || newState >= Object.keys(SHIPMENT_STATE).length) {
      return {error: "Shipment state invalid"}
    }

    return shipment.setState(newState)
}

// A function to calculate and return an array of shipments in a 'DELIVERED' state
const getDeliveredShipments = function(user_uid) {

    let countDelivered = 0
    let deliveredShipments = []

    let shipments = cache.get(user_uid)

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
const getActiveShipment = function(user_uid) {

    let shipments = cache.get(user_uid)

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
const getShipment = function(shipmentID, user_uid) {
    if (cache.has(user_uid)) {
        let shipments = cache.get(user_uid)
        return shipments[shipmentID]
    }

    return undefined
}

// Add the model utility functions to the exports
module.exports = {
    calculateNearestShipment: calculateNearestShipment,
    reCalculateNearestShipment: reCalculateNearestShipment,
    getDeliveredShipments: getDeliveredShipments,
    getActiveShipment: getActiveShipment,
    getShipment: getShipment,
    updateShipmentState: updateShipmentState,
    SHIPMENT_STATE: SHIPMENT_STATE,
    cache_delete,
    cache_has,
    cache_keys
}
