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

package com.criticalblue.shipfast

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.criticalblue.shipfast.config.JniEnv
import com.criticalblue.shipfast.user.saveUserCredentials


/**
 * The Login activity class.
 *
 * TODO: use Auth0 CredentialsManager
 * TODO: use Android Lock rather than web Lock
 */
class LoginActivity : BaseActivity() {

    /** The progress bar */
    private lateinit var loginProgressBar: ProgressBar

    /** The user login button */
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        // Add an 'on-click' listener to the user login button
        loginButton = findViewById(R.id.loginButton)
        loginButton.setOnClickListener { _ -> performLogin()}

        loginProgressBar = findViewById(R.id.loginProgressBar)
    }

    /**
     * Get the value for the given key from the `AndroidManifest.xml` file.
     *
     * @param key The key name to retrieve the value for.
     *
     * @return the value for the given key.
     */
    private fun getManifestValueFor(key: String): String {

        var value: String? = null

        try {
            val app = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            value = app.metaData.getString(key)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "${key} NameNotFound: " + e.message)
        } catch (e: NullPointerException) {
            Log.e(TAG, "${key}, NullPointer: " + e.message)
        }

        if (value == null) {
            throw RuntimeException("Android Manifest is missing value for key: ${key}")
        }

        return value
    }

    /**
     * Perform user login using Auth0.
     */
    private fun performLogin() {

        startProgress()

        val jniEnv = JniEnv()

        // The Auth0 domain is not retrieve from the `JninEnv`, because the Auth0 package requires
        //  it to be present in the `build.gradle` files as a manifest placeholder.
        val auth0Domain = this.getManifestValueFor("com.criticalblue.shipfast.auth0Domain")

        val auth0 = Auth0(jniEnv.getAuth0ClientId(), auth0Domain)

        // Always prompt the user, otherwise if we have multiple Gmail accounts
        // we will not know wich one was used by the ShipFast App in order to
        // login with the same one in ShipRaider.
        val parameters: HashMap<String, Any> = HashMap()
        parameters.put("prompt", "select_account")

        auth0.isOIDCConformant = true
        WebAuthProvider.init(auth0)
                .withScheme(this.getManifestValueFor("com.criticalblue.shipfast.auth0Scheme"))
                .withAudience(String.format("https://%s/userinfo", auth0Domain))
                .withParameters(parameters)
                .start(this@LoginActivity, object : AuthCallback {
                    override fun onFailure(dialog: Dialog) {
                        stopProgress()
                        runOnUiThread {
                            dialog.show()
                        }
                    }

                    override fun onFailure(exception: AuthenticationException) {
                        stopProgress()
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Login Failed (${exception.localizedMessage})",
                                    Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onSuccess(credentials: Credentials) {
                        stopProgress()
                        saveUserCredentials(this@LoginActivity, credentials)
                        val intent = Intent(this@LoginActivity, ShipmentActivity::class.java)
                        startActivity(intent)
                    }
                })
    }

    /**
     * Start showing progress.
     */
    private fun startProgress() {
        runOnUiThread {
            loginProgressBar.visibility = View.VISIBLE
        }
    }

    /**
     * Stop showing progress.
     */
    private fun stopProgress() {
        runOnUiThread {
            loginProgressBar.visibility = View.INVISIBLE
        }
    }
}
