const log = require('./utils/logging')
const approovMiddleware = require('./middleware/approov')
const staticHmacMiddleware = require('./middleware/static-hmac')
const dynamicHmacMiddleware = require('./middleware/dynamic-hmac')
const apiKeyMiddleware = require('./middleware/api-key')
const auth0Middleware = require('./middleware/auth0')
const api = require('./server')
const endpoints = require('./endpoints')
const request = require('./utils/request')

api.use(function(req, res, next) {
  const log_id = request.log_identifier(req, 'authorization', 'sub', ' api.js')

  log.raw("\n\n-------------------- START REQUEST: " + req.url + " --------------------\n", log_id)
  log.raw("Headers:", log_id)
  log.debug(req.headers, log_id)
  next()
})


/**
 * MIDDLEWARE REQUEST AUTHENTICATION
 */

// API Key authentication its only used to authenticate WHAT is doing the
//  request in v1, and its replaced with HMAC in v2.
api.use('/v1', apiKeyMiddleware)

// In v2 HMAC validation with a static secret replaces the API key to
//  authenticate WHAT is doing the request.
api.use('/v2', staticHmacMiddleware)

// In v3 HMAC validation with a dynamic secret(calculated at run-time) is used
//  to authenticate WHAT is doing the request.
api.use('/v3', dynamicHmacMiddleware)

// In v4 the Approov Token replaces the dynamic HMAC to authenticate WHAT is
//  doing the request.
api.use('/v4', approovMiddleware)

// User authentication is always performed, except for the root `/`, but we do
//  it as the last step, because we don't want to waste time with authenticating
//  the user when we don't trust in WHAT is doing the request.
api.use("/v*", auth0Middleware)


/**
 * UNPROTECTED ENDPOINTS
 */

api.get('/', function(req, res) {
  res.status(200).json({ status: "ShipFast is ready to accept connections!!!"})
})


/**
 * PROTECTED ENDPOINTS
 */

api.use(endpoints)
