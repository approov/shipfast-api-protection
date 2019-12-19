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

module.exports = {
    config
}
