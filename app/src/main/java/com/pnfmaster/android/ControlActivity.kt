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
import androidx.core.view.GravityCompat
import com.pnfmaster.android.BtConnection.BluetoothScanActivity
import com.pnfmaster.android.databinding.ActivityControlBinding
import com.pnfmaster.android.BtConnection.BluetoothCommunication
import com.pnfmaster.android.utils.Toast
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
            it.title = "你好, ${intent.getStringExtra("userAccount")}"
            // todo: 改成 你好，用户名字
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
                        val bytes = msg.arg1
                        val buffer = msg.obj as ByteArray
                        // 将buffer数组内的字节转换成字符串
                        val receivedData = String(buffer, 0, bytes)
                        val content = "<font color='red'>[$curTime]收到消息：$receivedData</font>"
                        binding.receiveText.append(Html.fromHtml(content))
                        binding.receiveText.append("\n")

                        // 设置让收到内容的TextView自动滚动到最后一行：
                        val textView = binding.receiveText
                        val offset = textView.lineCount * textView.lineHeight
                        if (offset > textView.height) {
                            textView.scrollTo(0, offset - textView.height)
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
                        val content = "<font color='blue'>[$curTime]发送消息：${binding.inputEditText.text}</font>\n"
                        binding.receiveText.append(Html.fromHtml(content))
                        binding.receiveText.append("\n")

                        // 设置让收到内容的TextView自动滚动到最后一行：
                        val textView = binding.receiveText
                        val offset = textView.lineCount * textView.lineHeight
                        if (offset > textView.height) {
                            textView.scrollTo(0, offset - textView.height)
                        }
                    }
                }
            }
        }

        val btComm = BluetoothCommunication(handler)
        val device = MyApplication.bluetoothDevice
        val socket = MyApplication.bluetoothSocket

        binding.curDevice.text = if (device != null) {
            "${device.name}(mac地址: ${device.address})"
        } else {
            "无"
        }

        // 点击启动之后开始运行线程，可以读取信息了。
        binding.StartBtn.setOnClickListener {
            if (device == null || socket == null) {
                Log.e(TAG, "MainActivity:StartBtn.\n" +
                        "device is $device \n" +
                        "socket is $socket \n" +
                        "MyApplication.bluetoothDevice is ${MyApplication.bluetoothDevice} \n " +
                        "MyApplication.bluetoothSocket is ${MyApplication.bluetoothSocket}")
                "启动失败：尚未连接设备".Toast()
            } else {
                socket.let {
                    Log.d(TAG, "Both device and socket are not null.\n" +
                            "device is $device\n" +
                            "socket is $it")
                    val connectedThread = btComm.ConnectedThread(it)
                    if (binding.StartBtn.text == "启动") {
                        connectedThread.start()
                        binding.StartBtn.text = "关闭"
                    } else {
                        connectedThread.cancel()
                        binding.StartBtn.text = "启动"
                    }
                }

            }
        }

        // 点击发送按钮调用write(bytes: ByteArray)方法
        binding.SendBtn.setOnClickListener {
            if (device == null || MyApplication.bluetoothSocket == null) {
                 "发送失败：设备或Socket为空".Toast()
                 Log.e(TAG,"Main: StartBtn. Device or socket is null.\n" +
                "device: $device\n" +
                "socket: $socket")
            } else {
                val connectedThread = btComm.ConnectedThread(MyApplication.bluetoothSocket!!)
                val msg = binding.inputEditText.text
                val msgString = msg.toString()
                Log.d(TAG, "Ready to send $msgString")
                connectedThread.write(msgString.toByteArray())
            }
        }
    }


    // 右上角三个按钮的点击事件
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    @SuppressLint("MissingPermission")
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
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("当前蓝牙设备")
                        .setMessage("设备名称：${device.name}\n" +
                                "设备地址：${device.address}")
                        .setPositiveButton("我知道了") { _, _ -> }
                        .setNegativeButton("断开连接") { _, _ ->
                            Log.d(TAG, "Main: onOptionsItemSelected. User cancelled the bluetooth.")
                            MyApplication.bluetoothSocket!!.close()
                        }
                        .create()
                        .show()
                }
            }

            R.id.backup -> {
                judgeIfSkipped()
                "将数据上传至云端功能：正在开发中".Toast() // TODO
            }

            R.id.help -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("用法介绍")
                    .setMessage(R.string.usage)
                    .setPositiveButton("我知道了") { _, _ -> }
                    .create()
                    .show()
            }

            R.id.developerMode -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("提示")
                    .setMessage("确认进入开发者模式？")
                    .setPositiveButton("确认") { _, _ ->
                        // TODO: 跳转至 Developer Mode
                    }
                    .setNegativeButton("取消") { _, _ -> }
                    .create()
                    .show()
            }

            R.id.settings -> "设置功能：正在开发中".Toast() // TODO
        }
        return true
    }

    // 左侧滑动菜单中的点击事件
    private fun jumpToActivity(context: Context, targetActivity: String) {
        when (targetActivity) {
            "Profile" -> {
                judgeIfSkipped()
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
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
                judgeIfSkipped()
            }
            "Logout" -> { // 返回登录菜单
                val builder = AlertDialog.Builder(this)
                builder.setTitle("提示")
                    .setMessage("确定要退出登录吗？")
                    .setPositiveButton("退出") { _, _ ->
                        val logOutIntent = Intent(context, LoginActivity::class.java)
                        startActivity(logOutIntent)
                        finish()
                    }
                    .setNegativeButton("取消") { _, _ -> }
                    .create()
                    .show()
            }
        }
    }

    private fun judgeIfSkipped() {
        val isSkipped = intent.getBooleanExtra("skip", false)
        if (isSkipped) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("提示")
                .setMessage("未登录用户无法使用此功能。是否现在登录？")
                .setPositiveButton("是") { _, _ ->
                    val backIntent = Intent(this, LoginActivity::class.java)
                    startActivity(backIntent)
                    finish()
                }
                .setNegativeButton("否") { _, _ -> }
                .create()
                .show()
        }
    }
}
