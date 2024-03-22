package com.pnfmaster.android.newuser

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.pnfmaster.android.BaseActivity
import com.pnfmaster.android.LoginActivity
import com.pnfmaster.android.MyApplication
import com.pnfmaster.android.database.MyDatabaseHelper
import com.pnfmaster.android.databinding.ActivityRehabInfoBinding
import com.pnfmaster.android.utils.ActivityCollector

class RehabInfoActivity : BaseActivity() {
    private lateinit var binding: ActivityRehabInfoBinding
    private lateinit var dbHelper: MyDatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRehabInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityCollector.addActivity(this)

        setSupportActionBar(binding.toolbarNewUser)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = "新用户注册"
        }

        dbHelper = MyDatabaseHelper(this, "user.db", MyApplication.DB_VERSION)

        binding.registerButton.setOnClickListener {
            // 所有准备录入数据库的个人资料
            val account = intent.getStringExtra("useraccount")
            val password = intent.getStringExtra("password")
            val name = intent.getStringExtra("name")
            val gender = intent.getIntExtra("gender", -1)
            val age = intent.getStringExtra("age")?.toInt()
            val contact = intent.getStringExtra("contact")
            val diagnosisInfo = binding.diagnosisInfo.text.toString()
            val plan = binding.treatmentPlan.text.toString()
            val progressRecord = binding.progressRecord.text.toString()
            val goals = binding.goals.text.toString()

            insertUser(account, password)
            insertUserInfo(name, gender, age, contact)
            insertRehabInfo(diagnosisInfo, plan, progressRecord, goals)

            val NEWUSER = 1
            val nextIntent = Intent(this, LoginActivity::class.java)
            nextIntent.putExtra("newAccount", account)
            nextIntent.putExtra("newPassword", password)
            nextIntent.putExtra("newUserFlag", NEWUSER)
            startActivity(nextIntent)

            // 关闭全部注册activity
            ActivityCollector.finishAll()

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

    private fun insertUser(username: String?, password: String?) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("password", password)
        }
        db.insert("User", null, values)
    }

    private fun insertUserInfo(name: String?, gender: Int?, age: Int?, contact: String?) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("gender", gender)
            put("age", age)
            put("phone", contact)
        }
        db.insert("UserInfo", null, values)
    }

    private fun insertRehabInfo(diagnosisInfo: String?, plan: String?, progressRecord: String?, goals: String?) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("diagnosisInfo", diagnosisInfo)
            put("treatPlan", plan)
            put("progressRecord", progressRecord)
            put("goals", goals)
        }
        db.insert("RehabInfo", null, values)
    }

}