const dotenv = require('dotenv').config()

if (dotenv.error) {
  throw dotenv.error
}

let server_behind_proxy
let proxy_protocol_header

if (dotenv.parsed.SHIPFAST_SERVER_BEHIND_PROXY.toLowerCase() === 'false' ) {
    server_behind_proxy = false
    proxy_protocol_header = ""
} else if (dotenv.parsed.SHIPFAST_SERVER_BEHIND_PROXY.toLowerCase() === 'true' ) {
    server_behind_proxy = true
    proxy_protocol_header = dotenv.parsed.SHIPFAST_REQUEST_PROXY_PROTOCOL_HEADER || undefined
} else {
    server_behind_proxy = undefined
    proxy_protocol_header = undefined
}

let distance_in_miles = true

if (dotenv.parsed.DISTANCE_IN_MILES.toLowerCase() === 'false' ) {
    distance_in_miles = false
}

const config = {
    NODE_SSL_DIR: dotenv.parsed.NODE_SSL_DIR || process.env.HOME + "/.ssl",
    SHIPFAST_SERVER_BEHIND_PROXY: server_behind_proxy,
    SHIPFAST_REQUEST_PROXY_PROTOCOL_HEADER: proxy_protocol_header,
    SHIPFAST_SERVER_HOSTNAME: dotenv.parsed.SHIPFAST_SERVER_HOSTNAME || 'localhost',
    SHIPFAST_HTTP_PROTOCOL: dotenv.parsed.SHIPFAST_HTTP_PROTOCOL || 'https',
    SHIPFAST_HTTP_PORT: dotenv.parsed.SHIPFAST_HTTP_PORT || '4333',
    SHIPFAST_HTTPS_PORT: dotenv.parsed.SHIPFAST_HTTPS_PORT || '4443',
    DISTANCE_IN_MILES: distance_in_miles,
    CURRENCY_SYMBOL: dotenv.parsed.CURRENCY_SYMBOL || "£",
    SHIPFAST_PUBLIC_DOMAIN_HTTP_PROTOCOL: dotenv.parsed.SHIPFAST_PUBLIC_DOMAIN_HTTP_PROTOCOL || undefined,
    SHIPFAST_PUBLIC_DOMAIN: dotenv.parsed.SHIPFAST_PUBLIC_DOMAIN || undefined,
    SHIPFAST_API_KEY: dotenv.parsed.SHIPFAST_API_KEY || undefined,
    SHIPFAST_API_HMAC_SECRET: dotenv.parsed.SHIPFAST_API_HMAC_SECRET || undefined,
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
    throw new Error("Missing Env Vars values for: " + missing_env_vars.slice(0, -2)) // removes last comma in the string
}

module.exports = {
    config,
}
