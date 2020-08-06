const express = require('express')
const router = express.Router()
const jwt = require('express-jwt')
const jwksRsa = require('jwks-rsa')
const log = require('./../utils/logging')
const config = require('./../config/auth0')
const request = require('./../utils/request')

// Create middleware for checking the user's ID token JWT
const checkUserAuthJWT = jwt({
  // Dynamically provide a signing key based on the kid in the header and the singing keys provided by the JWKS endpoint
  secret: jwksRsa.expressJwtSecret({
    cache: true,
    rateLimit: true,
    jwksRequestsPerMinute: 50,
    jwksUri: "https://" + config.AUTH0_DOMAIN + "/.well-known/jwks.json"
  }),

  // Validate the audience and the issuer.
  audience: config.AUTH0_CLIENT_ID,
  issuer: "https://" + config.AUTH0_DOMAIN + "/",
  algorithms: ['RS256']
})

router.use(checkUserAuthJWT)

router.use(function(req, res, next) {
  const log_id = request.log_identifier(req, 'authorization', 'sub', 'auth0.js')
  log.success("AUTH0: Valid Authorization token", log_id)
  next()
})

router.use(function(err, req, res, next) {
  const log_id = request.log_identifier(req, 'authorization', 'sub', 'auth0.js')
  log.error("AUTH0 ERROR: " + err.message, log_id)
  res.status(401)
  res.json({})
})

module.exports = router
