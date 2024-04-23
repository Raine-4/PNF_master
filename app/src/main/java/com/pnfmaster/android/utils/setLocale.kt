package com.pnfmaster.android.utils

import android.content.res.Resources
import android.util.Log
import com.pnfmaster.android.MyApplication
import java.util.Locale

fun setLocale(resources: Resources) {
    val language = MyApplication.sharedPreferences.getString("language", "en") ?: "en"
    val currentLocale = if (language == "en") Locale.ENGLISH else Locale.CHINESE
    Log.d("setLocale", "setLocale: currentLocale is ${currentLocale.displayLanguage}")
    Locale.setDefault(currentLocale)
    val config = resources.configuration
    config.setLocale(currentLocale)
    resources.updateConfiguration(config, resources.displayMetrics)
}