package com.pnfmaster.android

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.pnfmaster.android.database.MyDatabaseHelper
import com.pnfmaster.android.databinding.ActivityProfileBinding
import com.pnfmaster.android.utils.Toast
import kotlin.properties.Delegates

class ProfileActivity : BaseActivity() {
    private lateinit var binding : ActivityProfileBinding
    private var userId by Delegates.notNull<Int>()

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarProfile)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(R.string.Profile)
        }

        // 用id查询数据库中的用户基本信息
        val dbHelper = MyDatabaseHelper(this, "user.db", MyApplication.DB_VERSION)
        val db = dbHelper.writableDatabase

        var name = ""
        var age = ""
        var gender = -1
        var phone = ""

        val cursor = db.query("UserInfo", null, null, null, null, null, null)
        userId = intent.getIntExtra("userId", -1)
        val userAccount = intent.getStringExtra("userAccount")
        if (cursor.moveToFirst()) {
            do {
                binding.userName.text = userAccount
                val id = cursor.getInt(cursor.getColumnIndex("id"))
                name = cursor.getString(cursor.getColumnIndex("name"))
                age = cursor.getString(cursor.getColumnIndex("age"))
                gender = cursor.getInt(cursor.getColumnIndex("gender"))
                phone = cursor.getString(cursor.getColumnIndex("phone"))
                if (id == userId) {
                    binding.USERNAME.text = name
                    binding.AGE.text = age
                    binding.GENDER.text = if (gender == 1) "男" else "女"
                    binding.CONTACT.text = phone
                }
            } while (cursor.moveToNext())
        }
        cursor.close()

        binding.personInfo.setOnClickListener {
            // 使用LayoutInflater来加载自定义布局
            val dialogView = layoutInflater.inflate(R.layout.custom_dialog, null)
            val nameEditText = dialogView.findViewById<EditText>(R.id.name_edit_text)
            val ageEditText = dialogView.findViewById<EditText>(R.id.age_edit_text)
            val genderRadioGroup = dialogView.findViewById<RadioGroup>(R.id.gender_radio_group)
            val maleButton = dialogView.findViewById<RadioButton>(R.id.radio_button_male)
            val femaleButton = dialogView.findViewById<RadioButton>(R.id.radio_button_female)
            val contactEditText = dialogView.findViewById<EditText>(R.id.contact_edit_text)

            // 显示原数据
            nameEditText.setText(name)
            ageEditText.setText(age)
            contactEditText.setText(phone)
            if (gender == 1) maleButton.isChecked = true else femaleButton.isChecked = true

            // 创建AlertDialog.Builder对象
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle("修改个人信息")
            dialogBuilder.setView(dialogView)

            // 设置“取消”按钮
            dialogBuilder.setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }

            // 设置“保存”按钮
            dialogBuilder.setPositiveButton("保存") { dialog, _ ->
                // 获取用户输入的数据
                val inputName = nameEditText.text.toString()
                val inputAge = ageEditText.text.toString()
                val genderId = genderRadioGroup.checkedRadioButtonId
                val inputGender = if (genderId == R.id.radio_button_male) 1 else 0
                val inputContact = contactEditText.text.toString()
                savePersonInfo(inputName, inputAge, inputGender, inputContact)
                dialog.dismiss()
            }
            val dialog = dialogBuilder.create()
            dialog.show()
        }

        var diagnosisInfo = "尚未填写"
        var treatPlan = "尚未填写"
        var progressRecord = "尚未填写"
        var goals = "尚未填写"
        
        // 查询健康信息
        val cursor2 = db.query("RehabInfo", null, null, null, null, null, null)
        if (cursor2.moveToFirst()) {
            do {
                binding.userName.text = userAccount
                val id = cursor2.getInt(cursor2.getColumnIndex("id"))

                val curDiagnosisInfo = cursor2.getString(cursor2.getColumnIndex("diagnosisInfo"))
                val curTreatPlan = cursor2.getString(cursor2.getColumnIndex("treatPlan"))
                val curProgressRecord = cursor2.getString(cursor2.getColumnIndex("progressRecord"))
                val curGoals = cursor2.getString(cursor2.getColumnIndex("goals"))

                if (id == userId) {
                    diagnosisInfo = if (curDiagnosisInfo != "尚未填写" && curDiagnosisInfo != "") curDiagnosisInfo else "尚未填写"
                    treatPlan = if (curTreatPlan!="尚未填写" && curTreatPlan != "") curTreatPlan else "尚未填写"
                    progressRecord = if (curProgressRecord!="尚未填写" && curProgressRecord != "") curProgressRecord else "尚未填写"
                    goals = if (curGoals!="尚未填写" && curGoals != "") curGoals else "尚未填写"
                }
            } while (cursor2.moveToNext())
        }
        cursor2.close()

        binding.diagnosisInfo.setOnClickListener {
            popUpDialog("诊断信息", diagnosisInfo, "diagnosisInfo")
        }

        binding.plan.setOnClickListener {
            popUpDialog("治疗方案", treatPlan, "treatPlan")
        }

        binding.progress.setOnClickListener {
            popUpDialog("进展记录", progressRecord, "progressRecord")
        }

        binding.goals.setOnClickListener {
            popUpDialog("我的目标", goals, "goals")
        }
    }

    private fun savePersonInfo(name: String, age: String, gender: Int, contact: String) {
        val dbHelper = MyDatabaseHelper(this, "user.db", MyApplication.DB_VERSION)
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        values.put("name", name)
        values.put("age", age)
        values.put("gender", gender)
        values.put("phone", contact)
        try {
            db.update("UserInfo", values, "id = ?", arrayOf(userId.toString()))
            "修改成功".Toast()
            this.recreate() // 刷新页面
        } catch (e: Exception) {
            Log.e("Profile", e.toString())
            "修改失败".Toast()
        }
    }

    private fun popUpDialog(title: String, msg: String, columnName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        // 设置“确定”按钮，点击后关闭对话框
        builder.setPositiveButton("确定") { dialog, _ ->
            dialog.dismiss()
        }
        // 设置“编辑”按钮，点击后替换内容为EditText
        builder.setNegativeButton("编辑") { dialog, _ ->
            val dialogView = layoutInflater.inflate(R.layout.rehab_edit_dialog, null)
            val editText = dialogView.findViewById<EditText>(R.id.myEditText)
            if (msg == "尚未填写" || msg == "") {
                editText.hint = when (title) {
                    "诊断信息" -> getString(R.string.rehabHint1)
                    "治疗方案" -> getString(R.string.rehabHint2)
                    "进展记录" -> getString(R.string.rehabHint3)
                    "我的目标" -> getString(R.string.rehabHint4)
                    else -> ""
                }
            } else {
                editText.setText(msg)
            }
            // 创建一个新的AlertDialog.Builder来替换原来的对话框
            val newBuilder = AlertDialog.Builder(this).apply{
                setTitle("编辑$title")
                setView(dialogView)
            }
            // 设置“保存”按钮，点击后保存内容并关闭对话框
            newBuilder.setPositiveButton("保存") { newDialog, _ ->
                val userInput = editText.text.toString()
                val dbHelper = MyDatabaseHelper(this, "user.db", MyApplication.DB_VERSION)
                val db = dbHelper.writableDatabase
                val values = ContentValues()
                values.put(columnName, userInput)
                try {
                    db.update("RehabInfo", values, "id = ?", arrayOf(userId.toString()))
                    "修改成功".Toast()
                    this.recreate() // 刷新页面
                } catch (e: Exception) {
                    "修改失败".Toast()
                }
                newDialog.dismiss()
            }
            // 设置“取消”按钮，点击后不保存修改，直接关闭对话框
            newBuilder.setNegativeButton("取消") { newDialog, _ ->
                newDialog.dismiss()
            }
            newBuilder.show()
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return true
    }
}