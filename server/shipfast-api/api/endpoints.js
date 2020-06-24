const model = require('./model')
const log = require('./utils/logging')
const express = require('express')
const router = express.Router()
const crypto = require('crypto')

const hash = function(text) {
  return crypto.createHash('sha256').update(text, 'utf-8').digest('hex')
}

// The '/shipments/nearest_shipment' GET request route
router.get('/shipments/nearest_shipment', function(req, res) {

  user_agent = req.headers["user-agent"]
  log.info("\nUSER AGENT: " + user_agent)

  user_uid = hash(req.user.sub)

  log.info("\nENDPOINT: /shipments/nearest_shipment")

  // Retrieve the location data from the request headers
  var latitude = parseFloat(req.get('DRIVER-LATITUDE'))
  let longitude = parseFloat(req.get('DRIVER-LONGITUDE'))

  if (!latitude || !longitude) {
    log.error("\nLocation data not specified or in the wrong format\n")
    res.status(400).send()
    return
  }

  log.debug("\nLatitude: " + latitude + "\nLongitude: " + longitude + "\n")

  // Calculate the nearest shipment to the given location
  let nearestShipment = model.calculateNearestShipment(latitude, longitude, user_uid, user_agent)

  if (nearestShipment && nearestShipment.error) {
    log.error("\nError:\n" + nearestShipment.error)
    res.status(400).json(nearestShipment)
    return
  }

  if (nearestShipment) {
    log.info("Nearest Shipment: " + nearestShipment.description)

    res.status(200).json(nearestShipment)
  }

  log.warning("\nUnable to find a nearest shipment...\n")
  res.status(400).send()
})

// The '/shipments/delivered' GET request route
router.get('/shipments/delivered', function(req, res) {

  log.info("\nENDPOINT: /shipments/delivered")

  // Calculate the array of delivered shipments
  let deliveredShipments = model.getDeliveredShipments(hash(req.user.sub))

  res.status(200).json(deliveredShipments)
})

// The '/shipments/active' GET request route
router.get('/shipments/active', function(req, res) {

    log.info("\nENDPOINT: /shipments/active\n")

    // Calculate the array of active shipments
    let activeShipment = model.getActiveShipment(hash(req.user.sub))
    if (!activeShipment) {
      log.warning("\nNo active shipment found\n")
      res.status(200).json({})
      return
    }
    log.debug("\nActive Shipment:")
    log.raw(activeShipment.description)
    res.status(200).json(activeShipment)
  })

// The '/shipments/:shipmentID' GET request route
router.get('/shipments/:shipmentID', function(req, res) {

  log.info("\nENDPOINT: /shipments/:shipmentID\n")

  // Retrieve the shipment ID from the request header
  let shipmentID = parseInt(req.params.shipmentID)
  if (!Number.isInteger(shipmentID)) {
    log.error("\nShipment ID not specified or in the wrong format\n")
    res.status(400).send()
    return
  }

  // Find the shipment with the given ID
  let shipment = model.getShipment(shipmentID, hash(req.user.sub))
  if (!shipment) {
    log.error("\nNo shipment found for ID: " + shipmentID)
    res.status(404).send()
    return
  }

  log.debug("\nShipment ID: " + shipmentID)
  log.debug("Shipment Description: " + shipment.description)

  res.status(200).json(shipment)

})

// The '/shipments/update_state/:shipmentID' POST request route
router.post('/shipments/update_state/:shipmentID', function(req, res) {

  log.info("\nENDPOINT: /shipments/update_state/:shipmentID\n")

  // Retrieve the shipment ID from the request header
  let shipmentID = parseInt(req.params.shipmentID)
  if (!Number.isInteger(shipmentID)) {
    log.error("\nShipment ID not specified or in the wrong format\n")
    res.status(400).send()
    return
  }

  // Retrieve the location data from the request headers
  let latitude = parseFloat(req.get('DRIVER-LATITUDE'))
  let longitude = parseFloat(req.get('DRIVER-LONGITUDE'))
  if (!latitude || !longitude) {
    log.error("\nLocation data not specified or in the wrong format\n")
    res.status(400).send()
    return
  }

  log.debug("\nLatitude: " + latitude + "\nLongitude: " + longitude + "\n")

  // Retrieve the new shipment state from the request header
  let newState = parseInt(req.get('SHIPMENT-STATE'))
  if (!Number.isInteger(newState)) {
    log.error("\nShipment state not specified or in the wrong format\n")
    res.status(400).send()
    return
  }

  isUpdated = model.updateShipmentState(shipmentID, newState, hash(req.user.sub))

  if (isUpdated) {
      log.warning("\nState of shipment " + shipmentID + " updated to " + newState + "\n")
      res.status(200).send()
      return
  }

  res.status(400).send()
})

module.exports = router
