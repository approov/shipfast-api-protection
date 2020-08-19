const crypto = require('crypto')

const log_identifier = function(req, header_name, claim, script_name) {
  const user_hash = hash_header_payload_claim(req, header_name, claim)
  return log_simple_identifier(user_hash, script_name)
}

const log_simple_identifier = function(user_hash, script_name) {
  // Cut at 15 chars in order we can see the INVALID_REQUEST string in the logs
  // when an exception occurs at hash_header_payload_claim().
  const request_id = user_hash.substring(0, 15)
  return {
    request_id: request_id,
    script: script_name,
    string: script_name + " | " + request_id
  }
}

const hash_header_payload_claim = function(req, header_name, claim) {
  try {
    const user_claim = _extract_unverified_payload_claim(req, header_name, claim)
    return _hash(user_claim)
  } catch(error) {
    // When the request doesn't contain the authorization header or the token is
    // a malformed one we end up here.
    console.error(error)
    return "INVALID_REQUEST"
  }
}

const hash_user_claim_by_api_version = function(req) {
  return req.params.version + ":" + hash_header_payload_claim(req, 'authorization', 'sub')
}

const _extract_unverified_payload_claim = function(req, header_name, claim) {
  const auth_token = req.headers[header_name].substring(7)
  const auth_payload = auth_token.split('.')
  const decoded = Buffer.from(auth_payload[1], 'base64').toString('utf-8')
  const auth_decoded = JSON.parse(decoded)
  return auth_decoded[claim]
}

const _hash = function(text) {
  return crypto.createHash('sha256').update(text, 'utf-8').digest('hex')
}

module.exports = {
  hash_user_claim_by_api_version,
  hash_header_payload_claim,
  log_identifier,
  log_simple_identifier
}
