package com.pnfmaster.android

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.SharedPreferences
import android.util.Log


class MyApplication: Application() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context : Context
        var bluetoothDevice: BluetoothDevice? = null
        var bluetoothSocket: BluetoothSocket? = null
        var DB_VERSION: Int = 1
        var isSkipped = false
        var userId: Int = -1
        var id: Int = -1 // ParamsGroup id
        lateinit var sharedPreferences: SharedPreferences
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        // get the current language from sharedPreferences
        sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString("language", "en") ?: "en"
        Log.d("MyApplication", "onCreate: Language is $language")

    }

}