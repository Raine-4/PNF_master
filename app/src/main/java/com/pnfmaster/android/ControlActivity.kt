package com.pnfmaster.android

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.pnfmaster.android.BtConnection.BluetoothCommunication
import com.pnfmaster.android.BtConnection.BluetoothScanActivity
import com.pnfmaster.android.MyApplication.Companion.context
import com.pnfmaster.android.chat.ChatActivity
import com.pnfmaster.android.database.connect
import com.pnfmaster.android.databinding.ActivityControlBinding
import com.pnfmaster.android.utils.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import java.util.Calendar
import java.util.Locale
import com.pnfmaster.android.BtConnection.MSG_READ
import com.pnfmaster.android.BtConnection.MSG_TOAST
import com.pnfmaster.android.BtConnection.MSG_WRITE

class ControlActivity : BaseActivity() {

    private lateinit var binding: ActivityControlBinding

    private lateinit var curTitle: String
    private var curLowerLimit: Int = -1
    private var curUpperLimit: Int = -1
    private var curPosition: Int = -1
    private var curTime: Int = -1
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置ActionBar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_menu)
            it.title = getString(R.string.Control)
        }

        binding.navView.setCheckedItem(R.id.navControl)
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navProfile -> jumpToActivity(this, "Profile")
                R.id.navControl -> binding.drawerLayout.closeDrawers()
                R.id.navTasks -> jumpToActivity(this, "Tasks")
                R.id.navLogout -> jumpToActivity(this, "Logout")
            }
            true
        }

        val navHeaderView = binding.navView.getHeaderView(0)

        lifecycleScope.launch {
            val userAccount = withContext(Dispatchers.IO) {
                connect.queryUsername(MyApplication.userId)
            }
            val tvUserName = navHeaderView.findViewById<TextView>(R.id.userName)
            // 如果不是未登录状态，则在nav_header中显示用户名
            if (!MyApplication.isSkipped) {
                tvUserName.text = userAccount
            }
        }

        val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val second = calendar.get(Calendar.SECOND)
                val curTime = "$hour:$minute:$second"

                when (msg.what) {
                    // 处理从蓝牙设备读取的数据
                    MSG_READ -> {
                        // val bytes = msg.arg1
                        val buffer = msg.obj as ByteArray
                        // 将buffer数组内的字节转换成字符串
                        val receivedData = String(buffer, Charset.forName("GBK"))
                        // 删除receivedData中的空格和无法解码字符
                        val filteredData = receivedData.filter { !it.isWhitespace() && it.code != 0xFFFD }
                        val content = "<font color='red'>[$curTime]"+ getString(R.string.msgRcv) +"$filteredData</font>"
                        binding.receiveText.append(Html.fromHtml(content, 0))
                        binding.receiveText.append("\n")

                        // ScrollView自动滚动到最后一行：
                        val scrollView = findViewById<ScrollView>(R.id.scrollView)
                        scrollView.post {
                            scrollView.fullScroll(View.FOCUS_DOWN)
                        }

                        // TODO：更新曲线
//                        val entries = ArrayList<Entry>()
//                        entries.add(Entry(0f, lowerLimitFloat)) // 点击启动之后开始计时
//
//                        val dataSet = LineDataSet(entries, "Force")
//                        val data = LineData(dataSet)
//                        binding.lineChart.data = data
//
//                        binding.lineChart.notifyDataSetChanged()
//                        binding.lineChart.invalidate() // refresh chart

                    }
                    // 处理连接中的错误信息
                    MSG_TOAST -> {
                        val bundle = msg.data
                        val toastMessage = bundle.getString("toast")
                        if (toastMessage != null) {
                            Log.e(TAG, toastMessage)
                        }
                        "错误：连接异常。".Toast()
                    }
                    // 处理发送出的数据
                    MSG_WRITE -> {
                        val buffer = msg.obj as ByteArray
                        val sentData = String(buffer, Charset.forName("GBK"))
                        val content = "[$curTime]" + getString(R.string.msgSend) + sentData
                        binding.receiveText.append(Html.fromHtml(content, 1))
                        binding.receiveText.append("\n")
                    }
                }
            }
        }

        val btComm = BluetoothCommunication(handler)
        // 由于未知原因，这里如果使用device和socket进行赋值，有时会出现为null的情况
