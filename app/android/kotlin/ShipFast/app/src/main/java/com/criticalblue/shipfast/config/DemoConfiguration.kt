/*
Copyright (C) 2020 CriticalBlue Ltd.

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.criticalblue.shipfast.config

import com.criticalblue.shipfast.BuildConfig

private val jniEnv = JniEnv()

/**
 * The enumeration of various stages of the demo.
 */
enum class DemoStage {
    API_KEY_PROTECTION,
    /** The demo which introduces API request signing by HMAC using a static secret in code */
    HMAC_STATIC_SECRET_PROTECTION,
    /** The demo which introduces API request signing by HMAC using a dynamic secret in code */
    HMAC_DYNAMIC_SECRET_PROTECTION,
    /** The demo which uses CriticalBlue Approov protection by authenticating the app */
    APPROOV_APP_AUTH_PROTECTION
}

object ApiUrl {
    fun build(): String {
        val api_version = when (BuildConfig.DEMO_STAGE) {
            "API_KEY_PROTECTION" -> "v1"
            "HMAC_STATIC_SECRET_PROTECTION" -> "v2"
            "HMAC_DYNAMIC_SECRET_PROTECTION" -> "v3"
            "APPROOV_APP_AUTH_PROTECTION" -> "v4"
            else -> ""
        }

        return "${jniEnv.getApiBaseUrl()}/${api_version}"
    }
}

/** The current demo stage */
val CURRENT_DEMO_STAGE = DemoStage.valueOf(BuildConfig.DEMO_STAGE)

/** The ShipFast server's base URL */
//const val API_BASE_URL = "http://10.0.2.2:3333"
val API_BASE_URL = ApiUrl.build()

/** The maximum number of attempts to try to make an API request before reporting a failure */
const val API_REQUEST_ATTEMPTS = 3

const val API_REQUEST_RETRY_SLEEP_MILLESECONDS = 1000

const val SNACKBAR_DISPLAY_MILLESECONDS = 1000

// Hack to:
//  * wait for the previous message to be displayed.
//  * run only the intent after the user had a chance to see the snack bar success message.
const val SNACKBAR_THREAD_SLEAP_MILLESECONDS = 1000
