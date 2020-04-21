const approov = require('./../approov/approov-token-check')
const express = require('express')
const router = express.Router()

// Use the Approov-Token header to authenticate the connecting mobile app

router.use(approov.checkApproovToken)

// Handles failure in validating the Approov token
router.use(approov.handlesApproovTokenError)

// Handles requests where the Approov token is a valid one.
router.use(approov.handlesApproovTokenSuccess)

// Checks if the Approov token binding is valid and aborts the request when the environment variable
// APPROOV_ABORT_REQUEST_ON_INVALID_TOKEN_BINDING is set to true in the environment file.
router.use(approov.handlesApproovTokenBindingVerification)


module.exports = router
