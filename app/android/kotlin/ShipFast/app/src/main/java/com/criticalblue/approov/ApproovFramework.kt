/*****************************************************************************
 * Project:     Approov Kotlin Framework
 * Copyright(c) 2019 by CriticalBlue Ltd.
 *****************************************************************************/

package com.criticalblue.approov

import android.content.Context
import android.util.Log
import com.criticalblue.approovsdk.Approov
import com.criticalblue.approov.exceptions.ApproovIOFatalException

/**
 * This singleton wraps the Approov SDK in order to initialize it only once, and to allow to save
 *  a new dynamic configuration.
 */
object ApproovFramework {

    private var tokenBindingHeader: String? = null
    private var initialized: Boolean = false

    val TAG = "APPROOV_FRAMEWORK"

    private lateinit var config: ApproovSdkConfigurationInterface

    /**
     * Initializes the Approov SDK with the given context, but only in the first time is called.
     *
     * @throws ApproovIOFatalException When fails initialization or when is called without being
     *                                  initialized.
     *
     * @param context The application context.
     */
    fun initialize(context: Context, config: ApproovSdkConfigurationInterface) {
        this.initializeApproovSdk(context, config, null)
    }

    fun initialize(context: Context, config: ApproovSdkConfigurationInterface, tokenBindingHeader: String) {
        this.initializeApproovSdk(context, config, tokenBindingHeader)
    }

    private fun initializeApproovSdk(context: Context, config: ApproovSdkConfigurationInterface, tokenBindingHeader: String?) {

        this.config = config
        this.tokenBindingHeader = tokenBindingHeader

        // The Approov SDK cannot be initialize more than once.
        if (this.initialized) {
            return
        }

        val dynamicConfig: String = this.config.readDynamicConfig()

        try {

            Approov.initialize(context.applicationContext, this.config.readInitialConfig(), dynamicConfig, null)
            this.initialized = true

        } catch (e: IllegalArgumentException) {

            this.initialized = false

            Log.e(TAG, "Approov SDK initialization failed: " + e.message)

            // This is fatal, because if the Approov SDK cannot be initialized, all subsequent
            // attempts to use the it will fail.
            throw ApproovIOFatalException("Approov SDK initialization failed.")
        }

        // It's only an empty configuration if we are installing the app.
        if (dynamicConfig == "") {
            this.config.saveDynamicConfig()
        }
    }

    fun getTokenBindingHeader(): String? {
        return this.tokenBindingHeader
    }

    /**
     * Will update the Approov dynamic configuration with the one present in the last Approov token
     *  fetch.
     *
     * @throws ApproovIOFatalException When is called without initializing first the Approov SDK.
     */
    fun saveDynamicConfig(): Boolean {

        if (this.initialized.not()) {
            throw ApproovIOFatalException("Approov SDK it's not initialized.")
        }

        return this.config.saveDynamicConfig()
    }
}
