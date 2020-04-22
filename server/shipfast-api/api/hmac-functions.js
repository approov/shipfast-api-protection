const log = require('./utils/logging')

const isValidHmac = function(hmac, config, req) {

  let requestProtocol

  // Retrieve the ShipFast HMAC used to sign the API request from the request header
  let requestShipFastHMAC = req.get('HMAC')
  if (!requestShipFastHMAC) {
    log.error('\tShipFast HMAC not specified or in the wrong format')
    return false
  }

  if (config.SHIPFAST_SERVER_BEHIND_PROXY) {
    requestProtocol = req.get(config.SHIPFAST_REQUEST_PROXY_PROTOCOL_HEADER)
  } else {
    requestProtocol = req.protocol
  }

  log.info("protocol: " + requestProtocol)
  log.info("host: " + req.hostname)
  log.info("originalUrl: " + req.originalUrl)
  log.info("Authorization: " + req.get('Authorization'))

  // Compute the request HMAC using the HMAC SHA-256 algorithm
  hmac.update(requestProtocol)
  hmac.update(req.hostname)
  hmac.update(req.originalUrl)
  hmac.update(req.get('Authorization'))
  let ourShipFastHMAC = hmac.digest('hex')

  // Check to see if our HMAC matches the one sent in the request header
  // and send an error response if it doesn't
  if (ourShipFastHMAC != requestShipFastHMAC) {
    log.error("\tShipFast HMAC invalid: received " + requestShipFastHMAC
      + " but should be " + ourShipFastHMAC)
    return false
  }

  log.success("\nValid HMAC.")

  return true
}

module.exports = {
  isValidHmac
}
