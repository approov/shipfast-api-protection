const express = require('express')
const router = express.Router()
const log = require('./../utils/logging')
const config = require('./../config/server').config
const request = require('./../utils/request')

let cli_api_keys = [
  config.SHIPFAST_CLI_API_KEY
]

// Verify the ShipFast CLI API key
router.use(function(req, res, next) {

  const log_id = request.log_identifier(req, 'authorization', 'sub', 'api-key.js')

  let cli_api_key = req.get('CLI-API-KEY')

  if (!cli_api_key) {
    log.error('SHIPFAST ADMIN: CLI API key not specified or in the wrong format', log_id)
    res.status(400).send()
    return
  }

  if (!cli_api_keys.includes(cli_api_key)) {
    log.error('SHIPFAST ADMIN: CLI API key invalid', log_id)
    res.status(401).send()
    return
  }

  log.success('SHIPFAST ADMIN: Valid CLI API key.', log_id)

  next()
})

module.exports = router
