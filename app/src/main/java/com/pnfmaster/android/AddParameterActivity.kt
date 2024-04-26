package com.pnfmaster.android

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.pnfmaster.android.MyApplication.Companion.userId
import com.pnfmaster.android.database.connect
import com.pnfmaster.android.databinding.ActivityAddParameterBinding
import com.pnfmaster.android.utils.MyProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Locale

class AddParameterActivity : BaseActivity() {
    private lateinit var binding : ActivityAddParameterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddParameterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = getString(R.string.my_param)
        }

        binding.generateParameters.setOnClickListener {
            // Generate parameters based on user's profile
            // Step 1. Get user information from the database
            val user = UserBackground(this)
            user.init()
            // Step 2. Write the prompt and Send to the AI
            val prompt: String = if (Locale.getDefault().language == "en") {
                "1. This is my personal information:\n" +
                        "I am ${user.age()} years old. I am a ${user.gender()}. My diagnosis information is: ${user.diagnosisInfo()}. Currently, my treat plan is ${user.treatPlan()} and my progress is ${user.progressRecord()}. My goal is ${user.goals()}.\n" +
                        "2. Now, assume you are an experienced and professional rehabilitation physiotherapists. Please get me a set of parameters (force range, motor ending position and training time)based on my personal information given you above.\n" +
                        "3. Your answer must strictly be the following format: intArrayOf(force lower limit, force upper limit, position, time). Every element in the array should be an interger. The \"position\" parameter should be an interger between 0 and 10000. The unit of \"time parameter is \"second\".\n" +
                        "4. Followings are some examples:\n" +
                        "User Ask 1: Now, based on my information, please recommend me a set of parameters.\n" +
                        "Your Answer 1: intArrayOf(0, 90, 8000, 20)\n" +
                        "User Ask2: Now, based on my information, please recommend me a set of parameters.\n" +
                        "Your Answer 1: intArrayOf(50, 50, 10000, 30)\n" +
                        "5. Now, based on my information, please recommend me a set of parameters. Reply me in English."
            } else {
                "1.这是我的个人信息：\n" +
                "我的年龄是 ${user.age()}。我的性别是 ${user.gender()}。我的诊断信息是：${user.diagnosisInfo()}。目前，我的治疗计划是 ${user.treatPlan()}，我的治疗进度是 ${user.progressRecord()}。我的目标是 ${user.goals()}.\n" +
                "2.现在，假设您是一位经验丰富的专业康复理疗师。请根据我上面提供的个人信息给我一组参数（用力范围、运动结束位置和训练时间）。\n" +
                "3.你的答案必须严格遵守以下格式：\"force lower limit,force upper limit,position,time\"，并且不包含任何其他文字。数组中的每个元素都应该是一个整数。\"position\"参数应该是介于0到10000之间的整数。时间参数的单位是\"秒\"" +
                "你的回答中只能包含四个数字，不要包含任何其他文字。"
            }

            val pd = MyProgressDialog(this)
            pd.show()

            var answer = ""
            CoroutineScope(Dispatchers.IO).launch{
                try {
                    val ai = AIAssistant()
                    answer = ai.GetAnswer(prompt, "") as String
                    Log.d("AddParameterActivity", "Answer: $answer")
                } catch (e: Exception) {
                    answer = "ERROR"
                    Log.e("AddParameterActivity", e.toString())
                }

                // 切回主线程才能显示Dialog
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@AddParameterActivity)
                        .setTitle("PNF Master的建议")
                        .setMessage(answer)
                        .setPositiveButton(getString(R.string.Yes)) { dialog, _ -> dialog.dismiss() }
                        .show()
                }

//                    val params = answer.split(",")
//                    val lowerLimit = params[0]
//                    val upperLimit = params[1]
//                    val motorPosition = params[2]
//                    val trainingTime = params[3]
//                    // Update EditTexts
//                    binding.forceUpperLimit.setText(upperLimit)
//                    binding.forceLowerLimit.setText(lowerLimit)
//                    binding.motorPosition.setText(motorPosition)
//                    binding.trainingTime.setText(trainingTime)
//
//                    // Update chart data
//                    val entries = ArrayList<Entry>()
//                    entries.add(Entry(0f, lowerLimit.toFloat()))
//                    entries.add(Entry(1f, upperLimit.toFloat()))
//
//                    val dataSet = LineDataSet(entries, "Force")
//                    val data = LineData(dataSet)
//                    binding.chart.data = data
//                    binding.chart.invalidate() // refresh chart
            }
            pd.dismiss()
        }
    }

    class UserBackground(private val context: Context) {
        private var infoList = listOf<Any>()
        private var rehabInfoList = listOf<String>()
        fun init() {
            runBlocking {
                launch {
                    withContext(Dispatchers.IO) {
                        infoList = connect.queryUserInfo(userId)
                        rehabInfoList = connect.queryRehabInfo(userId)
                    }
                }
            }
        }
        fun name(): String { return infoList[0] as String }
        fun age(): Int { return infoList[1] as Int }
        fun gender(): String { return if (infoList[2] == 0) "Female" else "Male" }
        fun diagnosisInfo(): String { return assign(rehabInfoList[0]) }
        fun treatPlan(): String { return assign(rehabInfoList[1]) }
        fun progressRecord(): String { return assign(rehabInfoList[2]) }
        fun goals(): String { return assign(rehabInfoList[3]) }
        private fun assign(string: String):String {
            return if (string != context.getString(R.string.not_filled_yet) && string != "") string
            else "无"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return true
    }

}