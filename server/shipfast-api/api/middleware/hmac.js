const DEMO = require('./../config/demo-stages')
const config = require('./../config/server').config
const express = require('express')
const router = express.Router()
const log = require('./../utils/logging')
const crypto = require('crypto');

// Configure the request HMAC verification based on the demo stage
if (DEMO.CURRENT_STAGE == DEMO.STAGES.HMAC_STATIC_SECRET_PROTECTION || DEMO.CURRENT_STAGE == DEMO.STAGES.HMAC_DYNAMIC_SECRET_PROTECTION) {

  router.use(function(req, res, next) {

    // Retrieve the ShipFast HMAC used to sign the API request from the request header
    let requestShipFastHMAC = req.get('HMAC')
    if (!requestShipFastHMAC) {
      log.error('\tShipFast HMAC not specified or in the wrong format')
      res.status(400).send()
      return
    }

    let hmac
    let base64_decoded_hmac_secret = Buffer.from(config.SHIPFAST_API_HMAC_SECRET, 'base64')

    if (DEMO.CURRENT_STAGE == DEMO.STAGES.HMAC_STATIC_SECRET_PROTECTION) {
      // Just use the static secret during HMAC verification for this demo stage
      hmac = crypto.createHmac('sha256', base64_decoded_hmac_secret)
      log.info('---> VALIDATING STATIC HMAC <---')

    } else if (DEMO.CURRENT_STAGE == DEMO.STAGES.HMAC_DYNAMIC_SECRET_PROTECTION) {
      log.info('---> VALIDATING DYNAMIC HMAC <---')
      // Obfuscate the static secret to produce a dynamic secret to use during HMAC
      // verification for this demo stage
      let obfuscatedSecretData = base64_decoded_hmac_secret
      let shipFastAPIKeyData = new Buffer(config.SHIPFAST_API_KEY)

      for (let i = 0; i < Math.min(obfuscatedSecretData.length, shipFastAPIKeyData.length); i++) {
        obfuscatedSecretData[i] ^= shipFastAPIKeyData[i]
      }

      let obfuscatedSecret = new Buffer(obfuscatedSecretData).toString('base64')
      hmac = crypto.createHmac('sha256', Buffer.from(obfuscatedSecret, 'base64'))
    }

    let requestProtocol

    if (config.SHIPFAST_SERVER_BEHIND_PROXY) {
      requestProtocol = req.get(config.SHIPFAST_REQUEST_PROXY_PROTOCOL_HEADER)
    } else {
      requestProtocol = req.protocol
    }

    log.info("protocol: " + requestProtocol)
    log.info("host: " + req.hostname)
    log.info("originalUrl: " + req.originalUrl)
    log.info("Authorization: " + req.get('Authorization'))

    // Compute the request HMAC using the HMAC SHA-256 algorithm
    hmac.update(requestProtocol)
    hmac.update(req.hostname)
    hmac.update(req.originalUrl)
    hmac.update(req.get('Authorization'))
    let ourShipFastHMAC = hmac.digest('hex')

    // Check to see if our HMAC matches the one sent in the request header
    // and send an error response if it doesn't
    if (ourShipFastHMAC != requestShipFastHMAC) {
      log.error("\tShipFast HMAC invalid: received " + requestShipFastHMAC
        + " but should be " + ourShipFastHMAC)
      res.status(403).send()
      return
    }

    log.success("\nValid HMAC.")

    next()
  })
}

module.exports = router
