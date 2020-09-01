package com.criticalblue.shipfast

import android.app.Application
import android.util.Log
import com.criticalblue.shipfast.config.DemoStage
import com.criticalblue.shipfast.config.CURRENT_DEMO_STAGE
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// *** UNCOMMENT THE CODE BELOW FOR APPROOV ***
import io.approov.framework.okhttp.ApproovService

class ShipFastApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // *** UNCOMMENT THE CODE BELOW FOR APPROOV ***
        approovService = ApproovService(applicationContext, resources.getString(R.string.approov_config))
        approovService!!.setBindingHeader("Authorization")
        Log.d(TAG, "Created The ShipFastApp")
    }

    companion object {
        // *** UNCOMMENT THE CODE BELOW FOR APPROOV ***
        var approovService: ApproovService? = null

        fun getOkHttpClient(): OkHttpClient {
            when (CURRENT_DEMO_STAGE) {
                DemoStage.APPROOV_APP_AUTH_PROTECTION -> {

                    // *** COMMENT OUT THE CODE BELOW FOR APPROOV ***
                    // throw RuntimeException("The Approov demo stage is enabled, but Approov OkHttpClient not configured!")

                    // *** UNCOMMENT THE CODE BELOW FOR APPROOV ***
                    // The Approov OkHttpClient with the correct pins preset
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
