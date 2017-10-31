/*****************************************************************************
 * Project:     ShipFast API Protection (App)
 * File:        DemoConfiguration.kt
 * Original:    Created on 24 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * A class for configuring the ShipFast demo.
 *****************************************************************************/

package com.criticalblue.shipfast

/**
 * The enumeration of various stages of the demo.
 */
enum class DemoStage {
    /** The demo which uses basic protection by way of API key specified in the manifest */
    API_KEY_PROTECTION,
    /** The demo which introduces API request signing by HMAC using a static secret in code */
    HMAC_STATIC_SECRET_PROTECTION,
    /** The demo which introduces API request signing by HMAC using a dynamic secret in code */
    HMAC_DYNAMIC_SECRET_PROTECTION,
    /** The demo which uses CriticalBlue Approov protection by authenticating the app */
    APPROOV_APP_AUTH_PROTECTION
}

/** The current demo stage */
val currentDemoStage = DemoStage.API_KEY_PROTECTION

/** The ShipFast server's base URL */
const val SERVER_BASE_URL = "https://10.0.2.2"