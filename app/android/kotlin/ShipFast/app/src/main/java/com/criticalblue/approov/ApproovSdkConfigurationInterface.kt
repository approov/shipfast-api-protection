/*****************************************************************************
 * Project:     Approov Kotlin Framework
 * Copyright(c) 2019 by CriticalBlue Ltd.
 *****************************************************************************/

package com.criticalblue.approov

import com.criticalblue.approov.exceptions.ApproovIOFatalException

interface ApproovSdkConfigurationInterface {

    /**
     * Reads the Approov initial configuration, that MUST be present when the app is compiled.
     *
     * @throws ApproovIOFatalException When the is not able to read the initial Approov config.
     *
     * @return The Approov initial config that was shipped with the release.
     */
    fun readInitialConfig(): String

    /**
     * Reads the Approov dynamic configuration, that is not required to be present when the app is
     *  compiled.
     *
     * @return The Approov dynamic config that is persisted from the Approov token fetch result.
     */
    fun readDynamicConfig(): String

    /**
     * Saves the Approov dynamic configuration. This must be called after every Approov token fetch
     *  when, `isConfigChanged()` is set to `true`.
     * Any new saved configuration will be only read for the first time in the next app startup,
     *  because it's already updated in memory.
     *
     * @throws ApproovIOFatalException When an IOException occurs while save the configuration.
     *
     * @return True when succeeds, False otherwise.
     */
    fun saveDynamicConfig(): Boolean
}