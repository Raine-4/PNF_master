package com.pnfmaster.android

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.pnfmaster.android.database.MyDatabaseHelper
import com.pnfmaster.android.databinding.ActivityLoginBinding
import com.pnfmaster.android.newuser.NewuserActivity
import com.pnfmaster.android.utils.Toast
import kotlin.properties.Delegates

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private var id by Delegates.notNull<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 启动动画
        binding.appName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
        binding.logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.logo_animation))

        /* 先判断是否是从注册界面来的新用户，如果是的话就把他刚注册的用户名和密码填上去
         * 如果不是新用户，就启动记住密码功能。
         */
        val flag = intent.getIntExtra("newUserFlag", 0)
        val prefs = getPreferences(Context.MODE_PRIVATE)

        if (flag == 1) {
            val newAccount = intent.getStringExtra("newAccount")
            val newPassword = intent.getStringExtra("newPassword")
            binding.accountEdit.setText(newAccount)
            binding.pswEdit.setText(newPassword)
        } else {
            val isRemember = prefs.getBoolean("rememberPsw", false)
            if (isRemember) {
                val account = prefs.getString("account", "")
                val password = prefs.getString("password", "")
                binding.accountEdit.setText(account)
                binding.pswEdit.setText(password)
                binding.rememberPsw.isChecked = true
            }
        }

        // 登录
        binding.loginInBtn.setOnClickListener {
            val account = binding.accountEdit.text.toString()
            val psw = binding.pswEdit.text.toString()

            if (isRegistered(account, psw)) {
                if (flag == 0) {
                    // 如果勾选了“记住密码”选项
                    val editor = prefs.edit()
                    if (binding.rememberPsw.isChecked) {
                        editor.putBoolean("rememberPsw", true)
                        editor.putString("account", account)
                        editor.putString("password", psw)
                    } else { editor.clear() }
                    editor.apply()
                }
                val intent = Intent(this, ControlActivity::class.java)
                intent.putExtra("userAccount", account)
                intent.putExtra("userId", id)
                startActivity(intent)
                finish()
            } else { "用户名或密码错误".Toast() }
        }

        // 跳转至注册界面
        binding.newUserBtn.setOnClickListener {
            val intent = Intent(this, NewuserActivity::class.java)
            startActivity(intent)
        }

        binding.skipLoginEnter.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("提示")
                setMessage("跳过登录仍然可以使用全部基础功能，但是无法使用个性化服务，建议您登录后使用。")
                setPositiveButton("返回登录", null)
                setNegativeButton("直接使用") { _, _ ->
                    val intent = Intent(this@LoginActivity, ControlActivity::class.java)
                    intent.putExtra("skip", true)
                    startActivity(intent)
                    finish()
                }
                create()
                show()
            }
        }
    }

    @SuppressLint("Range")
    private fun isRegistered(inputAccount:String, inputPassword: String): Boolean {
        val dbHelper = MyDatabaseHelper(this, "user.db", MyApplication.DB_VERSION)
        val db = dbHelper.writableDatabase
        val cursor = db.query("User", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val account = cursor.getString(cursor.getColumnIndex("username"))
                val password = cursor.getString(cursor.getColumnIndex("password"))
                id = cursor.getInt(cursor.getColumnIndex("id"))
                Log.d("Login", "id = $id")
                if (account == inputAccount && password == inputPassword) return true
            } while (cursor.moveToNext())
        }
        cursor.close()
        return false
    }
}