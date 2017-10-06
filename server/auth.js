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