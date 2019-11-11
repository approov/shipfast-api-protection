const dotenv = require('dotenv').config()
const log = require('./../utils/logging')

if (dotenv.error) {
  log.warning('FAILED TO PARSE `.env` FILE | ' + dotenv.error)
}

const auth0 = {
    domain: dotenv.parsed.AUTH0_DOMAIN,
    audience: dotenv.parsed.AUTH0_AUDIENCE,
}

module.exports = auth0
