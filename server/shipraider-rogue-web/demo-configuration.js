/*****************************************************************************
 * Project:     ShipFast API Protection (Server)
 * File:        demo-configuration.js
 * Original:    Created on 24 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * A file for configuring the ShipFast demo.
 *****************************************************************************/

const dotenv = require('dotenv').config()

if (dotenv.error) {
  throw dotenv.error
}

const config = {
    NODE_SSL_DIR: dotenv.parsed.NODE_SSL_DIR || process.env.HOME + "/.ssl",
    SHIPRAIDER_SERVER_HOSTNAME: dotenv.parsed.SHIPRAIDER_SERVER_HOSTNAME || 'localhost',
    SHIPRAIDER_HTTP_PROTOCOL: dotenv.parsed.SHIPRAIDER_HTTP_PROTOCOL || 'https',
    SHIPRAIDER_HTTP_PORT: dotenv.parsed.SHIPRAIDER_HTTP_PORT || '4333',
    SHIPRAIDER_HTTPS_PORT: dotenv.parsed.SHIPRAIDER_HTTPS_PORT || '4443',
    SHIPFAST_PUBLIC_DOMAIN_HTTP_PROTOCOL: dotenv.parsed.SHIPFAST_PUBLIC_DOMAIN_HTTP_PROTOCOL || undefined,
    SHIPFAST_PUBLIC_DOMAIN: dotenv.parsed.SHIPFAST_PUBLIC_DOMAIN || undefined,
    SHIPFAST_API_VERSION: dotenv.parsed.SHIPFAST_API_VERSION || undefined,
    SHIPFAST_API_KEY: dotenv.parsed.SHIPFAST_API_KEY || undefined,
    SHIPFAST_API_HMAC_SECRET: dotenv.parsed.SHIPFAST_API_HMAC_SECRET || undefined,
    AUTH0_DOMAIN: dotenv.parsed.AUTH0_DOMAIN || undefined,
    AUTH0_CLIENT_ID: dotenv.parsed.AUTH0_CLIENT_ID || undefined,
    DRIVER_LATITUDE: dotenv.parsed.DRIVER_LATITUDE || undefined,
    DRIVER_LONGITUDE: dotenv.parsed.DRIVER_LONGITUDE || undefined
}

let missing_env_vars = ""

Object.entries(config).forEach(([key, value]) => {
    if (value === undefined) {
        missing_env_vars += key + ", "
    }
})

if (missing_env_vars !== "") {
    throw new Error("Missing Env Vars: " + missing_env_vars.slice(0, -2)) // removes last comma in the string
}

// The enumeration of various stages of the demo.
const STAGES = {
    // The demo which uses basic protection by way of API key specified in the app manifest
    API_KEY_PROTECTION: 0,
    // The demo which introduces API request signing by HMAC using a static secret in code
    HMAC_STATIC_SECRET_PROTECTION: 1,
    // The demo which introduces API request signing by HMAC using a dynamic secret in code
    HMAC_DYNAMIC_SECRET_PROTECTION: 2,
    // The demo which uses CriticalBlue Approov protection by authenticating the app
    APPROOV_APP_AUTH_PROTECTION: 3
}

// The current demo stage
let demo_stage = dotenv.parsed.SHIPFAST_DEMO_STAGE || undefined

if (demo_stage === undefined) {
    throw new Error("Missing Env Var value for: SHIPFAST_DEMO_STAGE")
}

if (STAGES[demo_stage] === undefined) {
    throw new Error("Invalid value for env var: SHIPFAST_DEMO_STAGE")
}

config["SHIPFAST_CURRENT_DEMO_STAGE"] = STAGES[demo_stage]
config["SHIPFAST_DEMO_STAGES"] = STAGES

module.exports = {
    config
}
