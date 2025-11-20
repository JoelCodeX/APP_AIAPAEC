package com.jotadev.aiapaec

import android.app.Application
import com.jotadev.aiapaec.data.storage.TokenStorage

class AIAPAECApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenStorage.init(this)
    }
}