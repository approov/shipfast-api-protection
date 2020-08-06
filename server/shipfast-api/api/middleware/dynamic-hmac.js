const express = require('express')
const router = express.Router()
const log = require('./../utils/logging')
const crypto = require('crypto');
const hmacHelpers = require('./../hmac-functions.js')
const config = require('./../config/server').config
const request = require('./../utils/request')

router.use(function(req, res, next) {

  const log_id = request.log_identifier(req, 'authorization', 'sub', 'dynamic-hmac.js')

  log.info('---> VALIDATING DYNAMIC HMAC <---', log_id)

  let base64_decoded_hmac_secret = Buffer.from(config.SHIPFAST_API_HMAC_SECRET, 'base64')

  // Obfuscate the static secret to produce a dynamic secret to use during HMAC
  // verification for this demo stage
  let obfuscatedSecretData = base64_decoded_hmac_secret
  let shipFastAPIKeyData = new Buffer(config.SHIPFAST_API_KEY)

  for (let i = 0; i < Math.min(obfuscatedSecretData.length, shipFastAPIKeyData.length); i++) {
    obfuscatedSecretData[i] ^= shipFastAPIKeyData[i]
  }

  let obfuscatedSecret = new Buffer(obfuscatedSecretData).toString('base64')
  hmac = crypto.createHmac('sha256', Buffer.from(obfuscatedSecret, 'base64'))

  if (hmacHelpers.isValidHmac(hmac, config, req)) {
    next()
    return
  }

  res.status(400).send()

  return
})

module.exports = router
