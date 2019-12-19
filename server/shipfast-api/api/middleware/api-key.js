const express = require('express')
const router = express.Router()
const log = require('./../utils/logging')
const config = require('./../config/server').config
const DEMO = require('./../config/demo-stages')


if (DEMO.CURRENT_STAGE == DEMO.STAGES.API_KEY_PROTECTION) {

  // The array of ShipFast API keys
  let shipFastAPIKeys = [
    config.SHIPFAST_API_KEY
  ]

  // Verify the ShipFast API key
  router.use(function(req, res, next) {

    // Retrieve the ShipFast API key from the request header
    let shipFastAPIKey = req.get('API-KEY')

    if (!shipFastAPIKey) {
      log.error('\tShipFast API key not specified or in the wrong format')
      res.status(400).send()
      return
    }

    // Verify the ShipFast API key
    if (!shipFastAPIKeys.includes(shipFastAPIKey)) {
      log.error('\tShipFast API key invalid')
      res.status(403).send()
      return
    }

    next()
  })

}

// Add the authentication router to the exports
module.exports = router
