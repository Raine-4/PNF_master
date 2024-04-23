package com.pnfmaster.android.utils

import android.content.res.Resources
import com.pnfmaster.android.MyApplication
import java.util.Locale

fun setLocale(resources: Resources) {
    val language = MyApplication.sharedPreferences.getString("language", "en") ?: "en"
    val currentLocale = if (language == "en") Locale.ENGLISH else Locale.CHINESE
    Locale.setDefault(currentLocale)
    val config = resources.configuration
    config.setLocale(currentLocale)
    resources.updateConfiguration(config, resources.displayMetrics)
}