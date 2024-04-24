package com.pnfmaster.android

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.pnfmaster.android.database.connect
import com.pnfmaster.android.databinding.ActivityProfileBinding
import com.pnfmaster.android.utils.MyProgressDialog
import com.pnfmaster.android.utils.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ProfileActivity : BaseActivity() {

    private lateinit var binding : ActivityProfileBinding
    private val job = Job()
    private val scope = CoroutineScope(job)

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

        val userId = MyApplication.userId

        var infoList = listOf<Any>()

        val pd = MyProgressDialog(this)
        pd.show()
        runBlocking {
            launch {
                infoList = withContext(Dispatchers.IO) {
                    connect.queryUserInfo(userId)
                }
            }
        }
        pd.dismiss()

        val username = intent.getStringExtra("userAccount") //用户名
        val name = infoList[0] as String
        val age = infoList[1] as Int
        val gender = infoList[2] as Int
        val phone = infoList[3] as String

        binding.userName.text = username
        binding.USERNAME.text = name
        binding.AGE.text = age.toString()
        binding.GENDER.text = if (gender == 1) getString(R.string.male) else getString(R.string.female)
        binding.CONTACT.text = if (phone != "") phone else getString(R.string.not_filled_yet)

        binding.personInfo.setOnClickListener {
            // 使用LayoutInflater来加载自定义布局
            val dialogView = layoutInflater.inflate(R.layout.edit_userinfo_dialog, null)
            val nameEditText = dialogView.findViewById<EditText>(R.id.name_edit_text)
            val ageEditText = dialogView.findViewById<EditText>(R.id.age_edit_text)
            val genderRadioGroup = dialogView.findViewById<RadioGroup>(R.id.gender_radio_group)
            val maleButton = dialogView.findViewById<RadioButton>(R.id.radio_button_male)
            val femaleButton = dialogView.findViewById<RadioButton>(R.id.radio_button_female)
            val contactEditText = dialogView.findViewById<EditText>(R.id.contact_edit_text)

            // 显示原数据
            nameEditText.setText(name)
            ageEditText.setText(age.toString())
            contactEditText.setText(phone)
            if (gender == 1) maleButton.isChecked = true else femaleButton.isChecked = true

            // 创建AlertDialog.Builder对象
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle(getString(R.string.editUserInfo))
            dialogBuilder.setView(dialogView)

            // 设置“取消”按钮
            dialogBuilder.setNegativeButton(getString(R.string.No)) { dialog, _ ->
                dialog.dismiss()
            }

            // 设置“保存”按钮
            dialogBuilder.setPositiveButton(getString(R.string.save)) { dialog, _ ->
                // 获取用户输入的数据
                val inputName = nameEditText.text.toString()
                val inputAge = ageEditText.text.toString()
                val genderId = genderRadioGroup.checkedRadioButtonId
                val inputGender = if (genderId == R.id.radio_button_male) 1 else 0
                val inputContact = contactEditText.text.toString()

                val pd1 = MyProgressDialog(this)
                pd1.show()
                try {
                    runBlocking {
                        launch {
                            withContext(Dispatchers.IO) {
                                connect.savePersonInfo(inputName, inputAge, inputGender, inputContact)
                            }
                        }
                    }
                    Thread.sleep(500)
                    getString(R.string.edit_success).Toast()
                    this.recreate()
                } catch (e: Exception) {
                    getString(R.string.edit_fail).Toast()
                    Log.e("profile", "dialogBuilder.setPositiveButton")
                }
                // ----------------------------------
                pd1.dismiss()
                dialog.dismiss()
            }

            val dialog = dialogBuilder.create()
            dialog.show()
        }


        // 查询健康信息
        var rehabInfoList = listOf<String>()
        // ----------------------------------
        val pd2 = MyProgressDialog(this)
        pd2.show()
        runBlocking {
            launch {
                withContext(Dispatchers.IO) {
                    rehabInfoList = connect.queryRehabInfo(userId)
                }
            }
        }
        pd2.dismiss()
        // ----------------------------------

        val diagnosisInfo = assign(rehabInfoList[0])
        val treatPlan = assign(rehabInfoList[1])
        val progressRecord = assign(rehabInfoList[2])
        val goals = assign(rehabInfoList[3])

        binding.diagnosisInfo.setOnClickListener {
            popUpDialog(getString(R.string.diagnosisinfo), diagnosisInfo, "diagnosisinfo")
        }

        binding.plan.setOnClickListener {
            popUpDialog(getString(R.string.treatplan), treatPlan, "treatplan")
        }

        binding.progress.setOnClickListener {
            popUpDialog(getString(R.string.progressrecord), progressRecord, "progressrecord")
        }

        binding.goals.setOnClickListener {
            popUpDialog(getString(R.string.goals), goals, "goals")
        }
    }

    @SuppressLint("InflateParams")
    private fun popUpDialog(title: String, msg: String, columnName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        // 设置“确定”按钮，点击后关闭对话框
        builder.setPositiveButton(getString(R.string.Yes)) { dialog, _ ->
            dialog.dismiss()
        }
        // 设置“编辑”按钮，点击后替换内容为EditText
        builder.setNegativeButton(getString(R.string.edit)) { dialog, _ ->
            val dialogView = layoutInflater.inflate(R.layout.rehab_edit_dialog, null)
            val editText = dialogView.findViewById<EditText>(R.id.myEditText)
            if (msg == getString(R.string.not_filled_yet) || msg == "") {
                editText.hint = when (title) {
                    getString(R.string.diagnosisinfo) -> getString(R.string.rehabHint1)
                    getString(R.string.treatplan) -> getString(R.string.rehabHint2)
                    getString(R.string.progressrecord) -> getString(R.string.rehabHint3)
                    getString(R.string.goals) -> getString(R.string.rehabHint4)
                    else -> getString(R.string.EnterHere)
                }
            } else {
                editText.setText(msg)
            }

            // 创建一个新的AlertDialog.Builder来替换原来的对话框
            val newBuilder = AlertDialog.Builder(this).apply{
                setTitle(getString(R.string.edit) + title)
                setView(dialogView)
            }
            // 设置“保存”按钮，点击后保存内容并关闭对话框
            newBuilder.setPositiveButton(getString(R.string.save)) { newDialog, _ ->
                val userInput = editText.text.toString()

                val pd3 = MyProgressDialog(this)
                pd3.show()
                try {
                    runBlocking {
                        launch {
                            withContext(Dispatchers.IO) {
                                connect.saveRehabInfo(userInput, columnName, MyApplication.userId)
                            }
                        }
                    }
                    getString(R.string.edit_success).Toast()
                    this.recreate() // 刷新页面
                } catch (e: Exception) {
                    getString(R.string.edit_fail).Toast()
                    Log.e("profile", "private fun popUpDialog")
                }
                // 关闭ProgressDialog
                pd3.dismiss()

                newDialog.dismiss()
            }
            // 设置“取消”按钮，点击后不保存修改，直接关闭对话框
            newBuilder.setNegativeButton(getString(R.string.No)) { newDialog, _ ->
                newDialog.dismiss()
            }
            newBuilder.show()
            dialog.dismiss()
        }
        builder.show()
    }

    private fun assign(string: String):String {
        return if (string != getString(R.string.not_filled_yet) && string != "") string
        else getString(R.string.not_filled_yet)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return true
    }
}