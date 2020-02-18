const dotenv = require('dotenv').config()

if (dotenv.error) {
  throw dotenv.error
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

let CURRENT_STAGE = STAGES[demo_stage]

module.exports = {
    STAGES,
    CURRENT_STAGE,
}
