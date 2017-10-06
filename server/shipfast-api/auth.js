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

  // var hmac = crypto.createHmac('sha256', 'a secret');
  // hmac.update("this is my data");
  // var hmachex = hmac.digest('hex');
  // console.log("hmac: ", hmachex);

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