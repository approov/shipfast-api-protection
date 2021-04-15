package com.criticalblue.shipfast

import android.app.Application
import android.util.Log
import com.criticalblue.shipfast.config.DemoStage
import com.criticalblue.shipfast.config.CURRENT_DEMO_STAGE
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// *** APPROOV IMPLEMENTATION ***
// The Approov service with the Android OkHttp client to wrap the Approov SDK usage.
// @link https://github.com/approov/quickstart-android-kotlin-okhttp
import io.approov.framework.okhttp.ApproovService

class ShipFastApp : Application() {
    override fun onCreate() {
        super.onCreate()

        when (CURRENT_DEMO_STAGE) {
            DemoStage.APPROOV_APP_AUTH_PROTECTION -> {

              // *** APPROOV IMPLEMENTATION ***
              // Initializes the Approov SDK with the Approov initial configuration
              // @link https://approov.io/docs/latest/approov-usage-documentation/#sdk-configuration
              approovService = ApproovService(applicationContext, resources.getString(R.string.approov_config))

              // *** APPROOV IMPLEMENTATION ***
              // Enables the Approov token binding advanced feature, by binding the
              // Approov token with the Authorization token header.
              // @link https://approov.io/docs/latest/approov-usage-documentation/#token-binding
              approovService!!.setBindingHeader("Authorization")
            }
        }

        Log.d(TAG, "Created The ShipFastApp")
    }

    companion object {
        // *** APPROOV IMPLEMENTATION ***
        var approovService: ApproovService? = null

        fun getOkHttpClient(): OkHttpClient {
            when (CURRENT_DEMO_STAGE) {
                DemoStage.APPROOV_APP_AUTH_PROTECTION -> {

                    // *** APPROOV IMPLEMENTATION ***
                    // The Approov OkHttpClient with the correct pins preset via
                    // the Approov dynamic pinning feature.
                    // @link https://approov.io/docs/latest/approov-usage-documentation/#approov-dynamic-pinning
                    return approovService!!.getOkHttpClient()
                }
                else -> {
                    // Use a simple client for non-Approov demo stages
                    return OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .writeTimeout(15, TimeUnit.SECONDS)
                            .build()
                }
            }
        }
    }
}
