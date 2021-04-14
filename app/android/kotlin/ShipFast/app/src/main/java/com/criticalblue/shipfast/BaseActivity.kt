package com.criticalblue.shipfast

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.criticalblue.shipfast.config.DemoStage
import com.criticalblue.shipfast.config.CURRENT_DEMO_STAGE

open class BaseActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var primary_color: String
        var primary_dark_color: String

        when (CURRENT_DEMO_STAGE) {
            DemoStage.API_KEY_PROTECTION -> {
                primary_color = "#3f51b5"
                primary_dark_color = "#303f9f"
            }
            DemoStage.HMAC_STATIC_SECRET_PROTECTION -> {
                primary_color = "#ec971f"
                primary_dark_color = "#d58512"

            }
            DemoStage.HMAC_DYNAMIC_SECRET_PROTECTION -> {
                primary_color = "#d9534f"
                primary_dark_color = "#d43f3a"
            }
            DemoStage.CERTIFICATE_PINNING_PROTECTION -> {
              primary_color = "#868646"
              primary_dark_color = "#646435"
            }
            DemoStage.APPROOV_APP_AUTH_PROTECTION -> {
                primary_color = "#449d44"
                primary_dark_color = "#398439"
            }
        }

        this.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        this.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        this.window.statusBarColor = Color.parseColor(primary_dark_color)

        this.supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor(primary_color)))
    }
}
