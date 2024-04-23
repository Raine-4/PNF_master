package com.pnfmaster.android.newuser

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.pnfmaster.android.BaseActivity
import com.pnfmaster.android.MyApplication
import com.pnfmaster.android.R
import com.pnfmaster.android.database.MyDatabaseHelper
import com.pnfmaster.android.database.connect
import com.pnfmaster.android.databinding.ActivityNewuserBinding
import com.pnfmaster.android.utils.ActivityCollector
import com.pnfmaster.android.utils.LoadingDialog
import com.pnfmaster.android.utils.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NewuserActivity : BaseActivity() {
    private lateinit var binding: ActivityNewuserBinding
    private lateinit var dbHelper: MyDatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewuserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = MyDatabaseHelper(this, "user.db", MyApplication.DB_VERSION)
        ActivityCollector.addActivity(this)

        setSupportActionBar(binding.toolbarNewUser)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = getString(R.string.NewUser)
        }

        binding.registerButton.setOnClickListener {
            val username = binding.registerUsername.text.toString()
            val password = binding.registerPassword.text.toString()
            val confirmPsw = binding.confirmPassword.text.toString()
            if (isLegal(username, password, confirmPsw)) {
                val intent = Intent(this, UserInfoActivity::class.java)
                intent.putExtra("account", username)
                intent.putExtra("psw", password)
                startActivity(intent)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                finish()
            }
        }
        return true
    }

    @SuppressLint("Range")
    private fun isLegal(username: String, password: String, confirmPsw: String): Boolean {
        if (username == "" || password == "") {
            getString(R.string.input_username_psw).Toast()
            return false
        } else {
            if (password != confirmPsw) {
                getString(R.string.not_the_same).Toast()
                return false
            }
        }

        // ----------------------------------
        var flag = false
        // TODO: 设置成dispatcher(IO)和progressDialog的形式
        fun main() {
            GlobalScope.launch {
                flag = connect.isUsernameUsed(username)
            }
            Thread.sleep(1000)
        }
        val loadingDialog = LoadingDialog(this)
        loadingDialog.show()
        main()
        loadingDialog.dismiss()
        // ----------------------------------

        if (flag) {
            getString(R.string.already_exist).Toast()
            return false
        }

        /* 使用本地数据库
        val db = dbHelper.writableDatabase
        val cursor = db.query("User", arrayOf("username"), null, null, null, null, null)
        if (cursor.moveToFirst()) {
            val curAccount = cursor.getString(cursor.getColumnIndex("username"))
            if (curAccount == username) {
                "用户名已存在。".Toast()
                return false
            }
        }
        cursor.close()
        */

        return true
    }
}