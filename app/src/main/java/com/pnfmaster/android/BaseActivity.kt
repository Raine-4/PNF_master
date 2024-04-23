package com.pnfmaster.android

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.pnfmaster.android.utils.setLocale

open class BaseActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("BaseActivity", javaClass.simpleName)
    }

    /**
     * Before onCreate()
     * Change the language of the first page (LoginActivity in this case)
     */
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        setLocale(resources)
    }

    override fun onResume() {
        super.onResume()
        setLocale(resources)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setLocale(resources)
    }

}