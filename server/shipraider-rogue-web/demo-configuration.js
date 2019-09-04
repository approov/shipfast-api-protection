/*****************************************************************************
 * Project:     ShipFast API Protection (Server)
 * File:        demo-configuration.js
 * Original:    Created on 24 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * A file for configuring the ShipFast demo.
 *****************************************************************************/


var config = {}

// The ShipFast server host name
config.serverHostName = process.env.SHIP_RAIDER_DOMAIN || 'localhost'

config.httpProtocol = process.env.SHIP_RAIDER_HTTP_PROTOCOL || 'https'
config.httpPort = process.env.SHIP_RAIDER_HTTP_PORT || '4333'
config.httpsPort = process.env.SHIP_RAIDER_HTTPS_PORT || '4443'

config.latitude = process.env.ANDROID_EMULATOR_LATITUDE || '51.5355'
config.longitude = process.env.ANDROID_EMULATOR_LONGITUDE || '-0.104971'

// The flag for whether to run the ShipFast server over HTTPS (true) or HTTP (false)
config.runSecureServer = (config.httpProtocol === "https")
console.log("RUN SECURE SERVER: " + config.runSecureServer)

config.baseDir = process.env.BASE_DIR || '/home/mobile'

module.exports = {
    config
}
