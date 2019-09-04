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
//val currentDemoStage = DemoStage.APPROOV_APP_AUTH_PROTECTION

val latitude = System.getenv("ANDROID_EMULATOR_LATITUDE") ?: "51.5355"
val longitude = System.getenv("ANDROID_EMULATOR_LONGITUDE") ?: "-0.104971"

val ANDROID_EMULATOR_LATITUDE: Double = latitude.toDouble()
val ANDROID_EMULATOR_LONGITUDE: Double = longitude.toDouble()


/** The ShipFast server's base URL */
//const val SERVER_BASE_URL = "http://10.0.2.2:3333"

val SERVER_PROTOCOL =  System.getenv("SHIP_FAST_HTTP_PROTOCOL") ?: "http"

/** The ShipFast server's base URL */
val SERVER_DOMAIN =  System.getenv("SHIP_FAST_EMULATOR_DOMAIN") ?: "10.0.2.2:3333"
val SERVER_BASE_URL =  SERVER_PROTOCOL + "://" + SERVER_DOMAIN
