package com.pnfmaster.android

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.GravityCompat
import com.pnfmaster.android.BtConnection.BluetoothCommunication
import com.pnfmaster.android.BtConnection.BluetoothScanActivity
import com.pnfmaster.android.databinding.ActivityControlBinding
import com.pnfmaster.android.utils.Toast
import java.nio.charset.Charset
import java.util.Calendar

/* 一次清理缓存之后这里就不能再引用了，非常奇怪。
 * 编译器不会报错，但是运行时却显示Unresolved reference: MSG_READ
 * 现在只能暂时用真值(0,1,2)来代替。
 */
//import com.pnfmaster.android.BtConnection.MSG_READ
//import com.pnfmaster.android.BtConnection.MSG_TOAST
//import com.pnfmaster.android.BtConnection.MSG_WRITE

class ControlActivity : BaseActivity() {

    private lateinit var binding: ActivityControlBinding
    private val TAG = "MainActivity"


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置ActionBar
        setSupportActionBar(binding.toolbarControl)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_menu)
            it.title = getString(R.string.Control)
        }

        val navView = binding.navView
        navView.setCheckedItem(R.id.navControl)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navProfile -> jumpToActivity(this, "Profile")
                R.id.navControl -> jumpToActivity(this, "Control")
                R.id.navTasks -> jumpToActivity(this, "Tasks")
                R.id.navLogout -> jumpToActivity(this, "Logout")
            }
            true
        }

        val navHeaderView = binding.navView.getHeaderView(0)
        val userAccount = intent.getStringExtra("userAccount")
        val tvUserName = navHeaderView.findViewById<TextView>(R.id.userName)
        // 如果不是未登录状态，则在nav_header中显示用户名
        if (!MyApplication.isSkipped) {
            tvUserName.text = userAccount
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
                    0 -> {
                        // val bytes = msg.arg1
                        val buffer = msg.obj as ByteArray
                        // 将buffer数组内的字节转换成字符串
                        val receivedData = String(buffer, Charset.forName("GBK"))
                        val filteredData = receivedData.filter { !it.isWhitespace() && it.code != 0xFFFD }
                        val content = "<font color='red'>[$curTime]收到消息：$filteredData</font>"
                        binding.receiveText.append(Html.fromHtml(content))
                        binding.receiveText.append("\n")

                        // ScrollView自动滚动到最后一行：
                        val scrollView = findViewById<ScrollView>(R.id.scrollView)
                        scrollView.post {
                            scrollView.fullScroll(View.FOCUS_DOWN)
                        }
                    }
                    // 处理连接中的错误信息
                    2 -> {
                        val bundle = msg.data
                        val toastMessage = bundle.getString("toast")
                        if (toastMessage != null) {
                            Log.e(TAG, toastMessage)
                        }
                        "错误：连接异常。".Toast()
                    }
                    1 -> {
                        val content = "<font color='blue'>[$curTime]" + getString(R.string.msgSend) + "${binding.inputEditText.text}</font>\n"
                        binding.receiveText.append(Html.fromHtml(content))
                        binding.receiveText.append("\n")
                    }
                }
            }
        }

        val btComm = BluetoothCommunication(handler)
        val device = MyApplication.bluetoothDevice
        val socket = MyApplication.bluetoothSocket

        binding.curDevice.text =
            if (device != null)  device.name
            else " ${getString(R.string.None)}"

        // 点击启动之后开始运行线程，可以读取信息了。
        binding.StartBtn.setOnClickListener {
            if (device == null || socket == null) {
                Log.e(TAG, "MainActivity:StartBtn.\n" +
                        "device is $device \n" +
                        "socket is $socket \n" +
                        "MyApplication.bluetoothDevice is ${MyApplication.bluetoothDevice} \n " +
                        "MyApplication.bluetoothSocket is ${MyApplication.bluetoothSocket}")
                getString(R.string.fail_no_connection).Toast()
            } else {
                socket.let {
                    Log.d(TAG, "Both device and socket are not null.\n" +
                            "device is $device\n" +
                            "socket is $it")
                    val connectedThread = btComm.ConnectedThread(it)
                    if (binding.StartBtn.text == getString(R.string.start)) {
                        connectedThread.start()
                        binding.StartBtn.text = getString(R.string.Close)
                    } else {
                        connectedThread.cancel()
                        binding.StartBtn.text = getString(R.string.start)
                    }
                }

            }
        }

        // TEST
        binding.enableBtn.setOnClickListener {
            if (device == null || MyApplication.bluetoothSocket == null) {
                getString(R.string.fail_no_connection).Toast()
                Log.e(TAG,"Main: StartBtn. Device or socket is null.\n" +
                        "device: $device\n" +
                        "socket: $socket")
            } else {
                val connectedThread = btComm.ConnectedThread(MyApplication.bluetoothSocket!!)
                val hexString = "B5E7BBFACAB9C4DC0d0a"  // 十六进制字符串
                val bytes = hexString.hexStringToByteArray() // 将十六进制字符串转换为字节数组
                connectedThread.write(bytes)
            }
        }

        binding.disableBtn.setOnClickListener {
            if (device == null || MyApplication.bluetoothSocket == null) {
                getString(R.string.fail_no_connection).Toast()
                Log.e(TAG,"Main: StartBtn. Device or socket is null.\n" +
                        "device: $device\n" +
                        "socket: $socket")
            } else {
                val connectedThread = btComm.ConnectedThread(MyApplication.bluetoothSocket!!)
                val hexString = "CDA3D6B9CAB9C4DC0d0a"  // 十六进制字符串
                val bytes = hexString.hexStringToByteArray() // 将十六进制字符串转换为字节数组
                connectedThread.write(bytes)
            }
        }

        // 点击发送按钮调用write(bytes: ByteArray)方法
        binding.SendBtn.setOnClickListener {
            if (device == null || MyApplication.bluetoothSocket == null) {
                getString(R.string.fail_no_connection).Toast()
                 Log.e(TAG,"Main: StartBtn. Device or socket is null.\n" +
                "device: $device\n" +
                "socket: $socket")
            } else {
                val connectedThread = btComm.ConnectedThread(MyApplication.bluetoothSocket!!)
                val hexString = binding.inputEditText.text.toString()  // 十六进制字符串
                val bytes = hexString.hexStringToByteArray() // 将十六进制字符串转换为字节数组
                connectedThread.write(bytes)
            }
        }
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

    /*// 为了能让toolbar显示用户的名字，需要用userId在数据库里查一下
    @SuppressLint("Range")
    private fun getRealName(id: Int): String {
        if (id == -1) {
            return getString(R.string.defaultUserName)
        }

        // ----------------------------------
        var infoList = listOf<Any>()
        fun main() {
            GlobalScope.launch {
                infoList = connect.queryUserInfo(MyApplication.userId)
                Log.d("profile", "private fun getRealName： infolist = $infoList")
            }
            Thread.sleep(1000)
        }
        val loadingDialog = LoadingDialog(this)
        loadingDialog.show()
        main()
        loadingDialog.dismiss()
        // ----------------------------------

        return infoList[0] as String
    }*/

    // 右上角三个按钮的点击事件
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 打开左滑菜单
            android.R.id.home -> binding.drawerLayout.openDrawer(GravityCompat.START)
            // 右上角按钮
            R.id.bluetooth -> {
                val device = MyApplication.bluetoothDevice
                Log.d(TAG, "Main: 调用MyApplication.bluetoothDevice = $device")
                if (device == null) {
                    Log.d(TAG, "Device is null. Start the BluetoothScanActivity")
                    val intent = Intent(this, BluetoothScanActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.currentDevice))
                        .setMessage(getString(R.string.currentDevice) + "：${device.name}\n" +
                                getString(R.string.MacAddress) + "：${device.address}")
                        .setPositiveButton(getString(R.string.Yes),null)
                        .setNegativeButton(getString(R.string.break_connection)) { _, _ ->
                            Log.d(TAG, "Main: onOptionsItemSelected. User cancelled the bluetooth.")
                            binding.curDevice.text = " ${getString(R.string.None)}"
                            MyApplication.bluetoothSocket!!.close()
                            MyApplication.bluetoothDevice = null
                        }
                        .create()
                        .show()
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

            R.id.developerMode -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.Hint))
                    .setMessage(getString(R.string.developerMode))
                    .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                        // TODO: 跳转至 Developer Mode
                    }
                    .setNegativeButton(getString(R.string.No),null)
                    .create()
                    .show()
            }

            R.id.settings -> "^_^".Toast() // TODO
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
            "Control" -> {
                // 如果当前界面不在Control界面中，则跳转至Control界面
                // TODO：目前MainActivity是作为开发者模式存在的，之后可能需要更改要跳转的Activity
                if (javaClass.simpleName != ControlActivity::class.java.simpleName) {
                    val intent = Intent(this, ControlActivity::class.java)
                    startActivity(intent)
                    finish()
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
            "Logout" -> { // 返回登录菜单
                if (!judgeIfSkipped()) {
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
}