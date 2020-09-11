const log = require('./utils/logging')
const request = require('./utils/request')

const isValidHmac = function(hmac, config, req) {

  const log_id = request.log_identifier(req, 'authorization', 'sub', 'hmac-functions.js')

  let requestProtocol

  // Retrieve the ShipFast HMAC used to sign the API request from the request header
  let requestShipFastHMAC = req.get('HMAC')
  if (!requestShipFastHMAC) {
    log.error('ShipFast HMAC not specified or in the wrong format', log_id)
    return false
  }

  if (config.SHIPFAST_SERVER_BEHIND_PROXY) {
    requestProtocol = req.get(config.SHIPFAST_REQUEST_PROXY_PROTOCOL_HEADER)
  } else {
    requestProtocol = req.protocol
  }

  log.info("protocol: " + requestProtocol, log_id)
  log.info("host: " + req.hostname, log_id)
  log.info("originalUrl: " + req.originalUrl, log_id)
  log.info("Authorization: " + req.get('Authorization'), log_id)

  // Compute the request HMAC using the HMAC SHA-256 algorithm
  hmac.update(requestProtocol)
  hmac.update(req.hostname)
  hmac.update(req.originalUrl)
  hmac.update(req.get('Authorization'))
  let ourShipFastHMAC = hmac.digest('hex')

  // Check to see if our HMAC matches the one sent in the request header
  // and send an error response if it doesn't
  if (ourShipFastHMAC != requestShipFastHMAC) {
    log.error("ShipFast HMAC invalid: received " + requestShipFastHMAC
      + " but should be " + ourShipFastHMAC, log_id)
    return false
  }

  log.success("Valid HMAC.", log_id)

  return true
}

module.exports = {
  isValidHmac
}
