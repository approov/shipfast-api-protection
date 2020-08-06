const express = require('express')
const router = express.Router()
const log = require('./../utils/logging')
const crypto = require('crypto');
const hmacHelpers = require('./../hmac-functions.js')
const config = require('./../config/server').config
const request = require('./../utils/request')

router.use(function(req, res, next) {
  const log_id = request.log_identifier(req, 'authorization', 'sub', 'static-hmac.js')

  log.info('---> VALIDATING STATIC HMAC <---', log_id)

  let base64_decoded_hmac_secret = Buffer.from(config.SHIPFAST_API_HMAC_SECRET, 'base64')

  // Just use the static secret during HMAC verification for this demo stage
  let hmac = crypto.createHmac('sha256', base64_decoded_hmac_secret)

  if (hmacHelpers.isValidHmac(hmac, config, req)) {
    next()
    return
  }

  res.status(400).send()

  return
})

module.exports = router
