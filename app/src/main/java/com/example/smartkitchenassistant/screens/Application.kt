package com.example.smartkitchenassistant.screens

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = mapOf(
            "cloud_name" to "dz1uykaxp" // tu cloud_name
        )

        MediaManager.init(this, config)
    }
}
