/*****************************************************************************
 * Project:     ShipFast API Protection (Server)
 * File:        auth.js
 * Original:    Created on 29 Sept 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * This file contains the API request authentication router.
 *****************************************************************************/

const express = require('express')
const router = express.Router()
const jwt = require('express-jwt')
const jwksRsa = require('jwks-rsa')
const crypto = require('crypto')
const DEMO_STAGE = require('./demo-configuration').DEMO_STAGE
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

// The array of ShipFast API keys
var shipFastAPIKeys = [
  'VGhpcyBpcyBOT1QgYSBnb29kIGtleSEhIDoo',
  'QXBwcm9vdidzIHRvdGFsbHkgYXdlc29tZSEh',
  'V2hhdCBnZWVrIGNvbnZlcnRlZCB0aGlzPyE/'
]

// The ShipFast HMAC secret used to sign API requests
const SHIPFAST_HMAC_SECRET = '4ymoofRe0l87QbGoR0YH+/tqBN933nKAGxzvh5z2aXr5XlsYzlwQ6pVArGweqb7cN56khD/FvY0b6rWc4PFOPw=='

// Verify the ShipFast API key
router.use(function(req, res, next) {

  console.log("\n\n-------------------- AUTHENTICATING A NEW REQUEST --------------------\n")
  console.log(debug("Headers:"))
  console.debug(req.headers)

  // Retrieve the ShipFast API key from the request header
  var shipFastAPIKey = req.get('API-KEY')

  if (!shipFastAPIKey) {
    console.log(error('\tShipFast API key not specified or in the wrong format'))
    res.status(400).send()
    return
  }

  // Verify the ShipFast API key
  if (!shipFastAPIKeys.includes(shipFastAPIKey)) {
    console.log(error('\tShipFast API key invalid'))
    res.status(403).send()
    return
  }

  // Configure the request HMAC verification based on the demo stage
  if (config.currentDemoStage == DEMO_STAGE.HMAC_STATIC_SECRET_PROTECTION
      || config.currentDemoStage == DEMO_STAGE.HMAC_DYNAMIC_SECRET_PROTECTION) {

    // Retrieve the ShipFast HMAC used to sign the API request from the request header
    var requestShipFastHMAC = req.get('HMAC')
    if (!requestShipFastHMAC) {
      console.log(error('\tShipFast HMAC not specified or in the wrong format'))
      res.status(400).send()
      return
    }

    // Calculate our version of the HMAC and compare with one sent in the request header
    var secret = SHIPFAST_HMAC_SECRET
    var hmac
    if (config.currentDemoStage == DEMO_STAGE.HMAC_STATIC_SECRET_PROTECTION) {
      // Just use the static secret during HMAC verification for this demo stage
      hmac = crypto.createHmac('sha256', Buffer.from(secret, 'base64'))
    }
    else if (config.currentDemoStage == DEMO_STAGE.HMAC_DYNAMIC_SECRET_PROTECTION) {
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
      console.log(error("\tShipFast HMAC invalid: received " + requestShipFastHMAC
        + " but should be " + ourShipFastHMAC))
      res.status(403).send()
      return
    }
  }

  next()
})

// Use the Approov-Token header to authenticate the connecting mobile app
// if we are in the Approov demo stage
if (config.currentDemoStage == DEMO_STAGE.APPROOV_APP_AUTH_PROTECTION) {
  // Verify and decode the Approov token and respond with 403 if the JWT
  // could not be decoded, has expired, or has an invalid signature
  const checkApproovTokenJWT = jwt({
    secret: new Buffer(config.approovTokenSecret, 'base64'),
    getToken: function fromApproovTokenHeader(req) {
      // Retrieve the Approov token used to authenticate the mobile app from the request header
      var approovToken = req.get('Approov-Token')
      if (!approovToken) {
        console.debug(error('\tApproov token not specified or in the wrong format'))
      }

      return approovToken
    },
    algorithms: ['HS256']
  })

  router.use(checkApproovTokenJWT)

  router.use(function (req, res, next) {
    console.log(success('\n APPROOV TOKEN VALIDATED '));
    next();
  });

  router.use(function (err, req, res, next) {
    if (err.name === 'UnauthorizedError') {
      console.debug(fatalError("\n APPROOV TOKEN INVALID: " + err + " "));
    }
    true? next(err) : next();
  });
}

// Create middleware for checking the user's ID token JWT
const checkUserAuthJWT = jwt({
  // Dynamically provide a signing key based on the kid in the header and the singing keys provided by the JWKS endpoint
  secret: jwksRsa.expressJwtSecret({
    cache: true,
    rateLimit: true,
    jwksRequestsPerMinute: 5,
    jwksUri: "https://" + config.auth0Domain + "/.well-known/jwks.json"
  }),

  // Validate the audience and the issuer.
  audience: process.env.AUTH0_AUDIENCE,
  issuer: "https://" + config.auth0Domain + "/",
  algorithms: ['RS256']
})
router.use(checkUserAuthJWT)

// Add the authentication router to the exports
module.exports = router
