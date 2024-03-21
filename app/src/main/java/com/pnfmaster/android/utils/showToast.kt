package com.pnfmaster.android.utils

import android.widget.Toast
import com.pnfmaster.android.MyApplication

fun String.Toast(duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(MyApplication.context, this, duration).show()
}