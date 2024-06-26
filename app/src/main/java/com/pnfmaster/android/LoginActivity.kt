package com.pnfmaster.android

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
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
import androidx.lifecycle.lifecycleScope
import com.pnfmaster.android.MyApplication.Companion.sharedPreferences
import com.pnfmaster.android.database.MyDatabaseHelper
import com.pnfmaster.android.database.connect
import com.pnfmaster.android.database.connect.DBNAME
import com.pnfmaster.android.databinding.ActivityLoginBinding
import com.pnfmaster.android.newuser.NewuserActivity
import com.pnfmaster.android.utils.MyProgressDialog
import com.pnfmaster.android.utils.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.util.Locale

class LoginActivity : BaseActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var pd : MyProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create progressDialog Instance
        pd = MyProgressDialog(this)

        // Get the current language from sharedPreferences
        val language = sharedPreferences.getString("language", "en") ?: "en"
        val currentLocale = if (language == "en") Locale.ENGLISH else Locale.CHINESE

        // Test tunnel
        binding.test.setOnClickListener {
            val intent = Intent(this, TestActivity::class.java)
            startActivity(intent)
        }

        // 切换语言
        binding.changeLanguage.setOnClickListener {
            val newLocale: Locale
            if (currentLocale.language == "en") {
                newLocale = Locale.CHINESE
                sharedPreferences.edit().putString("language", "cn").apply()
            } else {
                newLocale = Locale.ENGLISH
                sharedPreferences.edit().putString("language", "en").apply()
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

        /**
         * 先判断是否是从注册界面来的新用户，如果是的话就把他刚注册的用户名和密码填上去
         * 如果不是新用户，就启动记住密码功能。
         */
        val flag = intent.getIntExtra("newUserFlag", 0) // 是刚注册的新用户
        val prefs = getPreferences(Context.MODE_PRIVATE)
        var isRemember = prefs.getBoolean("rememberPsw", false)

        if (flag == 1) {
            val newAccount = intent.getStringExtra("newAccount")
            val newPassword = intent.getStringExtra("newPassword")
            binding.accountEdit.setText(newAccount)
            binding.pswEdit.setText(newPassword)
        } else {
            if (isRemember) {
                val account = prefs.getString("account", "")
                val password = prefs.getString("password", "")
                binding.accountEdit.setText(account)
                binding.pswEdit.setText(password)
                binding.rememberPsw.isChecked = true
            }
        }

        // Login
        binding.loginInBtn.setOnClickListener {
            val account = binding.accountEdit.text.toString()
            val psw = binding.pswEdit.text.toString()

            isRemember = if (binding.rememberPsw.isChecked) {
                prefs.edit().putBoolean("rememberPsw", true).apply()
                true
            } else {
                prefs.edit().putBoolean("rememberPsw", false).apply()
                false
            }

            // Show ProgressDialog
            pd.show()

            /**
             * 原来的设想是先在本地数据库中搜索，加快登录速度。但是因为以下原因还是舍弃了这个特性：
             * 1. 首先最大的问题是本地数据库中的userId和云端是不一样的，ProfileActivity目前使用的还是云端数据库
             * 2. 本地数据库中的数据是不会更新的，如果用户在云端修改了个人资料，本地数据库中的数据不会更新
             * 3. 这个特性本身的意义也不是很大，因为登录的时候也不会有太大的延迟并且登录后使用的功能还是需要联网。
             *
             * 舍弃之后的代码中，isLocal是永远为false的，所以不会执行本地数据库的搜索。如果需要重新启用这个特性，
             * 只需将下列代码取消注释，并将注释下方的两行代码删除即可。
             * 恢复特性需要：
             * 1. 在ProfileActivity中实现一遍本地数据库读取；
             * 2. 此时，用户更新之后的个人资料不光要保存在本地，还需要同时保存在云端；同时还需要每次记得更新云端数据到本地
             * 3. 为了实现以上的操作，势必要同时使用本地userId和云端userId。很容易混乱。
             */
            // First Search in Local Database
//            var registerFlag = isRegistered(account, psw)

//            var isLocal = false
//            if (registerFlag) {
//                isLocal = true
//            }
//            Log.d("LoginActivity", "LocalDB  isLocal: $isLocal")
            var registerFlag: Boolean
            val isLocal = false

            // If not detected in local db, then search in Online Database
            if (!isLocal) {
                lifecycleScope.launch {
                    // Search in the IO thread
                    registerFlag = withContext(Dispatchers.IO) {
                        connect.isRegistered(account, psw)
                    }

                    // Update UI in the main thread
                    withContext(Dispatchers.Main) {
                        Log.d("LoginActivity", "withContext(Dispatchers.Main)")
                        Log.d("LoginActivity", "isRemember: $isRemember")
                        // 如果已注册并且用户信任该设备(勾选了记住密码),则存储至本地数据库
//                        if (registerFlag && isRemember) {
//                            // 1 存储账户信息
//                            fun insertUser(username: String, password: String) {
//                                val db = MyDatabaseHelper(
//                                    this@LoginActivity,
//                                    "user.db",
//                                    MyApplication.DB_VERSION
//                                ).writableDatabase
//                                val values = ContentValues().apply {
//                                    put("username", username)
//                                    put("password", password)
//                                }
//                                db.insert("User", null, values)
//                            }
//                            insertUser(account, psw)
//
//                            lifecycleScope.launch {
//                                Log.d(
//                                    "LoginActivity",
//                                    "----------- lifecycleScope started. -----------"
//                                )
//                                // 2 个人资料
//                                withContext(Dispatchers.IO) personInfo@ {
//                                    val infoList = connect.queryUserInfo(MyApplication.userId)
//                                    if (isConnected == -1) {
//                                        "2:连接错误,请重试.".Toast(Toast.LENGTH_LONG)
//                                        return@personInfo
//                                    }
//                                    val name = infoList[0] as String
//                                    val age = infoList[1] as Int
//                                    val gender = infoList[2] as Int
//                                    val phone = infoList[3] as String
//                                    fun insertUserInfo(
//                                        name: String?,
//                                        gender: Int?,
//                                        age: Int?,
//                                        contact: String?
//                                    ) {
//                                        val db = MyDatabaseHelper(
//                                            this@LoginActivity,
//                                            "user.db",
//                                            MyApplication.DB_VERSION
//                                        ).writableDatabase
//                                        val values = ContentValues().apply {
//                                            put("name", name)
//                                            put("gender", gender)
//                                            put("age", age)
//                                            put("phone", contact)
//                                        }
//                                        db.insert("UserInfo", null, values)
//                                    }
//                                    insertUserInfo(name, age, gender, phone)
//                                }
//
//                                // 3 康复资料
//                                withContext(Dispatchers.IO) rehabInfo@ {
//                                    val rehabInfoList = connect.queryRehabInfo(MyApplication.userId)
//                                    if (isConnected == -1) {
//                                        "3:连接错误,请重试.".Toast(Toast.LENGTH_LONG)
//                                        return@rehabInfo
//                                    }
//                                    fun assign(string: String): String {
//                                        return if (string != getString(R.string.not_filled_yet) && string != "") string
//                                        else getString(R.string.not_filled_yet)
//                                    }
//
//                                    val diagnosisInfo = assign(rehabInfoList[0])
//                                    val treatPlan = assign(rehabInfoList[1])
//                                    val progressRecord = assign(rehabInfoList[2])
//                                    val goals = assign(rehabInfoList[3])
//                                    fun insertRehabInfo(
//                                        diagnosisInfo: String?,
//                                        plan: String?,
//                                        progressRecord: String?,
//                                        goals: String?
//                                    ) {
//                                        val db = MyDatabaseHelper(
//                                            this@LoginActivity,
//                                            "user.db",
//                                            MyApplication.DB_VERSION
//                                        ).writableDatabase
//                                        val values = ContentValues().apply {
//                                            put("diagnosisInfo", diagnosisInfo)
//                                            put("treatPlan", plan)
//                                            put("progressRecord", progressRecord)
//                                            put("goals", goals)
//                                        }
//                                        db.insert("RehabInfo", null, values)
//                                    }
//                                    insertRehabInfo(diagnosisInfo, treatPlan, progressRecord, goals)
//                                    Log.d(
//                                        "LoginActivity",
//                                        "----------- lifecycleScope ended. -----------"
//                                    )
//                                }
//                                withContext(Dispatchers.Main) {
//                                    pd.dismiss()
//                                }
//                            }
//                        } else
                        if (registerFlag) {
                            if (flag == 0) {
                                // 如果勾选了“记住密码”选项
                                val editor = prefs.edit()
                                if (binding.rememberPsw.isChecked) {
                                    editor.putBoolean("rememberPsw", true)
                                    editor.putString("account", account)
                                    editor.putString("password", psw)
                                } else {
                                    editor.clear()
                                }
                                editor.apply()
                            }
                            MyApplication.isSkipped = false
                            val intent = Intent(this@LoginActivity, ControlActivity::class.java)
                            intent.putExtra("userAccount", account)
                            intent.putExtra("userId", MyApplication.userId)
                            startActivity(intent)
                            finish()
                        } else {
                            pd.dismiss()
                            getString(R.string.wrong_name_psw).Toast()
                        }
                    }
                    Log.d("LoginActivity", "OnlineDB registerFlag: $registerFlag")
                }
            } else {
                pd.dismiss()
                if (flag == 0) {
                    // 如果勾选了“记住密码”选项
                    val editor = prefs.edit()
                    if (binding.rememberPsw.isChecked) {
                        editor.putBoolean("rememberPsw", true)
                        editor.putString("account", account)
                        editor.putString("password", psw)
                    } else {
                        editor.clear()
                    }
                    editor.apply()
                }
                MyApplication.isSkipped = false
                val intent = Intent(this@LoginActivity, ControlActivity::class.java)
                intent.putExtra("userAccount", account)
                intent.putExtra("userId", MyApplication.userId)
                startActivity(intent)
                finish()
            }
        }

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

        // Double Slide to Exit
        var doubleBackToExitPressedOnce = false
        onBackPressedDispatcher.addCallback(this) {
            pd.dismiss()
            Log.d("LoginActivity", "onBackPressed")
            if (doubleBackToExitPressedOnce) {
                finish()
            } else {
                doubleBackToExitPressedOnce = true
                Toast.makeText(this@LoginActivity,
                    getString(R.string.doubleBack), Toast.LENGTH_SHORT).show()

                // 如果两秒内没有再次点击返回键，则重置标志
                Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        pd.dismiss()
    }

    /**
     * 该方法在本地数据库中检索用户是否已注册
     *
     * @param inputAccount 用户输入的账号
     * @param inputPassword 用户输入的密码
     * @return 是否已注册
     */
    @SuppressLint("Range")
    private fun isRegistered(inputAccount:String, inputPassword: String): Boolean {
        val dbHelper = MyDatabaseHelper(this, "user.db", MyApplication.DB_VERSION)
        val db = dbHelper.writableDatabase
        val cursor = db.query("User", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val account = cursor.getString(cursor.getColumnIndex("username"))
                val password = cursor.getString(cursor.getColumnIndex("password"))
                MyApplication.userId = cursor.getInt(cursor.getColumnIndex("id"))
                if (account == inputAccount && password == inputPassword) return true
            } while (cursor.moveToNext())
        }
        cursor.close()
        return false
    }

    companion object {
        var isConnected = -1
    }
}