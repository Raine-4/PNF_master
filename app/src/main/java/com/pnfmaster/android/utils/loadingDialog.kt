package com.pnfmaster.android.utils

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.pnfmaster.android.R

class LoadingDialog(private val context: Context) {
    private val dialog: Dialog
    init {
        val inflater = (context as AppCompatActivity).layoutInflater
        val view = inflater.inflate(R.layout.loading_dialog, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(view)

        dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        // 设置对话框的位置为屏幕正中间
        val window = dialog.window
        window?.let {
            val layoutParams = it.attributes
            layoutParams.gravity = Gravity.CENTER
            it.attributes = layoutParams
        }
    }

    fun show() {
        Log.d("loadingDialog", "loadingDialog.show()")
        dialog.show()
    }

    fun dismiss() {
        Log.d("loadingDialog", "loadingDialog.dismiss()")
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    fun block(time: Long) {
        val handler = Handler()
        // 阻塞线程，同时播放动画
        Thread.sleep(time)
        val dialog = LoadingDialog(context)
        // 启动动画
        dialog.show()
        // 延迟后关闭对话框
        handler.postDelayed({ dialog.dismiss() }, time)
    }
}
