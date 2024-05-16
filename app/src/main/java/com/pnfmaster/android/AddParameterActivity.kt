package com.pnfmaster.android

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.pnfmaster.android.MyApplication.Companion.userId
import com.pnfmaster.android.database.connect
import com.pnfmaster.android.databinding.ActivityAddParameterBinding
import com.pnfmaster.android.utils.CustomProgressDialog
import com.pnfmaster.android.utils.MyProgressDialog
import com.pnfmaster.android.utils.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.properties.Delegates

class AddParameterActivity : BaseActivity() {
    private lateinit var binding : ActivityAddParameterBinding
    private var paramsId by Delegates.notNull<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddParameterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = getString(R.string.edit_params)
        }

        paramsId = intent.getIntExtra("paramsId", -1)

        if (intent.getStringExtra("flag") == "EDIT") {
            val title = intent.getStringExtra("title")
            val ll = intent.getIntExtra("lowerlimit", -1).toString()
            val ul = intent.getIntExtra("upperlimit", -1).toString()
            val pos = intent.getIntExtra("position", -1).toString()
            val t = intent.getIntExtra("time", -1).toString()
            binding.parameterTitle.setText(title)
            binding.forceLowerLimit.setText(ll)
            binding.forceUpperLimit.setText(ul)
            binding.motorPosition.setText(pos)
            binding.trainingTime.setText(t)
        }

        binding.clear.setOnClickListener {
            binding.forceUpperLimit.setText("")
            binding.forceLowerLimit.setText("")
            binding.motorPosition.setText("")
            binding.trainingTime.setText("")
            "已清空".Toast()
        }

        // Generate parameters based on user's profile
        binding.generateParameters.setOnClickListener {
            // Step 1. Get user information from the database
            val user = UserBackground(this)
            user.init()
            // Wait until the user information is loaded
            while (user.isRunning) {
                Thread.sleep(1)
            }
            val myPd = CustomProgressDialog(this, getString(R.string.AI_is_thinking))
            myPd.show()

            // Step 2. Write the prompt and Send to the AI
            val prompt: String = if (Locale.getDefault().language == "en") {
                "1.This is my personal information：\n" +
                        "My age is ${user.age}. I am a ${user.gender}. My diagnosis info is：${user.diagnosisInfo}.  Currently, my treat plan is ${user.treatPlan} and my treat progress record is ${user.progressRecord}. My goals are ${user.goals}.\n" +
                        "2.Now, suppose you are an experienced and professional rehabilitation trainer. Please give me a set of parameters based on the info I gave to you.\n" +
                        "3.Your answer need to include four parameters:\"force lower limit,force upper limit,position,time\", where the units of \"force lower limit\" and \"force upper limit\" are Newton; the \"position\" should be an integer between 0 to 10000 with no unit; the unit of time is\"second\"\n" +
                        "4. Your answer should not exceed 200 words."
            } else {
                "1.这是我的个人信息：\n" +
                        "我的年龄是 ${user.age}。我的性别是 ${user.gender}。我的诊断信息是：${user.diagnosisInfo}。目前，我的治疗计划是 ${user.treatPlan}，我的治疗进度是 ${user.progressRecord}。我的目标是 ${user.goals}.\n" +
                        "2.现在，假设您是一位经验丰富的专业康复理疗师。请根据我上面提供的个人信息给我一组参数。\n" +
                        "3.这组参数包括：\"力的最小值，力的最大值，电机停止位置，时间\"，其中\"力的最小值\"与\"力的最大值\"的单位是牛顿；\"电机停止位置\"应该是介于0到10000之间的整数，无单位；时间参数的单位是\"秒\"\n" +
                        "4. 你的回答不要超过200字。"
            }

            lifecycleScope.launch {
                var answer: String
                withContext(Dispatchers.IO) {
                    try {
                        val ai = AIAssistant()
                        answer = ai.GetAnswer(prompt, "") as String
                        Log.d("AddParameterActivity", "Answer: $answer")
                    } catch (e: Exception) {
                        answer = "ERROR"
                        Log.e("AddParameterActivity", e.toString())
                    }
                }
                withContext(Dispatchers.Main) {
                    myPd.dismiss()
                    AlertDialog.Builder(this@AddParameterActivity)
                        .setTitle(getString(R.string.AI_suggestion))
                        .setMessage(answer)
                        .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                            // Extract parameters from the answer
                            val regex =
                                if (Locale.getDefault().language == "en") Regex("(?<=: )\\d+")
                                else Regex("(?<=：)\\d+")
                            val matches = regex.findAll(answer)
                            val threeParams = matches.map { it.value }.toList()

                            val regexSecond = Regex("\\d+(?=秒)")
                            val matchSecond = regexSecond.findAll(answer)
                            val secondParams = matchSecond.map { it.value }.toList()

                            val params = threeParams + secondParams

                            Log.d("AddParameterActivity", "params: $params")

                            try {
                                val lowerLimit = params[0]
                                val upperLimit = params[1]
                                val motorPosition = params[2]
                                val trainingTime = params[3]
                                // Update EditTexts
                                binding.parameterTitle.setText(getString(R.string.AI_suggestion))
                                binding.forceLowerLimit.setText(lowerLimit)
                                binding.forceUpperLimit.setText(upperLimit)
                                binding.motorPosition.setText(motorPosition)
                                binding.trainingTime.setText(trainingTime)

                                // Update chart data
                                val entries = ArrayList<Entry>()
                                entries.add(Entry(0f, lowerLimit.toFloat()))
                                entries.add(Entry(1f, upperLimit.toFloat()))

                                val dataSet = LineDataSet(entries, "Force")
                                val data = LineData(dataSet)
                                binding.chart.data = data
                                binding.chart.invalidate() // refresh chart
                            } catch (e: Exception) {
                                Log.d("AddParameterActivity", "params: $params")
                                Log.e("AddParameterActivity", e.toString())
                                getString(R.string.load_failed_retry_or_input_manually).Toast()
                            }
                        }
                        .setNegativeButton(getString(R.string.No)) { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }
        }

        binding.saveParameters.setOnClickListener {
             // Update chart data
            val lowerLimitString = binding.forceLowerLimit.text.toString()
            val upperLimitString = binding.forceUpperLimit.text.toString()
            val positionString = binding.motorPosition.text.toString()
            val timeString = binding.trainingTime.text.toString()

            if (lowerLimitString == "" || upperLimitString == "") {
                getString(R.string.please_input_params).Toast()
                return@setOnClickListener
            } else if (!isInt(lowerLimitString) || !isInt(upperLimitString)
                || !isInt(positionString) || !isInt(timeString)) {
                // Check if the inputs are all integer
                getString(R.string.please_input_valid_params).Toast()
                return@setOnClickListener
            }

            var title = binding.parameterTitle.text.toString()
            val lowerLimitFloat = lowerLimitString.toFloat()
            val upperLimitFloat = upperLimitString.toFloat()

            val lowerLimit = lowerLimitFloat.toInt()
            val upperLimit = upperLimitFloat.toInt()
            val position = positionString.toInt()
            val time = timeString.toInt()

            // Check if the parameters are valid
            if (lowerLimit >= upperLimit) {
                getString(R.string.lower_limit_cannot_be_larger_than_upper_limit).Toast()
                return@setOnClickListener
            }

            // Draw a figure
            val entries = ArrayList<Entry>()
            entries.add(Entry(0f, lowerLimitFloat)) // (0, lowerLimit)
            entries.add(Entry(1f, upperLimitFloat)) // (1, upperLimit)

            val dataSet = LineDataSet(entries, "Force")
            val data = LineData(dataSet)
            binding.chart.data = data
            binding.chart.invalidate() // refresh chart

            val pd = MyProgressDialog(this)
            pd.show()

            val flag = intent.getStringExtra("flag")
            if (flag == "EDIT") {
                if (title == "") {
                    Log.d("AddParameterActivity", "Title: $title")
                    title = getString(R.string.my_params)
                }
                // Update to database
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        connect.saveParams(title, lowerLimit, upperLimit, position, time, paramsId)
                    }
                    withContext(Dispatchers.Main) {
                        pd.dismiss()
                        getString(R.string.saved).Toast()
                    }
                }
                return@setOnClickListener
            } else {
                if (title == "") {
                    Log.d("AddParameterActivity", "Title: $title")
                    title = getString(R.string.my_params)
                }
                // Save to database
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        connect.insertParams(lowerLimit, upperLimit, position, time, title)
                    }
                    withContext(Dispatchers.Main) {
                        pd.dismiss()
                        getString(R.string.saved).Toast()
                    }
                }
                return@setOnClickListener
            }
        }
    }

    private fun isInt(string: String): Boolean {
        return string.toIntOrNull() != null
    }

    inner class UserBackground(private val context: Context) {
        private var infoList = listOf<Any>()
        private var rehabInfoList = listOf<String>()

        var isRunning = true

        lateinit var name : String
        var age = -1
        lateinit var gender : String
        lateinit var diagnosisInfo : String
        lateinit var treatPlan : String
        lateinit var progressRecord : String
        lateinit var goals : String

        fun init() {
            Log.d("AddParameterActivity", "init() started")
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    Log.d("AddParameterActivity", "Querying user information...")
                    infoList = connect.queryUserInfo(userId)
                    Log.d("AddParameterActivity", "User information queried.")

                    Log.d("AddParameterActivity", "Querying rehab information...")
                    rehabInfoList = connect.queryRehabInfo(userId)
                    Log.d("AddParameterActivity", "Rehab information queried.")
                    name = infoList[0] as String
                    age = infoList[1] as Int
                    gender = if (infoList[2] == 0) "Female" else "Male"
                    diagnosisInfo = assign(rehabInfoList[0])
                    treatPlan = assign(rehabInfoList[1])
                    progressRecord = assign(rehabInfoList[2])
                    goals = assign(rehabInfoList[3])

                    isRunning = false
                    Log.d("AddParameterActivity", "All Information loaded. Finish init()")
                }
            }
        }
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