//        val device = MyApplication.bluetoothDevice
//        val socket = MyApplication.bluetoothSocket

        binding.curDevice.text =
            if (MyApplication.bluetoothDevice != null)  MyApplication.bluetoothDevice!!.name
            else " ${getString(R.string.None)}"

        // 连接电机
        binding.connectBtn.setOnClickListener {
            if (MyApplication.bluetoothDevice == null || MyApplication.bluetoothSocket == null) {
                Log.e(
                    TAG, "MainActivity:StartBtn.\n" +
                            "MyApplication.bluetoothDevice is ${MyApplication.bluetoothDevice} \n " +
                            "MyApplication.bluetoothSocket is ${MyApplication.bluetoothSocket}"
                )
                getString(R.string.fail_no_connection).Toast()
            } else {
                if (binding.connectBtn.text == getString(R.string.build_connection)) {
                    getString(R.string.connect_success).Toast()
                    binding.connectBtn.text = getString(R.string.startTraining)
                } else {
                    MyApplication.bluetoothSocket.let {
                        Log.d(
                            TAG, "Both device and socket are not null.\n" +
                                    "device is ${MyApplication.bluetoothDevice}\n" +
                                    "socket is $it"
                        )
                        val connectedThread = btComm.ConnectedThread(it!!)
                        if (binding.connectBtn.text == getString(R.string.startTraining)) {
                            connectedThread.start()
                            binding.connectBtn.text = getString(R.string.endTraining)
                            setBtnState(binding.powerOnBtn, true)
                            setBtnState(binding.stopBtn, true)
                            setBtnState(binding.startStretchingBtn, true)
                            setBtnState(binding.holdBtn, true)
                            setBtnState(binding.relaxBtn, true)
                            setBtnState(binding.reStretchBtn, true)
                        } else {
                            connectedThread.cancel()
                            binding.connectBtn.text = getString(R.string.startTraining)
                            setBtnState(binding.powerOnBtn, false)
                            setBtnState(binding.stopBtn, false)
                            setBtnState(binding.startStretchingBtn, false)
                            setBtnState(binding.holdBtn, false)
                            setBtnState(binding.relaxBtn, false)
                            setBtnState(binding.reStretchBtn, false)
                        }
                    }
                }
            }
        }

        // 更改参数
        binding.ChangeParamsBtn.setOnClickListener {
            if (!judgeIfSkipped()) {
                val intent = Intent(this, TasksActivity::class.java)
                startActivity(intent)
            }
        }

        // Not clickable initially
        setBtnState(binding.powerOnBtn, false)
        // Turn on the motor
        binding.powerOnBtn.setOnClickListener {
            if (MyApplication.bluetoothDevice == null || MyApplication.bluetoothSocket == null) {
                getString(R.string.fail_no_connection).Toast()
                Log.e(TAG,"Main: StartBtn. Device or socket is null.\n" +
                        "device: ${MyApplication.bluetoothDevice}\n" +
                        "socket: ${MyApplication.bluetoothSocket}")
            } else {
                val connectedThread = btComm.ConnectedThread(MyApplication.bluetoothSocket!!)
                val hexString = "AA550407320000000000000000000D0A"
                // Converting hexadecimal strings to byte arrays
                val bytes = hexString.hexStringToByteArray()
                connectedThread.write(bytes)
            }
        }

        setBtnState(binding.holdBtn, false)
        // 电机急停
        binding.stopBtn.setOnClickListener {
            if (MyApplication.bluetoothDevice == null || MyApplication.bluetoothSocket == null) {
                getString(R.string.fail_no_connection).Toast()
                Log.e(TAG,"Main: StartBtn. Device or socket is null.\n" +
                        "device: ${MyApplication.bluetoothDevice}\n" +
                        "socket: ${MyApplication.bluetoothSocket}")
            } else {
                val connectedThread = btComm.ConnectedThread(MyApplication.bluetoothSocket!!)
                val hexString = "666f635430"
                val bytes = hexString.hexStringToByteArray()
                connectedThread.write(bytes)
            }
        }

        setBtnState(binding.startStretchingBtn, false)
        // 开始拉伸
        binding.startStretchingBtn.setOnClickListener {
            if (MyApplication.bluetoothDevice == null || MyApplication.bluetoothSocket == null) {
                getString(R.string.fail_no_connection).Toast()
                Log.e(TAG,"Main: StartBtn. Device or socket is null.\n" +
                        "device: ${MyApplication.bluetoothDevice}\n" +
                        "socket: ${MyApplication.bluetoothSocket}")
            } else {
                val connectedThread = btComm.ConnectedThread(MyApplication.bluetoothSocket!!)
                val hexString = "666F635431"
                val bytes = hexString.hexStringToByteArray()
                connectedThread.write(bytes)
            }
        }

        setBtnState(binding.holdBtn, false)
        // 维持静止
        binding.holdBtn.setOnClickListener {
            if (MyApplication.bluetoothDevice == null || MyApplication.bluetoothSocket == null) {
                getString(R.string.fail_no_connection).Toast()
                Log.e(TAG,"Main: StartBtn. Device or socket is null.\n" +
                        "device: ${MyApplication.bluetoothDevice}\n" +
                        "socket: ${MyApplication.bluetoothSocket}")
            } else {
                val connectedThread = btComm.ConnectedThread(MyApplication.bluetoothSocket!!)
                val hexString = "666F6353"
                val bytes = hexString.hexStringToByteArray()
                connectedThread.write(bytes)
            }
        }

        setBtnState(binding.relaxBtn, false)
        // 开始放松
        binding.relaxBtn.setOnClickListener {
            if (MyApplication.bluetoothDevice == null || MyApplication.bluetoothSocket == null) {
                getString(R.string.fail_no_connection).Toast()
                Log.e(TAG,"Main: StartBtn. Device or socket is null.\n" +
                        "device: ${MyApplication.bluetoothDevice}\n" +
                        "socket: ${MyApplication.bluetoothSocket}")
            } else {
                val connectedThread = btComm.ConnectedThread(MyApplication.bluetoothSocket!!)
                val hexString1 = "666F63542D33"
                val bytes1 = hexString1.hexStringToByteArray()
                //val hexString2 = "666F6347"
                //val bytes2 = hexString2.hexStringToByteArray()
                connectedThread.write(bytes1)
                //connectedThread.write(bytes2)
            }
        }

        setBtnState(binding.reStretchBtn, false)
        // 再次拉伸
        binding.reStretchBtn.setOnClickListener {
            if (MyApplication.bluetoothDevice == null || MyApplication.bluetoothSocket == null) {
                getString(R.string.fail_no_connection).Toast()
                Log.e(TAG,"Main: StartBtn. Device or socket is null.\n" +
                        "device: ${MyApplication.bluetoothDevice}\n" +
                        "socket: ${MyApplication.bluetoothSocket}")
            } else {
                val connectedThread = btComm.ConnectedThread(MyApplication.bluetoothSocket!!)
                val hexString = "666F6333"
                val bytes = hexString.hexStringToByteArray()
                connectedThread.write(bytes)
            }
        }

