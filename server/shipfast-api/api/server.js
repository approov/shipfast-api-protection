const express = require('express')
const cors = require('cors')
const log = require('./utils/logging')
const request = require('./utils/request')
const fs = require('fs')
const https = require('https')
const config = require('./config/server').config

const api = express()
const LOG_IDENTIFIER = request.log_simple_identifier('no_user', 'server.js')

// Support CORS
api.use(cors())
api.options('*', cors())

if (config.SHIPFAST_HTTP_PROTOCOL === "https") {

  log.info("Host Name: " + config.SHIPFAST_SERVER_HOSTNAME + "\n", LOG_IDENTIFIER)

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
    log.info("Secure ShipFast Rogue Web server listening on " + URL + "\n", LOG_IDENTIFIER)
  })

} else {
    api.listen(config.SHIPFAST_HTTP_PORT, function () {
      const URL = config.SHIPFAST_HTTP_PROTOCOL + '://' + config.SHIPFAST_SERVER_HOSTNAME + ":" + config.SHIPFAST_HTTP_PORT
      log.info("ShipFast server ready on " + URL + "\n", LOG_IDENTIFIER)
    })
}

module.exports = api
