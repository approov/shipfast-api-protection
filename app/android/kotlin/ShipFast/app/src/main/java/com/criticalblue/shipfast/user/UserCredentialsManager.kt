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

package com.criticalblue.shipfast.user

import android.content.Context
import com.auth0.android.result.Credentials

/** The name of the user credentials preference */
const val PREFS_NAME = "Auth0"
/** The preference key for the refresh token */
const val REFRESH_TOKEN__PREFS_KEY = "RefreshToken"
/** The preference key for the access token */
const val ACCESS_TOKEN__PREFS_KEY = "AccessToken"
/** The preference key for the ID token */
const val ID_TOKEN__PREFS_KEY = "IDToken"
/** The preference key for the token type */
const val TOKEN_TYPE__PREFS_KEY = "TokenType"
/** The preference key for the 'expires in' value */
const val EXPIRES_IN__PREFS_KEY = "ExpiresIn"

/**
 * Save the given user credentials to the preference store.
 *
 * @param context the application context which holds the preference store
 * @param credentials the user credentials to save
 */
fun saveUserCredentials(context: Context, credentials: Credentials) {

    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPrefs.edit()
            .putString(ID_TOKEN__PREFS_KEY, credentials.idToken)
            .putString(REFRESH_TOKEN__PREFS_KEY, credentials.refreshToken)
            .putString(ACCESS_TOKEN__PREFS_KEY, credentials.accessToken)
            .putString(TOKEN_TYPE__PREFS_KEY, credentials.type)
            .putLong(EXPIRES_IN__PREFS_KEY, credentials.expiresIn ?: 0)
            .apply()
}

/**
 * Load the user credentials from the preference store.
 *
 * @param context the application context which holds the preference store
 * @return the user credentials
 */
fun loadUserCredentials(context: Context): Credentials {

    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return Credentials(
            sharedPrefs.getString(ID_TOKEN__PREFS_KEY, null),
            sharedPrefs.getString(ACCESS_TOKEN__PREFS_KEY, null),
            sharedPrefs.getString(TOKEN_TYPE__PREFS_KEY, null),
            sharedPrefs.getString(REFRESH_TOKEN__PREFS_KEY, null),
            sharedPrefs.getLong(EXPIRES_IN__PREFS_KEY, 0))
}

/**
 * Delete the user credentials in the preference store.
 *
 * @param context the application context which holds the preference store
 */
fun deleteUserCredentials(context: Context) {

    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPrefs.edit()
            .remove(ID_TOKEN__PREFS_KEY)
            .remove(REFRESH_TOKEN__PREFS_KEY)
            .remove(ACCESS_TOKEN__PREFS_KEY)
            .remove(TOKEN_TYPE__PREFS_KEY)
            .remove(EXPIRES_IN__PREFS_KEY)
            .apply()
}
