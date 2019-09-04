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
const config = require('./demo-configuration').config

const chalk = require('chalk')

// Auto detection of colour support does not work always, thus we need to
// enforce it to support 256 colors.
const ctx = new chalk.constructor({level: 2})
const error = ctx.bold.red
const warning = ctx.bold.yellow
const info = ctx.bold.blue
const debug = ctx.bold.cyan

// needs to be hex colours, otherwise the contrast in the android terminal is very bad.
const success = ctx.bgHex('#008000').bold.hex('#ffffff')
const fatalError = ctx.bgHex('#ff0000').bold.hex('#ffffff')

const HOST_NAME = config.serverHostName
const HTTP_PORT = config.httpPort
const HTTPS_PORT = config.httpsPort
const URL = config.httpProtocol + '://' + HOST_NAME
const BASE_DIR = config.baseDir

// Support CORS
app.use(cors())

app.get('/', function(req, res) {
  res.status(200).json({ status: "\nShipFast is ready to accept connections!!!\n"})
})

// Add the authentication functionality to our server
app.use(auth)

// The '/shipments/nearest_shipment' GET request route
app.get('/shipments/nearest_shipment', function(req, res) {

  console.log(info("\n\nENDPOINT: ") + "/shipments/nearest_shipment\n")

  // Retrieve the location data from the request headers
  var latitude = parseFloat(req.get('DRIVER-LATITUDE'))
  var longitude = parseFloat(req.get('DRIVER-LONGITUDE'))

  if (!latitude || !longitude) {
    console.log(error("\nLocation data not specified or in the wrong format\n"))
    res.status(400).send()
    return
  }

  console.log(debug("Latitude: ") + latitude + debug("\nLongitude: ") + longitude)

  // Calculate the nearest shipment to the given location
  var nearestShipment = model.calculateNearestShipment(latitude, longitude)

  console.log(debug("\nNearest Shipment:"))
  console.log(nearestShipment)

  res.status(200).json(nearestShipment)
})

// The '/shipments/delivered' GET request route
app.get('/shipments/delivered', function(req, res) {

  console.log(info("\n\nENDPOINT: ") + "/shipments/delivered\n")

  // Calculate the array of delivered shipments
  var deliveredShipments = model.getDeliveredShipments()
  console.log(debug("\nDelivered Shipments:"))
  console.debug(deliveredShipments)
  res.status(200).json(deliveredShipments)
})

// The '/shipments/active' GET request route
app.get('/shipments/active', function(req, res) {

    console.log(info("\n\nENDPOINT: ") + "/shipments/active\n")

    // Calculate the array of active shipments
    var activeShipment = model.getActiveShipment()
    if (!activeShipment) {
      console.log(error("\nNo active shipment found\n"))
      res.status(404).send()
      return
    }
    console.log(debug("\nActive Shipment:"))
    console.debug(activeShipment)
    res.status(200).json(activeShipment)
  })

// The '/shipments/:shipmentID' GET request route
app.get('/shipments/:shipmentID', function(req, res) {

  console.log(info("\n\nENDPOINT: ") + "/shipments/:shipmentID\n")

  // Retrieve the shipment ID from the request header
  var shipmentID = parseInt(req.params.shipmentID)
  if (!shipmentID) {
    console.log(error("\nShipment ID not specified or in the wrong format\n"))
    res.status(400).send()
    return
  }

  // Find the shipment with the given ID
  var shipment = model.getShipment(shipmentID)
  if (!shipment) {
    console.log(error("No shipment found for ID " + shipmentID))
    res.status(404).send()
    return
  }
  console.log(debug("\nShipment ID " + shipmentID + ":"))
  console.log(shipment)
  res.status(200).json(shipment)
})

// The '/shipments/update_state/:shipmentID' POST request route
app.post('/shipments/update_state/:shipmentID', function(req, res) {

  console.log(info("\n\nENDPOINT: ") + "/shipments/update_state/:shipmentID\n")

  // Retrieve the shipment ID from the request header
  var shipmentID = parseInt(req.params.shipmentID)
  if (!shipmentID) {
    console.log(error("\nShipment ID not specified or in the wrong format\n"))
    res.status(400).send()
    return
  }

  // Find the shipment with the given ID
  var shipment = model.getShipment(shipmentID)
  if (!shipment) {
    console.log(error("\nNo shipment found for ID " + shipmentID + "\n"))
    res.status(404).send()
    return
  }

  // Retrieve the location data from the request headers
  var latitude = parseFloat(req.get('DRIVER-LATITUDE'))
  var longitude = parseFloat(req.get('DRIVER-LONGITUDE'))
  if (!latitude || !longitude) {
    console.log(error("\nLocation data not specified or in the wrong format\n"))
    res.status(400).send()
    return
  }

  console.log(debug("Latitude: ") + latitude + debug("\nLongitude: ") + longitude)

  // Retrieve the new shipment state from the request header
  var newState = parseInt(req.get('SHIPMENT-STATE'))
  if (!newState) {
    console.log(error("\nShipment state not specified or in the wrong format\n"))
    res.status(400).send()
    return
  }

  // Perform basic validation of the new shipment state
  if (newState <= 0
      || newState != shipment.getState() + 1
      || newState >= Object.keys(model.SHIPMENT_STATE).length) {
    console.log(error("\nShipment state invalid\n"))
    res.status(400).send()
    return
  }

  shipment.setState(newState)
  console.log(warning("\nState of shipment " + shipmentID + " updated to " + newState + "\n"))
  res.status(200).send()
})

if (config.runSecureServer) {
  // Load the certificate and key data for our server to be hosted over HTTPS
  var serverOptions = {
    key: fs.readFileSync(BASE_DIR + "/.ssl/" + HOST_NAME + ".key"),
    cert: fs.readFileSync(BASE_DIR + "/.ssl/" + HOST_NAME + ".pem"),
    requestCert: false,
    rejectUnauthorized: false
  }

  // Create and run the HTTPS server
  https.createServer(serverOptions, app).listen(HTTPS_PORT, function() {
    console.log(info("\nSecure ShipFast server listening on " + URL + ":" + HTTPS_PORT + "\n"))
  })
}
else {
  // Create and run the HTTP server
  app.listen(HTTP_PORT, function () {
    console.log(info("\nShipFast server listening on " + HOST_NAME + ":" + HTTP_PORT + "\n"))
  })
}
