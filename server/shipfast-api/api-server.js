/*****************************************************************************
 * Project:     ShipFast API Protection (Server)
 * File:        api-server.js
 * Original:    Created on 29 Sept 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * This file contains the HTTPS express server.
 *****************************************************************************/

const express = require('express')
const app = express()
const https = require('https')
const fs = require('fs')
const cors = require('cors')
const model = require('./model')
const auth = require('./auth')

// Support CORS
app.use(cors())

// Add the authentication functionality to our server
app.use(auth)

// Load the certificate and key data for our server to be hosted over HTTPS
var options = {
  key: fs.readFileSync( './10.0.2.2.key' ),
  cert: fs.readFileSync( './10.0.2.2.pem' ),
  requestCert: false,
  rejectUnauthorized: false
}

// The '/shipments/nearest_shipment' GET request route
app.get('/shipments/nearest_shipment', function(req, res) {

  console.log("/nearest_shipment from " + req.user)

  // Retrieve the location data from the request headers
  var latitude = parseFloat(req.get('SF-Latitude'))
  var longitude = parseFloat(req.get('SF-Longitude'))
  if (!latitude || !longitude) {
    console.log('\tLocation data not specified or in the wrong format')
    res.status(400).send()
    return
  }
  console.log("\tLat=" + latitude + ", Lon=" + longitude)

  // Calculate the nearest shipment to the given location
  var nearestShipment = model.calculateNearestShipment(latitude, longitude)
  if (!nearestShipment) {
    console.log('\tNo available shipment found')
    res.status(404).send()
    return
  }
  console.log('\tNearest Shipment:')
  console.log(nearestShipment)
  res.status(200).json(nearestShipment)
})

// The '/shipments/delivered' GET request route
app.get('/shipments/delivered', function(req, res) {

  console.log("/shipments/delivered from " + req.user)

  // Calculate the array of delivered shipments
  var deliveredShipments = model.getDeliveredShipments()
  console.log('\tDelivered Shipments:')
  console.log(deliveredShipments)
  res.status(200).json(deliveredShipments)
})

// The '/shipments/active' GET request route
app.get('/shipments/active', function(req, res) {
  
    console.log("/shipments/active from " + req.user)
  
    // Calculate the array of active shipments
    var activeShipment = model.getActiveShipment()
    if (!activeShipment) {
      console.log('\tNo active shipment found')
      res.status(404).send()
      return
    }
    console.log('\tActive Shipment:')
    console.log(activeShipment)
    res.status(200).json(activeShipment)
  })

// The '/shipments/:shipmentID' GET request route
app.get('/shipments/:shipmentID', function(req, res) {

  console.log("/shipments/:shipmentID from " + req.user)

  // Retrieve the shipment ID from the request header
  var shipmentID = parseInt(req.params.shipmentID)
  if (!shipmentID) {
    console.log('\tShipment ID not specified or in the wrong format')
    res.status(400).send()
    return
  }

  // Find the shipment with the given ID
  var shipment = model.getShipment(shipmentID)
  if (!shipment) {
    console.log("\tNo shipment found for ID " + shipmentID)
    res.status(404).send()
    return
  }
  console.log("\tShipment ID " + shipmentID + ":")
  console.log(shipment)
  res.status(200).json(shipment)
})

// The '/shipments/update_state/:shipmentID' POST request route
app.post('/shipments/update_state/:shipmentID', function(req, res) {

  console.log("/shipments/update_state/:shipmentID from " + req.user)

  // Retrieve the shipment ID from the request header
  var shipmentID = parseInt(req.params.shipmentID)
  if (!shipmentID) {
    console.log('\tShipment ID not specified or in the wrong format')
    res.status(400).send()
    return
  }

  // Find the shipment with the given ID
  var shipment = model.getShipment(shipmentID)
  if (!shipment) {
    console.log("\tNo shipment found for ID " + shipmentID)
    res.status(404).send()
    return
  }

  // Retrieve the location data from the request headers
  var latitude = parseFloat(req.get('SF-Latitude'))
  var longitude = parseFloat(req.get('SF-Longitude'))
  if (!latitude || !longitude) {
    console.log('\tLocation data not specified or in the wrong format')
    res.status(400).send()
    return
  }
  console.log("\tLat=" + latitude + ", Lon=" + longitude)

  // Retrieve the new shipment state from the request header
  var newState = parseInt(req.get('SF-State'))
  if (!newState) {
    console.log('\tShipment state not specified or in the wrong format')
    res.status(400).send()
    return
  }

  // Perform basic validation of the new shipment state
  if (newState <= 0
      || newState != shipment.getState() + 1
      || newState >= Object.keys(model.SHIPMENT_STATE).length) {
    console.log('\tShipment state invalid')
    res.status(400).send()
    return
  }

  shipment.setState(newState)
  console.log("\tState of shipment " + shipmentID + " updated to " + newState)
  res.status(200).send()
})

// Create and run the HTTPS server
https.createServer(options, app).listen(443, function() {
  console.log('ShipFast server listening on port 443!')
})

// Create and run the HTTP server
// app.listen(3000, function () {
//   console.log('Insecure ShipFast server listening on port 3000!')
// })