//        // 点击发送按钮调用write(bytes: ByteArray)方法
//        binding.SendBtn.setOnClickListener {
//            if (MyApplication.bluetoothDevice == null || MyApplication.bluetoothSocket == null) {
//                getString(R.string.fail_no_connection).Toast()
//                 Log.e(TAG,"Main: StartBtn. Device or socket is null.\n" +
//                         "device: ${MyApplication.bluetoothDevice}\n" +
//                         "socket: ${MyApplication.bluetoothSocket}")
//            } else {
//                val connectedThread = btComm.ConnectedThread(MyApplication.bluetoothSocket!!)
//                val hexString = binding.inputEditText.text.toString()  // 十六进制字符串
//                val bytes = hexString.hexStringToByteArray() // 将十六进制字符串转换为字节数组
//                connectedThread.write(bytes)
//            }
//        }

        // Open Chatting Activity
        binding.fab.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        onBackPressedDispatcher.addCallback(this) {
            val builder = AlertDialog.Builder(this@ControlActivity)
            builder.setTitle(getString(R.string.Hint))
                .setMessage(getString(R.string.logout))
                .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                    val logOutIntent = Intent(this@ControlActivity, LoginActivity::class.java)
                    startActivity(logOutIntent)
                    finish()
                }
                .setNegativeButton(getString(R.string.No),null)
                .create()
                .show()
        }
    }

    private fun setBtnState(button: Button, state: Boolean) {
        button.isEnabled = state

        val shapeDrawable = GradientDrawable()
        shapeDrawable.shape = GradientDrawable.RECTANGLE
        val dpValue = 10
        val pxValue = (dpValue * Resources.getSystem().displayMetrics.density).toInt()
        shapeDrawable.cornerRadius = pxValue.toFloat()
        if (button.id == R.id.stopBtn) {
            shapeDrawable.setColor(if (state) getColor(R.color.red) else getColor(R.color.gray))
        } else {
            shapeDrawable.setColor(if (state) getColor(R.color.teal_200) else getColor(R.color.gray))
        }
        button.background = shapeDrawable
    }

    private fun setParams(): ParamsGroup {
        Log.d("ControlActivity", "MyApplication.id = ${MyApplication.id}")
        var paramsGroup = ParamsGroup(-1, "", -1, -1, -1, -1)
        lifecycleScope.launch {
            Log.d("ControlActivity", "lifecycleScope.launch")
            // 如果id为-1，则尝试从SharedPreferences中获取id
            if (MyApplication.id == -1) {
                val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                MyApplication.id = sharedPreferences.getInt("MyApplicationId", -1) // 返回保存的id，如果没有找到则返回-1
            }
            paramsGroup = withContext(Dispatchers.IO) {
                connect.queryParamsWithId(MyApplication.id)
            }
            Log.d("ControlActivity", "query ended.")
            withContext(Dispatchers.Main) {
                if (MyApplication.id != -1) {
                    saveIdToPreferences(this@ControlActivity, MyApplication.id)
                    curTitle = paramsGroup.title
                    curLowerLimit = paramsGroup.lowerLimit
                    curUpperLimit = paramsGroup.upperLimit
                    curPosition = paramsGroup.motorPosition
                    curTime = paramsGroup.trainingTime
                    val text =
                        if (Locale.getDefault().language == "en") " Force: $curLowerLimit ~ $curUpperLimit N\n Position: $curPosition; Time: $curTime s"
                        else " 【$curTitle】力: $curLowerLimit ~ $curUpperLimit 牛\n 位置: $curPosition;训练时间: $curTime 秒"

                    if (MyApplication.isSkipped) {
                        binding.curParams.text = getString(R.string.login_to_see_params)
                    } else if (curLowerLimit == -1) {
                        binding.curParams.text = getString(R.string.no_params_selected)
                    } else {
                        binding.curParams.text = text
                    }

                }
            }
            Log.d("ControlActivity", "set ended.")
        }
        return paramsGroup
    }

    private fun saveIdToPreferences(context: Context, id: Int) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("MyApplicationId", id)
        editor.apply()
    }

    private fun String.hexStringToByteArray(): ByteArray {
        val result = ByteArray(length / 2)
        for (i in indices step 2) {
            val firstDigit = Character.digit(this[i], 16)
            val secondDigit = Character.digit(this[i + 1], 16)
            result[i / 2] = ((firstDigit shl 4) + secondDigit).toByte()
        }
        return result
    }

    /*private fun gbk2Hex(chineseText: String): String {
        // 将字符串从UTF-8转换为GBK编码
        val gbkBytes = chineseText.toByteArray(Charset.forName("GBK"))

        // 将字节数组转换为16进制字符串
        val hexString = gbkBytes.joinToString("") { byte ->
            "%02X".format(byte)
        }

        // 输出16进制字符串
        return hexString
    }*/

    // 右上角三个按钮的点击事件
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        binding.navView.setCheckedItem(R.id.navControl)
        setParams() // 设置当前参数
        if (MyApplication.bluetoothDevice == null || MyApplication.bluetoothSocket == null) {
            setBtnState(binding.powerOnBtn, false)
            setBtnState(binding.stopBtn, false)
            setBtnState(binding.startStretchingBtn, false)
            setBtnState(binding.holdBtn, false)
            setBtnState(binding.relaxBtn, false)
            setBtnState(binding.reStretchBtn, false)
        }
        binding.curDevice.text =
            if (MyApplication.bluetoothDevice != null)  MyApplication.bluetoothDevice!!.name
            else " ${getString(R.string.None)}"
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 打开左侧菜单
            android.R.id.home -> binding.drawerLayout.openDrawer(GravityCompat.START)

            // 打开蓝牙搜索界面
            R.id.bluetooth -> {
                val device = MyApplication.bluetoothDevice
                Log.d(TAG, "Main: 调用MyApplication.bluetoothDevice = $device")
                if (device == null) {
                    Log.d(TAG, "Device is null. Start the BluetoothScanActivity")
                    val intent = Intent(this, BluetoothScanActivity::class.java)
                    startActivity(intent)
                } else {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.currentDevice))
                        .setMessage(getString(R.string.currentDevice) + "：${device.name}\n" +
                                getString(R.string.MacAddress) + "：${device.address}")
                        .setPositiveButton(getString(R.string.Yes),null)
                        .setNegativeButton(getString(R.string.break_connection)) { _, _ ->
                            Log.d(TAG, "Main: onOptionsItemSelected. User ended the connection.")
                            binding.curDevice.text = " ${getString(R.string.None)}"
                            MyApplication.bluetoothSocket!!.close()
                            MyApplication.bluetoothDevice = null
                        }
                        .create()
                        .show()
                }
            }

            // 在文字模式和图像模式之间切换
            R.id.changeMode -> {
                if (binding.scrollView.visibility == View.VISIBLE) {
                    binding.scrollView.visibility = View.INVISIBLE
                    binding.lineChart.visibility = View.VISIBLE
                } else {
                    binding.scrollView.visibility = View.VISIBLE
                    binding.lineChart.visibility = View.INVISIBLE
                }
            }

            R.id.help -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.intro))
                    .setMessage(R.string.usage)
                    .setPositiveButton(getString(R.string.Yes)) { _, _ -> }
                    .create()
                    .show()
            }

            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    // 左侧滑动菜单中的点击事件
    private fun jumpToActivity(context: Context, targetActivity: String) {
        when (targetActivity) {
            "Profile" -> {
                if (!judgeIfSkipped()) {
                    val account = intent.getStringExtra("userAccount")
                    val profileIntent = Intent(this, ProfileActivity::class.java)
                    profileIntent.putExtra("userAccount", account)
                    startActivity(profileIntent)
                }
            }
            "Tasks" -> {
                if (!judgeIfSkipped()){
                    val account = intent.getStringExtra("userAccount")
                    val id = intent.getIntExtra("userId", -1)
                    val tasksIntent = Intent(this, TasksActivity::class.java)
                    tasksIntent.putExtra("userAccount", account)
                    tasksIntent.putExtra("userId", id)
                    startActivity(tasksIntent)
                }
            }
            "Logout" -> {
                if (!MyApplication.isSkipped) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(getString(R.string.Hint))
                        .setMessage(getString(R.string.logout))
                        .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                            val logOutIntent = Intent(context, LoginActivity::class.java)
                            startActivity(logOutIntent)
                            finish()
                        }
                        .setNegativeButton(getString(R.string.No),null)
                        .create()
                        .show()
                } else {
                    val logOutIntent = Intent(context, LoginActivity::class.java)
                    startActivity(logOutIntent)
                    finish()
                }
            }
        }
    }

    // 判断用户是否跳过登录，如果跳过则返回 true
    private fun judgeIfSkipped(): Boolean {
        val isSkipped = MyApplication.isSkipped
        if (isSkipped) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.Hint))
                .setMessage(getString(R.string.notLoginYet))
                .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                    MyApplication.isSkipped = true
                    val backIntent = Intent(this, LoginActivity::class.java)
                    startActivity(backIntent)
                    finish()
                }
                .setNegativeButton(getString(R.string.No), null)
                .create()
                .show()
            return true
        }
        return false
    }

    companion object {
        const val TAG = "ControlActivity"
    }
}
