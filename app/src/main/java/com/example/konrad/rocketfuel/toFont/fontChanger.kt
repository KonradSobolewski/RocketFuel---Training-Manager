package com.example.konrad.rocketfuel.toFont

import android.app.Application
import com.example.konrad.rocketfuel.R
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

/**
 * Created by Konrad on 16.03.2018.
 */
class fontChanger : Application() {
    override fun onCreate() {
        super.onCreate()
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("sans_narrow/Sans_regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
    }
}