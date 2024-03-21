package com.pnfmaster.android.BtConnection

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.pnfmaster.android.ControlActivity
import com.pnfmaster.android.MyApplication
import com.pnfmaster.android.R
import com.pnfmaster.android.adapters.OnItemClickListener
import com.pnfmaster.android.adapters.btDeviceAdapter
import com.pnfmaster.android.databinding.ActivityScanBinding
import com.pnfmaster.android.utils.Toast
import java.io.IOException
import java.util.UUID

class BluetoothScanActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var binding: ActivityScanBinding

    private val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val TAG = "ScanActivity"
    private var mAdapter: btDeviceAdapter? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 注册广播
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND) // 发现新的蓝牙设备
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED) // 配对完成
        registerReceiver(mBluetoothReceiver, intentFilter)

        binding.tvScanStatus.setOnClickListener {
            if (mBluetoothAdapter.isDiscovering) stopScanning() else startScanning()
        }

        binding.backToMainBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("提示")
                .setMessage("尚未与设备建立蓝牙连接。是否确定返回主界面？")
                .setPositiveButton("确定") { _, _ ->
                    val backIntent = Intent(this, ControlActivity::class.java)
                    startActivity(backIntent)
                    finish()
                }
                .setNegativeButton("取消") { _, _ -> }
                .create()
                .show()
        }
    }

    /**
     * startScanning()：开始扫描
     * 0. 检查并请求权限
     * 1. 清楚设备列表和设备名列表
     * 2. 通知适配器刷新视图
     * 3. 开始蓝牙扫描
     * 4. 设置按钮的文字为停止搜索
     * 5. 设置进度条可见
     * 当扫描到设备时系统会发出广播
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun startScanning() {
        // Request for permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 1)
            return
        }

        listDevices.clear()
        listDeviceName.clear()
        mAdapter?.notifyDataSetChanged() // 通知适配器刷新视图
        mBluetoothAdapter.startDiscovery()
        binding.tvScanStatus.text = "停止搜索"
        binding.pbScanLoading.visibility = View.VISIBLE
    }

    /**
     * stopScanning()：停止扫描
     * 0. 检查并请求权限
     * 1. 关闭蓝牙扫描
     * 2. 设置按钮的文字为开始搜索
     * 3. 设置进度条不可见
     * 当扫描到设备时系统会发出广播
     */
    private fun stopScanning() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 2)
            return
        }
        mBluetoothAdapter.cancelDiscovery()
        binding.tvScanStatus.text = "开始搜索"
        binding.pbScanLoading.visibility = View.INVISIBLE
    }

    /**
     * 每扫描到一个设备，系统都会发送此广播。
     * 接收系统扫描到设备的广播，从广播中获取扫描到的设备的名称和地址等信息。
     */
    private val listDevices: MutableList<BluetoothDevice> = ArrayList() // 存放扫描到的设备
    private val listDeviceName: MutableList<String> = ArrayList() // 存放扫描到的设备名字

    private val mBluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            // 第一个广播接收：检查是否搜索到蓝牙设备
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                // 获取蓝牙设备
                val scanDevice = intent.getParcelableExtra<BluetoothDevice?>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(context as Activity,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 2)
                    return
                } else scanDevice?.name

                // 扫描到的设备为空或者设备名为空
                if ((scanDevice == null) || (deviceName == null)) return

                Log.d(TAG, "Device found: $deviceName")
                // 检查deviceName在设备名列表中的索引，如果不存在会返回-1，此时就需要将其加入列表中。
                if (listDeviceName.indexOf(deviceName) == -1) {
                    Log.i(TAG, "Device added")
                    listDevices.add(scanDevice)
                    listDeviceName.add(deviceName)
                    // 把“当前无设备”的空布局关掉
                    val emptyLayout = findViewById<View>(R.id.empty_lay)
                    emptyLayout.visibility = View.INVISIBLE
                }
            }

            // 第二个广播接收：蓝牙绑定状态发生改变
            else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                // device!!：name为null的device根本不会出现在列表中
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)!!

                when (device.bondState) {
                    BluetoothDevice.BOND_BONDING -> "正在与${device.name}配对中".Toast()
                    BluetoothDevice.BOND_NONE -> "取消与${device.name}的配对".Toast()
                    BluetoothDevice.BOND_BONDED -> {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("提示")
                            .setMessage("与设备配对完成。再次点击设备名称即可连接")
                            .setPositiveButton("我知道了") { _, _ ->
                            }.create().show()
                    }
                }
            }

            /**
             * 把扫描到的列表放到adapter中，显示在屏幕上
             * 如果mAdapter为空则会执行run{}中的代码，进行相关配置，最终返回配置的结果mAdapter
             */
            mAdapter ?: run {
                mAdapter = btDeviceAdapter(listDevices)
                binding.rvDevice.apply {
                    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                    layoutManager = LinearLayoutManager(this@BluetoothScanActivity)
                    adapter = mAdapter
                }
                mAdapter!!.setOnItemClickListener(this@BluetoothScanActivity)
                mAdapter
            }
            mAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * 点击要连接的设备
     * 1. 关闭蓝牙扫描；
     * 2. 如果点击的设备没有配对，则开始绑定；
     * 3. 如果点击的设备已经配对，则建立连接。
     * 4. SuppressLint的原因是：此时扫描权限必然已经打开。
     */
    @SuppressLint("MissingPermission")
    override fun onItemClick(view: View?, position: Int) {

        if (mBluetoothAdapter.isDiscovering) stopScanning()
        binding.tvScanStatus.text = "开始搜索"

        if (listDevices[position].bondState == BluetoothDevice.BOND_NONE) {
            "未配对：开始配对".Toast()
            val method = BluetoothDevice::class.java.getMethod("createBond")
            method.invoke(listDevices[position])
        }

        val pairedDevices = mBluetoothAdapter.bondedDevices
        val targetDeviceName = getString(R.string.TargetDeviceName)
        for (pairedDevice in pairedDevices) {
            if (pairedDevice.name == targetDeviceName) {
                val uuid = UUID.fromString(getString(R.string.UUID))
                val socket = pairedDevice?.createRfcommSocketToServiceRecord(uuid)
                if (pairedDevice != null && socket != null) {
                    Log.d(TAG, "BluetoothScan: onItemClick. Socket build successfully.\n" +
                            "Device = $pairedDevice\n" +
                            "Socket = $socket")
                    MyApplication.bluetoothDevice = pairedDevice
                    MyApplication.bluetoothSocket = socket
                    try{
                        socket.connect()
                        "Socket连接成功，可以开始训练。".Toast()
                    } catch (e: IOException) {
                        "连接超时".Toast()
                        Log.e(TAG, "BluetoothScan: onItemClick. Socket connection time out.")
                    }

                    val backIntent = Intent(this, ControlActivity::class.java)
                    startActivity(backIntent)
                    finish()
                } else {
                    Log.e(TAG, "BluetoothScan: fun onItemClick: device is $pairedDevice | socket is $socket")
                    "连接失败：尚未配对".Toast()
                }
                break
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStop() {
        super.onStop()
        if (mBluetoothAdapter.isDiscovering) mBluetoothAdapter.cancelDiscovery()
        unregisterReceiver(mBluetoothReceiver) // 取消注册接收蓝牙广播通知
    }
}