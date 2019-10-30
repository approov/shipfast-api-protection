package com.criticalblue.approov

import android.content.res.AssetManager
import android.util.Log
import java.io.*
import com.criticalblue.approovsdk.Approov
import com.criticalblue.approov.ApproovSdk.TAG
import com.criticalblue.approov.exceptions.ApproovSdkFatalException

/**
 * This class will handle the Approov dynamic configuration for read and write operations.
 */
class ApproovDynamicConfig(private val assets: AssetManager, private val outputStream: FileOutputStream) {

    /**
     * Reads the Approov initial configuration, that MUST be present when the app is compiled.
     *
     * @throws ApproovSdkFatalException When the error is not recoverable.
     *
     * @return The Approov initial config that was shipped with the release.
     */
    fun readInitialConfig(): String {
        return readFile("approov-initial.config") ?: throw ApproovSdkFatalException("Initial Appoov config is missing or is invalid.")
    }

    /**
     * Reads the Approov dynamic configuration, that is not required to be present when the app is
     *  compiled.
     *
     * @return The Approov dynamic config that is persisted from the Approov token fetch result.
     */
    fun readDynamicConfig(): String? {
        return readFile("approov-dynamic.config")
    }

    /**
     * Reads the Approov initial or dynamic configuration from the app assets folder, depending on
     *  the file name provided.
     *
     * @param filename The Approov config file to read.
     *
     * @return The content of the file Approov config file.
     */
    private fun readFile(filename: String): String? {
        var config: String? = null

        try {
            val stream = this.assets.open(filename)
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            config = reader.readLine()
            reader.close()
        } catch (e: IOException) {
            // This is not an issue when trying to read an Approov dynamic configuration, because:
            //  * Its expected to not exist when the app is first launched after being installed.
            //  * The Approov dynamic configuration is only created on the first Approov token fetch.
            //  * In the event of a corrupted Approov dynamic configuration the app will receive a
            //    new configuration update in the next Approov token fetch.
            //
            // This will be an issue only for reading the Approov initial config, but its taken care
            //  in the caller, the `readIntialConfig` method.
            Log.i(TAG, "Failed to read Approov configuration for: " + filename + ". Reason: " + e.message)
        }

        return config
    }

    /**
     * Saves the Approov dynamic configuration to the local app assets folder. This must be called
     *  after every Approov token fetch where isConfigChanged() is set to `true`.
     * Any new saved configuration will be only read for the first time in the next app startup,
     *  because ist already updated in memory.
     */
    fun saveApproovConfigUpdate() {

        val updateConfig = Approov.fetchConfig()

        if (updateConfig == null) {
            Log.e(TAG, "Could not get the Approov dynamic configuration")
            return
        }

        try {
            val printStream = PrintStream(this.outputStream)
            printStream.print(updateConfig)
            printStream.close()
            Log.i(TAG, "Wrote Approov dynamic configuration into the app assets folder.")
        } catch (e: IOException) {
            // This is an error, but is not fatal, as the app will receive a new update if the
            //  stored one is corrupted in some way
            Log.e(TAG, "Cannot write Approov dynamic configuration: " + e.message)
            return
        }
    }
}
