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

  console.log("ShipFast API key verifier invoked from " + req.user)
  
  // Retrieve the ShipFast API key from the request header
  var shipFastAPIKey = req.get('SF-API_KEY')
  if (!shipFastAPIKey) {
    console.log('\tShipFast API key not specified or in the wrong format')
    res.status(400).send()
    return
  }

  // Verify the ShipFast API key
  if (!shipFastAPIKeys.includes(shipFastAPIKey)) {
    console.log('\tShipFast API key invalid')
    res.status(403).send()
    return
  }

  // Retrieve the ShipFast HMAC used to sign the API request from the request header
  var requestShipFastHMAC = req.get('SF-HMAC')
  if (!requestShipFastHMAC) {
    console.log('\tShipFast HMAC not specified or in the wrong format')
    res.status(400).send()
    return
  }

  // Calculate our version of the HMAC and compare with one sent in the request header
  var secret = SHIPFAST_HMAC_SECRET
  var hmac = crypto.createHmac('sha256', Buffer.from(secret, 'base64'))
  hmac.update(req.protocol)
  hmac.update(req.host)
  hmac.update(req.originalUrl)
  hmac.update(req.get('Authorization'))
  var ourShipFastHMAC = hmac.digest('hex')
  if (ourShipFastHMAC != requestShipFastHMAC) {
    console.log("\tShipFast HMAC invalid: received " + requestShipFastHMAC
      + " but should be " + ourShipFastHMAC)
    res.status(403).send()
    return
  }

  next()
})

// Create middleware for checking the JWT
const checkJwt = jwt({
  // Dynamically provide a signing key based on the kid in the header and the singing keys provided by the JWKS endpoint
  secret: jwksRsa.expressJwtSecret({
    cache: true,
    rateLimit: true,
    jwksRequestsPerMinute: 5,
    jwksUri: `https://approov.auth0.com/.well-known/jwks.json`
  }),

  // Validate the audience and the issuer.
  audience: process.env.AUTH0_AUDIENCE,
  issuer: `https://approov.auth0.com/`,
  algorithms: ['RS256']
})
router.use(checkJwt)

// Add the authentication router to the exports
module.exports = router