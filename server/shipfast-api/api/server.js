const express = require('express')
const api = express()
const cors = require('cors')
const log = require('./utils/logging')
const fs = require('fs')
const https = require('https')

// Support CORS
api.use(cors())

const config = require('./config/server').config
const HOST_NAME = config.serverHostName
const HTTP_PORT = config.httpPort
const HTTPS_PORT = config.httpsPort
const URL = config.httpProtocol + '://' + HOST_NAME
const BASE_DIR = config.baseDir


if (config.runSecureServer) {
  // Load the certificate and key data for our server to be hosted over HTTPS
  var serverOptions = {
    key: fs.readFileSync(BASE_DIR + "/.ssl/" + HOST_NAME + ".key"),
    cert: fs.readFileSync(BASE_DIR + "/.ssl/" + HOST_NAME + ".pem"),
    requestCert: false,
    rejectUnauthorized: false
  }

  // Create and run the HTTPS server
  https.createServer(serverOptions, api).listen(HTTPS_PORT, function() {
    log.success("\nSecure ShipFast server listening on " + URL + ":" + HTTPS_PORT + "\n")
  })
}
else {
  // Create and run the HTTP server
  api.listen(HTTP_PORT, function () {
    log.warning("\nShipFast server listening on " + HOST_NAME + ":" + HTTP_PORT + "\n")
  })
}

module.exports = api
