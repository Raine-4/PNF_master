package com.pnfmaster.android

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.pnfmaster.android.database.MyDatabaseHelper
import com.pnfmaster.android.databinding.ActivityLoginBinding
import com.pnfmaster.android.newuser.NewuserActivity
import com.pnfmaster.android.utils.Toast

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Logo的动画
        val logoImageView = findViewById<ImageView>(R.id.logo)
        // 加载动画资源
        val logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        // 设置动画监听器
        logoAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // 动画开始时的处理
            }
            override fun onAnimationEnd(animation: Animation?) {
                // 动画结束时的处理，停止动画
                logoImageView.clearAnimation()
            }
            override fun onAnimationRepeat(animation: Animation?) {
                // 动画重复时的处理
            }
        })
        // 开始Logo旋转动画
        logoImageView.startAnimation(logoAnimation)

        binding.appName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))


        /* 先判断是否是从注册界面来的新用户，如果是的话就把他刚注册的用户名和密码给他填上去
         * 如果不是新用户，就启动记住密码功能。
         */
        val flag = intent.getIntExtra("newUserFlag", 0)
        if (flag == 1) {
            val newAccount = intent.getStringExtra("newAccount")
            val newPassword = intent.getStringExtra("newPassword")
            binding.accountEdit.text = Editable.Factory.getInstance().newEditable(newAccount)
            binding.pswEdit.text = Editable.Factory.getInstance().newEditable(newPassword)
        } else {
            // 记住密码功能1
            val prefs = getPreferences(Context.MODE_PRIVATE)
            val isRemember = prefs.getBoolean("remember_psw", false)
            if (isRemember) {
                val account = prefs.getString("account", "")
                val psw = prefs.getString("password", "")
                binding.accountEdit.setText(account)
                binding.pswEdit.setText(psw)
                binding.rememberPsw.isChecked = true
            }
        }

        // 登录
        binding.loginInBtn.setOnClickListener {
            val account = binding.accountEdit.text.toString()
            val psw = binding.pswEdit.text.toString()

            if (isRegistered(account, psw)) {

                if (flag == 0) {
                    // 记住密码
                    val prefs = getPreferences(Context.MODE_PRIVATE)
                    val editor = prefs.edit()
                    if (binding.rememberPsw.isChecked) {
                        editor.putBoolean("remember_psw", true)
                        editor.putString("account", account)
                        editor.putString("password", psw)
                    } else {
                        editor.clear()
                    }
                    editor.apply()
                }

                val intent = Intent(this, ControlActivity::class.java)
                intent.putExtra("userAccount", account)
                startActivity(intent)
                finish()
            } else {
                "用户名或密码错误".Toast()
            }
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
                setPositiveButton("返回登录") { _, _ -> }
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

    private fun isRegistered(account:String, sw: String): Boolean {
        val dbHelper = MyDatabaseHelper(this, "user.db", MyApplication.DB_VERSION)

        return true
    }

}