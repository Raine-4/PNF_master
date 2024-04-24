package com.pnfmaster.android.utils

import android.app.ProgressDialog
import android.content.Context
import com.pnfmaster.android.R

class MyProgressDialog(private val context: Context) {

    private val progressDialog = ProgressDialog(context)

    fun show() {
        progressDialog.setMessage(context.getString(R.string.loading))
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    fun dismiss() {
        progressDialog.dismiss()
    }

}