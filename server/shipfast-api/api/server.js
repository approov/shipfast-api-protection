const express = require('express')
const api = express()
const cors = require('cors')
const log = require('./utils/logging')
const fs = require('fs')
const https = require('https')
const config = require('./config/server').config

// Support CORS
api.use(cors())
api.options('*', cors())

if (config.SHIPFAST_HTTP_PROTOCOL === "https") {

  log.info("\nHost Name: " + config.SHIPFAST_SERVER_HOSTNAME + "\n")

  // Load the certificate and key data for our server to be hosted over HTTPS
  let serverOptions = {
    key: fs.readFileSync(config.NODE_SSL_DIR + config.SHIPFAST_SERVER_HOSTNAME + ".key"),
    cert: fs.readFileSync(config.NODE_SSL_DIR + config.SHIPFAST_SERVER_HOSTNAME + ".pem"),
    requestCert: false,
    rejectUnauthorized: false
  }

  // Create and run the HTTPS server
  https.createServer(serverOptions, api).listen(HTTPS_PORT, function() {
    const URL = config.SHIPFAST_HTTP_PROTOCOL + '://' + config.SHIPFAST_SERVER_HOSTNAME + ":" + config.SHIPFAST_HTTPS_PORT
    log.info("\nSecure ShipFast Rogue Web server listening on " + URL + "\n")
  })

} else {
    api.listen(config.SHIPFAST_HTTP_PORT, function () {
      const URL = config.SHIPFAST_HTTP_PROTOCOL + '://' + config.SHIPFAST_SERVER_HOSTNAME + ":" + config.SHIPFAST_HTTP_PORT
      log.info("\nShipFast server ready on " + URL + "\n")
    })
}

module.exports = api
