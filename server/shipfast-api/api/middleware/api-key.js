const express = require('express')
const router = express.Router()
const log = require('./../utils/logging')
const config = require('./../config/server').config
const request = require('./../utils/request')
const response = require('./../utils/response')

// The array of ShipFast API keys
let shipFastAPIKeys = [
  config.SHIPFAST_API_KEY
]

// Verify the ShipFast API key
router.use(function(req, res, next) {

  const log_id = request.log_identifier(req, 'authorization', 'sub', 'api-key.js')

  // Retrieve the ShipFast API key from the request header
  let shipFastAPIKey = req.get('API-KEY')

  if (!shipFastAPIKey) {
    log.error('ShipFast API key not specified or in the wrong format', log_id)
    res.status(400).json(response.bad_request(log_id))
    return
  }

  // Verify the ShipFast API key
  if (!shipFastAPIKeys.includes(shipFastAPIKey)) {
    log.error('ShipFast API key invalid', log_id)
    res.status(401).json(response.invalid_request(log_id))
    return
  }

  log.success('SHIPFAST: Valid API key.', log_id)

  next()
})


// Add the authentication router to the exports
module.exports = router
