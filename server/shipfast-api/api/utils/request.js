const crypto = require('crypto')

const log_identifier = function(req, header_name, claim, script_name) {
  const user_hash = hash_header_payload_claim(req, header_name, claim)
  return log_simple_identifier(user_hash, script_name)
}

const log_simple_identifier = function(user_hash, script_name) {
  return script_name + " | " + user_hash.substring(0, 8)
}

const hash_header_payload_claim = function(req, header_name, claim) {
  const user_claim = _extract_unverified_payload_claim(req, header_name, claim)
  return _hash(user_claim)
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
  hash_header_payload_claim,
  log_identifier,
  log_simple_identifier
}
