package com.pnfmaster.android.newuser

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.pnfmaster.android.BaseActivity
import com.pnfmaster.android.R
import com.pnfmaster.android.databinding.ActivityUserInfoBinding
import com.pnfmaster.android.utils.ActivityCollector
import com.pnfmaster.android.utils.Toast

class UserInfoActivity : BaseActivity() {
    private lateinit var binding : ActivityUserInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityCollector.addActivity(this)

        setSupportActionBar(binding.toolbarNewUser)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = "新用户注册"
        }

        var gender = -1

        binding.genderRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.maleRadioButton -> {
                    gender = 1
                }
                R.id.femaleRadioButton -> {
                    gender = 0
                }
            }
        }

        binding.registerButton.setOnClickListener {
            val name = binding.name.text.toString()
            val age = binding.age.text.toString()
            val contact = binding.contact.text.toString()

            if (isLegal(name, gender, age, contact)) {
                // 上一个Activity传过来的参数
                val useraccount = intent.getStringExtra("account")
                val password = intent.getStringExtra("psw")

                val nextIntent = Intent(this, RehabInfoActivity::class.java)
                nextIntent.putExtra("useraccount", useraccount)
                nextIntent.putExtra("password", password)
                nextIntent.putExtra("name", name)
                nextIntent.putExtra("gender", gender)
                nextIntent.putExtra("age", age)
                nextIntent.putExtra("contact", contact)
                startActivity(nextIntent)
            }
        }

    }

    private fun isLegal(name: String, gender: Int, age: String, contact: String): Boolean {
        if (name == "" ) {
            "请填写您的姓名。".Toast()
            return false
        }
        if (gender == -1) {
            "请选择您的性别".Toast()
            return false
        }
        if (age == "") {
            "请填写您的年龄".Toast()
            return false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return true
    }
}