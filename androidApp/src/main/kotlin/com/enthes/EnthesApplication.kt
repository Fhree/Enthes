package com.enthes

import android.app.Application
import com.enthes.di.initKoin
import org.koin.android.ext.koin.androidContext

class EnthesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@EnthesApplication)
        }
    }
}
