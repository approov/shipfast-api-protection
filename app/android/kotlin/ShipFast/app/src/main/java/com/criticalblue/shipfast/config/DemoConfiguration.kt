/*****************************************************************************
 * Project:     ShipFast API Protection (App)
 * File:        DemoConfiguration.kt
 * Original:    Created on 24 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * A class for configuring the ShipFast demo.
 *****************************************************************************/

package com.criticalblue.shipfast.config


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

val jniEnv = JniEnv()

/** The current demo stage */
val currentDemoStage = DemoStage.API_KEY_PROTECTION
//val currentDemoStage = DemoStage.APPROOV_APP_AUTH_PROTECTION

// 51.535472, -0.104971   -> London
// 37.441883, -122.143019 -> Palo Alto, California
// 55.944879, -3.181546   -> Edinburgh
val DRIVER_LATITUDE: Double = jniEnv.getDriverLatitude()
val DRIVER_LONGITUDE: Double = jniEnv.getDriverLongitude()

/** The ShipFast server's base URL */
//const val API_BASE_URL = "http://10.0.2.2:3333"
val API_BASE_URL = jniEnv.getApiBaseUrl()

/** The maximum number of attempts to try to make an API request before reporting a failure */
const val API_REQUEST_ATTEMPTS = 3

const val API_REQUEST_RETRY_SLEEP_MILLESECONDS = 1000

const val SNACKBAR_DISPLAY_MILLESECONDS = 1000

// Hack to:
//  * wait for the previous message to be displayed.
//  * run only the intent after the user had a chance to see the snack bar success message.
const val SNACKBAR_THREAD_SLEAP_MILLESECONDS = 1000
