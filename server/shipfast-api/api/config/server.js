const dotenv = require('dotenv').config()
const log = require('./../utils/logging')

if (dotenv.error) {
  log.warning('FAILED TO PARSE `.env` FILE | ' + dotenv.error)
}

var config = {}

// The ShipFast server host name
config.serverHostName = process.env.SHIPFAST_API_DOMAIN || 'localhost'

config.httpProtocol = process.env.SHIPFAST_HTTP_PROTOCOL || 'https'
config.httpPort = process.env.SHIPFAST_HTTP_PORT || '3333'
config.httpsPort = process.env.SHIPFAST_HTTPS_PORT || '3443'

// The flag for whether to run the ShipFast server over HTTPS (true) or HTTP (false)
config.runSecureServer = (config.httpProtocol === "https")

config.baseDir = process.env.BASE_DIR || '/home/developer'

module.exports = {
    config,
}
