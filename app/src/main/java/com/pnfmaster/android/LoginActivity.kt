package com.pnfmaster.android

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.pnfmaster.android.database.connect
import com.pnfmaster.android.database.connect.DBNAME
import com.pnfmaster.android.databinding.ActivityLoginBinding
import com.pnfmaster.android.newuser.NewuserActivity
import com.pnfmaster.android.utils.LoadingDialog
import com.pnfmaster.android.utils.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 测试通道
        binding.test.setOnClickListener {
            val intent = Intent(this, TestActivity::class.java)
            startActivity(intent)
        }

        // 切换语言
        binding.changeLanguage.setOnClickListener {
            val currentLocale = Locale.getDefault() // en
            val newLocale: Locale
            if (currentLocale.language == "en") {
                newLocale = Locale.SIMPLIFIED_CHINESE
                MyApplication.sharedPreferences.edit().putString("language", "cn").apply()
                Log.d("LoginActivity", "onCreate: Language is cn")
            } else {
                newLocale = Locale.ENGLISH
                MyApplication.sharedPreferences.edit().putString("language", "en").apply()
                Log.d("LoginActivity", "onCreate: Language is en")
            }

            Locale.setDefault(newLocale)
            val config = resources.configuration
            config.setLocale(newLocale)
            resources.updateConfiguration(config, resources.displayMetrics)

            recreate()
        }

        /* 如果数据库连接成功，则设置底部文字的背景为绿色；否则为红色 */
        val hdSetBgColor = Handler {
            when (isConnected) {
                1 -> binding.test.setBackgroundColor(Color.GREEN)
                0 -> binding.test.setBackgroundColor(Color.RED)
            }
            false
        }

        Thread {
            val msg = Message()
            try {
                connect.setConnection(DBNAME)
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            hdSetBgColor.sendMessage(msg) // 跳转到handler1
        }.start()

        // 启动logo和欢迎标语动画
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

            var registerFlag = false

            val job = Job()
            val scope = CoroutineScope(job)
            scope.launch {
                registerFlag = withContext(Dispatchers.IO) {
                    connect.isRegistered(account, psw)
                }
            }

            LoadingDialog(this).block(500)

            if (registerFlag) {
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
                MyApplication.isSkipped = false
                val intent = Intent(this, ControlActivity::class.java)
                intent.putExtra("userAccount", account)
                intent.putExtra("userId", MyApplication.userId)
                startActivity(intent)
                finish()
            } else {
                getString(R.string.wrong_name_psw).Toast() }
        }

        // 跳转至注册界面
        binding.newUserBtn.setOnClickListener {
            val intent = Intent(this, NewuserActivity::class.java)
            startActivity(intent)
        }

        binding.skipLoginEnter.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle(getString(R.string.Hint))
                setMessage(getString(R.string.Skip_hint))
                setNegativeButton(getString(R.string.BackToLogin), null)
                setPositiveButton(getString(R.string.UseDirectly)) { _, _ ->
                    MyApplication.isSkipped = true
                    val intent = Intent(this@LoginActivity, ControlActivity::class.java)
                    intent.putExtra("userId", -1)
                    startActivity(intent)
                    finish()
                }
                create()
                show()
            }
        }

        var doubleBackToExitPressedOnce = false
        onBackPressedDispatcher.addCallback(this) {
            if (doubleBackToExitPressedOnce) {
                finish() // 退出应用
            } else {
                doubleBackToExitPressedOnce = true
                Toast.makeText(this@LoginActivity,
                    getString(R.string.doubleBack), Toast.LENGTH_SHORT).show()

                // 如果两秒内没有再次点击返回键，则重置标志
                Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
            }
        }

    }
    companion object {
        var isConnected = -1
    }
}