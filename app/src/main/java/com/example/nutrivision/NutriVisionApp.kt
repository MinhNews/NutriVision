package com.example.nutrivision

import android.app.Application
import com.example.nutrivision.di.AppContainer

class NutriVisionApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
