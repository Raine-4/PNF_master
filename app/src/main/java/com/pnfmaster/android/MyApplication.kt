package com.pnfmaster.android

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context


class MyApplication: Application() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context : Context
        var bluetoothDevice: BluetoothDevice? = null
        var bluetoothSocket: BluetoothSocket? = null
        var DB_VERSION: Int = 1
        var isSkipped = false
        var userId: Int = -1
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

}