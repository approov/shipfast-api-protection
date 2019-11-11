const express = require('express')
const router = express.Router()
const jwt = require('express-jwt')
const log = require('./../utils/logging')
const config = require('./../config/server').config
const DEMO = require('./../config/demo-stages')


if (DEMO.CURRENT_STAGE == DEMO.STAGES.API_KEY_PROTECTION) {

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

    // Retrieve the ShipFast API key from the request header
    var shipFastAPIKey = req.get('API-KEY')

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
