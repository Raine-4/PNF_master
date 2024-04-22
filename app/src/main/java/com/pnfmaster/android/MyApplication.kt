package com.pnfmaster.android

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.pnfmaster.android.utils.Toast
import java.util.Locale


class MyApplication: Application() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context : Context
        var bluetoothDevice: BluetoothDevice? = null
        var bluetoothSocket: BluetoothSocket? = null
        var DB_VERSION: Int = 1
        var isSkipped = false
        var userId: Int = -1
        var language: String = "en" // current language
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        if (language == "en") {
            setLocale(Locale.ENGLISH)

        } else {
            setLocale(Locale.SIMPLIFIED_CHINESE)
        }
    }

    fun setLocale(locale: Locale) {
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        if (language == "en") {
            "Language changed to ${locale.displayLanguage}".Toast()
        } else {
            "语言已更改为${locale.displayLanguage}".Toast()
        }
    }

}