package com.pnfmaster.android.utils

import android.app.ProgressDialog
import android.content.Context
import android.util.Log

class CustomProgressDialog(private val context: Context, private val message: String) {

    private val progressDialog = ProgressDialog(context)

    fun show() {
        Log.d("CustomProgressDialog", "showed")
        progressDialog.setMessage(message)
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    fun dismiss() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
            Log.d("CustomProgressDialog", "dismissed")
        }
    }

}