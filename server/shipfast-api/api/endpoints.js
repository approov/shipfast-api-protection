const model = require('./model')
const log = require('./utils/logging')
const express = require('express')
const router = express.Router()
const crypto = require('crypto')
const request = require('./utils/request')

const hash_user_claim = function(req) {
  return request.hash_header_payload_claim(req, 'authorization', 'sub')
}

const log_identifier = function(user_uid, req) {
  return request.log_simple_identifier(user_uid, req.params.version + ': endpoint.js')
}

// The '/shipments/nearest_shipment' GET request route
router.get('/:version/shipments/nearest_shipment', function(req, res) {

  const user_uid = hash_user_claim(req)
  const log_id = log_identifier(user_uid, req)

  log.info("/shipments/nearest_shipment", log_id)

  user_agent = req.headers["user-agent"]
  log.info("USER AGENT: " + user_agent, log_id)

  // Retrieve the location data from the request headers
  var latitude = parseFloat(req.get('DRIVER-LATITUDE'))
  let longitude = parseFloat(req.get('DRIVER-LONGITUDE'))

  if (!latitude || !longitude) {
    log.error("Location data not specified or in the wrong format\n", log_id)
    res.status(400).send()
    return
  }

  log.debug("Latitude: " + latitude, log_id)
  log.debug("Longitude: " + longitude, log_id)

  // Calculate the nearest shipment to the given location
  let nearestShipment = model.calculateNearestShipment(latitude, longitude, user_uid, user_agent)

  if (nearestShipment && nearestShipment.error) {
    log.error("Error:\n" + nearestShipment.error, log_id)
    res.status(400).json(nearestShipment)
    return
  }

  if (nearestShipment) {
    log.info("Nearest Shipment: " + nearestShipment.description, log_id)
    res.status(200).json(nearestShipment)
    return
  }

  log.warning("Unable to find a nearest shipment...\n", log_id)
  res.status(400).send()
})

// The '/shipments/delivered' GET request route
router.get('/:version/shipments/delivered', function(req, res) {

  const user_uid = hash_user_claim(req)
  const log_id = log_identifier(user_uid, req)

  log.info("/shipments/delivered", log_id)

  // Calculate the array of delivered shipments
  let deliveredShipments = model.getDeliveredShipments(user_uid)

  res.status(200).json(deliveredShipments)
})

// The '/shipments/active' GET request route
router.get('/:version/shipments/active', function(req, res) {

  const user_uid = hash_user_claim(req)
  const log_id = log_identifier(user_uid, req)

  log.info("/shipments/active", log_id)

  const activeShipment = model.getActiveShipment(user_uid)
  if (!activeShipment) {
    log.warning("No active shipment found\n", log_id)
    res.status(200).json({})
    return
  }

  log.debug("Active Shipment:", log_id)
  log.raw(activeShipment.description, log_id)
  res.status(200).json(activeShipment)
})

// The '/shipments/:shipmentID' GET request route
router.get('/:version/shipments/:shipmentID', function(req, res) {

  const user_uid = hash_user_claim(req)
  const log_id = log_identifier(user_uid, req)

  log.info("/shipments/:shipmentID", log_id)

  // Retrieve the shipment ID from the request header
  let shipmentID = parseInt(req.params.shipmentID)
  if (!Number.isInteger(shipmentID)) {
    log.error("Shipment ID not specified or in the wrong format\n", log_id)
    res.status(400).send()
    return
  }

  // Find the shipment with the given ID
  let shipment = model.getShipment(shipmentID, user_uid)
  if (!shipment) {
    log.error("No shipment found for ID: " + shipmentI, log_id)
    res.status(404).send()
    return
  }

  log.debug("Shipment ID: " + shipmentID, log_id)
  log.debug("Shipment Description: " + shipment.description, log_id)

  res.status(200).json(shipment)
})

// The '/shipments/update_state/:shipmentID' POST request route
router.post('/:version/shipments/update_state/:shipmentID', function(req, res) {

  const user_uid = hash_user_claim(req)
  const log_id = log_identifier(user_uid, req)

  log.info("/shipments/update_state/:shipmentID", log_id)

  // Retrieve the shipment ID from the request header
  let shipmentID = parseInt(req.params.shipmentID)
  if (!Number.isInteger(shipmentID)) {
    log.error("Shipment ID not specified or in the wrong format\n", log_id)
    res.status(400).send()
    return
  }

  // Retrieve the location data from the request headers
  let latitude = parseFloat(req.get('DRIVER-LATITUDE'))
  let longitude = parseFloat(req.get('DRIVER-LONGITUDE'))
  if (!latitude || !longitude) {
    log.error("Location data not specified or in the wrong format\n", log_id)
    res.status(400).send()
    return
  }

  log.debug("Latitude: " + latitude, log_id)
  log.debug("Longitude: " + longitude, log_id)

  // Retrieve the new shipment state from the request header
  let newState = parseInt(req.get('SHIPMENT-STATE'))
  if (!Number.isInteger(newState)) {
    log.error("Shipment state not specified or in the wrong format\n", log_id)
    res.status(400).send()
    return
  }

  result = model.updateShipmentState(shipmentID, newState, user_uid)

  if (result && result.error) {
    log.error(result.error, log_id)
    res.status(400).send()
  }

  log.warning("State of shipment " + shipmentID + " updated to " + newState + "\n", log_id)
  res.status(200).send()
})

module.exports = router
