package com.pnfmaster.android.newuser

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.pnfmaster.android.BaseActivity
import com.pnfmaster.android.LoginActivity
import com.pnfmaster.android.MyApplication
import com.pnfmaster.android.R
import com.pnfmaster.android.database.MyDatabaseHelper
import com.pnfmaster.android.databinding.ActivityNewuserBinding
import com.pnfmaster.android.utils.Toast

class NewuserActivity : BaseActivity() {
    private lateinit var binding: ActivityNewuserBinding
    private lateinit var dbHelper: MyDatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewuserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = MyDatabaseHelper(this, "user.db", MyApplication.DB_VERSION)

        setSupportActionBar(binding.toolbarNewUser)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = "新用户注册"
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
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        return true
    }

    @SuppressLint("Range")
    private fun isLegal(username: String?, password: String?, confirmPsw: String?): Boolean {
        if (username == "" || password == "") {
            "请输入用户名和密码".Toast()
            return false
        } else {
            if (password != confirmPsw) {
                "密码与确认密码不一致！".Toast()
                return false
            }
        }

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

        return true
    }
}