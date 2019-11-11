const DEMO = require('./../config/demo-stages')
const express = require('express')
const router = express.Router()
const log = require('./../utils/logging')

// Configure the request HMAC verification based on the demo stage
if (DEMO.CURRENT_STAGE == DEMO.STAGES.HMAC_STATIC_SECRET_PROTECTION || DEMO.CURRENT_STAGE == DEMO.STAGES.HMAC_DYNAMIC_SECRET_PROTECTION) {

  router.use(function(req, res, next) {

    // Retrieve the ShipFast HMAC used to sign the API request from the request header
    var requestShipFastHMAC = req.get('HMAC')
    if (!requestShipFastHMAC) {
      log.error('\tShipFast HMAC not specified or in the wrong format')
      res.status(400).send()
      return
    }

    // Calculate our version of the HMAC and compare with one sent in the request header
    var secret = SHIPFAST_HMAC_SECRET
    var hmac

    if (DEMO.CURRENT_STAGE == DEMO.STAGES.HMAC_STATIC_SECRET_PROTECTION) {
      // Just use the static secret during HMAC verification for this demo stage
      hmac = crypto.createHmac('sha256', Buffer.from(secret, 'base64'))

    } else if (DEMO.CURRENT_STAGE == DEMO.STAGES.HMAC_DYNAMIC_SECRET_PROTECTION) {
      // Obfuscate the static secret to produce a dynamic secret to use during HMAC
      // verification for this demo stage
      var obfuscatedSecretData = Buffer.from(secret, 'base64')
      var shipFastAPIKeyData = new Buffer("QXBwcm9vdidzIHRvdGFsbHkgYXdlc29tZSEh")
      for (var i = 0; i < Math.min(obfuscatedSecretData.length, shipFastAPIKeyData.length); i++) {
        obfuscatedSecretData[i] ^= shipFastAPIKeyData[i]
      }
      var obfuscatedSecret = new Buffer(obfuscatedSecretData).toString('base64')
      hmac = crypto.createHmac('sha256', Buffer.from(obfuscatedSecret, 'base64'))
    }

    // Compute the request HMAC using the HMAC SHA-256 algorithm
    hmac.update(req.protocol)
    hmac.update(req.host)
    hmac.update(req.originalUrl)
    hmac.update(req.get('Authorization'))
    var ourShipFastHMAC = hmac.digest('hex')

    // Check to see if our HMAC matches the one sent in the request header
    // and send an error response if it doesn't
    if (ourShipFastHMAC != requestShipFastHMAC) {
      log.error("\tShipFast HMAC invalid: received " + requestShipFastHMAC
        + " but should be " + ourShipFastHMAC)
      res.status(403).send()
      return
    }

    next()
  })
}

module.exports = router
