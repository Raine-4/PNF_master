package com.pnfmaster.android.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

// 传入字符串
fun View.showSB(text: String,
                actionText: String? =null,
                duration: Int = Snackbar.LENGTH_SHORT,
                block: (() -> Unit)? = null) {
    val snackbar = Snackbar.make(this, text, duration)
    if (actionText != null && block != null) {
        snackbar.setAction(actionText) {
            block()
        }
    }
    snackbar.show()
}

// 传入字符串资源id
fun View.showSB(resId: Int,
                actionResId: String? =null,
                duration: Int = Snackbar.LENGTH_SHORT,
                block: (() -> Unit)? = null) {
    val snackbar = Snackbar.make(this, resId, duration)
    if (actionResId != null && block != null) {
        snackbar.setAction(actionResId) {
            block()
        }
    }
    snackbar.show()
}