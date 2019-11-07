/*****************************************************************************
 * Project:     Approov Kotlin Framework
 * Copyright(c) 2019 by CriticalBlue Ltd.
 *****************************************************************************/

package com.criticalblue.approov

import android.content.res.AssetManager
import android.util.Log
import com.criticalblue.approov.exceptions.ApproovIOFatalException
import com.criticalblue.approovsdk.Approov
import java.io.*

/**
 * This class will handle the Approov dynamic configuration for read and write operations.
 */
class ApproovSdkConfiguration(val assets: AssetManager, val outputStream: FileOutputStream): ApproovSdkConfigurationInterface {

    /**
     * Reads the Approov initial configuration, that MUST be present when the app is compiled.
     *
     * @throws ApproovIOFatalException When the is not able to read the initial Approov config.
     *
     * @return The Approov initial config that was shipped with the release.
     */
    override fun readInitialConfig(): String {

        try {
            return this.readFile("approov-initial.config")
        } catch (e: IOException) {
            Log.i(ApproovFramework.TAG, "Failed to read the initial Approov configuration for: " + e.message)
            throw ApproovIOFatalException("Initial Appoov config is missing or is invalid.")
        }
    }

    /**
     * Reads the Approov dynamic configuration, that is not required to be present when the app is
     *  compiled.
     *
     * @return The Approov dynamic config that is persisted from the Approov token fetch result.
     */
    override fun readDynamicConfig(): String {

        try {
            return this.readFile("approov-dynamic.config")
        } catch (e: IOException) {
            // This is not an issue when trying to read an Approov dynamic configuration, because:
            //  * Its expected to not exist when the app is first launched after being installed.
            //  * In the event of a corrupted Approov dynamic configuration the app will receive a
            //    new configuration update in the next Approov token fetch.
        }

        return ""
    }

    /**
     * Reads the Approov initial or dynamic configuration from the app assets folder, depending on
     *  the file name provided.
     *
     * @param filename The Approov config file to read.
     *
     * @return The content of the file Approov config file.
     */
    private fun readFile(filename: String): String {

        val stream = this.assets.open(filename)
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        val config = reader.readLine()
        reader.close()

        return config
    }

    /**
     * Saves the Approov dynamic configuration to the local app assets folder. This must be called
     *  after every Approov token fetch where isConfigChanged() is set to `true`.
     * Any new saved configuration will be only read for the first time in the next app startup,
     *  because it's already updated in memory.
     *
     * @return True when succeeds, False otherwise.
     */
    override fun saveDynamicConfig(): Boolean {

        val dynamicConfig: String = Approov.fetchConfig()

        return try {
            val printStream = PrintStream(this.outputStream)
            printStream.print(dynamicConfig)
            printStream.close()
            Log.i(ApproovFramework.TAG, "Wrote Approov dynamic configuration into the app assets folder.")
            true
        } catch (e: IOException) {
            // This is an error, but is not fatal, as the app will receive a new update if the
            //  stored one is corrupted in some way
            Log.e(ApproovFramework.TAG, "Cannot write Approov dynamic configuration: " + e.message)

            false
        }
    }
}
