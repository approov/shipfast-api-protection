/*****************************************************************************
 * Project:     ShipFast API Protection (Server)
 * File:        demo-configuration.js
 * Original:    Created on 24 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 * 
 * A file for configuring the ShipFast demo.
 *****************************************************************************/

// The enumeration of various stages of the demo.
const DEMO_STAGE = {
    // The demo which uses basic protection by way of API key specified in the app manifest
    API_KEY_PROTECTION: 0,
    // The demo which introduces API request signing by HMAC using a static secret in code
    HMAC_STATIC_SECRET_PROTECTION: 1,
    // The demo which introduces API request signing by HMAC using a dynamic secret in code
    HMAC_DYNAMIC_SECRET_PROTECTION: 2,
    // The demo which uses CriticalBlue Approov protection by authenticating the app
    APPROOV_APP_AUTH_PROTECTION: 3
}

var config = {}

// The ShipFast server host name
config.serverHostName = 'shipfast.approov.io'

// The flag for whether to run the ShipFast server over HTTPS (true) or HTTP (false)
config.runSecureServer = true

// The Auth0 domain to use for use authentication
config.auth0Domain = 'approov.auth0.com'

// The current demo stage
config.currentDemoStage = DEMO_STAGE.API_KEY_PROTECTION

module.exports = {
    DEMO_STAGE,
    config
}