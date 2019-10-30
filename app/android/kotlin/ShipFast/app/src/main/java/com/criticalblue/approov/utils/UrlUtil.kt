package com.criticalblue.approov.utils

import android.util.Log
import com.criticalblue.approov.ApproovSdk
import java.net.MalformedURLException
import java.net.URL

/**
 * A class for Url related utilities.
 */
object UrlUtil {

    /**
     * Will extract the hostname from the given url.
     *
     * @param url The full url from where we want to extract the hostname.
     *
     * @return The hostname from `https://example.com/path` as `example.com`.
     */
     fun extractHostname(url: String): String? {
        try {
            val urlParser = URL(url)
            val apiBaseUrl = urlParser.authority
            Log.i(ApproovSdk.TAG, "API BASE URL: $apiBaseUrl")
            return apiBaseUrl
        } catch (e: MalformedURLException) {
            Log.e(ApproovSdk.TAG, e.message)
            return null
        }
    }
}