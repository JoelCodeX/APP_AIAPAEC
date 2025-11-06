package com.jotadev.aiapaec

import android.app.Application
import com.jotadev.aiapaec.data.storage.TokenStorage
import org.opencv.android.OpenCVLoader

class AIAPAECApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenStorage.init(this)
        OpenCVLoader.initDebug()
    }
}