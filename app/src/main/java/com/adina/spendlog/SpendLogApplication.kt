package com.adina.spendlog

import android.app.Application
import com.adina.spendlog.data.AppContainer

class SpendLogApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
