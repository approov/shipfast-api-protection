/*****************************************************************************
 * Project:     ShipFast API Protection (App)
 * File:        LoginActivity.kt
 * Original:    Created on 2 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * The user login activity.
 *****************************************************************************/

package com.criticalblue.shipfast

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials

/**
 * The Login activity class.
 *
 * TODO: use Auth0 CredentialsManager
 * TODO: use Android Lock rather than web Lock
 */
class LoginActivity : AppCompatActivity() {

    /** The progress bar */
    private lateinit var loginProgressBar: ProgressBar

    /** The user login button */
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Add an 'on-click' listener to the user login button
        loginButton = findViewById(R.id.loginButton)
        loginButton.setOnClickListener { button -> performLogin()}

        loginProgressBar = findViewById(R.id.loginProgressBar)
    }

    /**
     * Perform user login using Auth0.
     */
    private fun performLogin() {

        startProgress()

        val auth0 = Auth0(this@LoginActivity)
        auth0.isOIDCConformant = true
        WebAuthProvider.init(auth0)
                .withScheme("demo")
                .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
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
