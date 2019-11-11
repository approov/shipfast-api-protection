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
//CURRENT_STAGE= process.env.SHIPFAST_API_DEMO_STAGE || DEMO_STAGE.API_KEY_PROTECTION
CURRENT_STAGE = STAGES.APPROOV_APP_AUTH_PROTECTION


module.exports = {
    STAGES,
    CURRENT_STAGE,
}
