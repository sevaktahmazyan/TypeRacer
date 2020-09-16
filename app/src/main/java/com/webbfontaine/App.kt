package com.webbfontaine

import android.app.Application
import com.webbfontaine.typeracer.network.ApiService

class App : Application() {

    private var apiService: ApiService? = null

    companion object {
        private lateinit var app: App
        fun instance(): App {
            return app
        }
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
}