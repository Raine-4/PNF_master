package com.pnfmaster.android.control

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.pnfmaster.android.BtConnection.BluetoothCommunication
import com.pnfmaster.android.MyApplication
import com.pnfmaster.android.R
import com.pnfmaster.android.utils.Toast
import java.nio.charset.Charset
import java.util.Calendar

class PositionFragment : Fragment() {

    companion object {
        const val TAG = "PositionFragment"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_position, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        /* 1. 图表部分
        * 目前的数据是随机生成的。
        * 之后应该需要在onCreate()方法中从蓝牙部分调取获得的数据
        */
        val chart = view.findViewById<LineChart>(R.id.lineChart)
        // 设置描述、坐标轴等
        chart.setPinchZoom(true)
        chart.setTouchEnabled(true)
        chart.setDragEnabled(true)
        chart.setScaleEnabled(true)
        chart.setDrawGridBackground(false)

        // 设置X轴
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f // 刻度之间的最小间隔
            labelCount = 20 // 刻度数量
            axisMinimum = 0f
            axisMaximum = 100f
        }

        /* 添加限制线
        val limitLine = LimitLine(30F, "尽量高于此线")
        limitLine.apply {
            lineWidth = 1.5f
            textSize = 10f
            textColor = Color.RED
            lineColor = Color.BLUE
        }
        */

        // 设置Y轴
        chart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 100f
            granularity = 1f // 颗粒度
            labelCount = 11
            textColor = Color.BLUE
            // addLimitLine(limitLine)
        }

        /*
        // 设置图例
        chart.legend.isEnabled = false

        // 设置描述
        val description = Description()
        description.apply {
            text = "描述"
            textColor = Color.RED
        }
        chart.description = description
        */

        // 添加随机数据 TODO：在 Handler 中替换成真正的数据
        val entries = mutableListOf<Entry>()
        for (i in 0 until 100) {
            entries.add(Entry(i.toFloat(), (Math.random() * 100f).toFloat()))
        }
        addData(chart, entries)

        /* 2. 蓝牙交互部分
        *  val handler: 处理蓝牙发送和读取数据
        *  蓝牙相关信息，如device、socket等
        *  使能按钮和失能按钮
        */

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
                        // TODO：处理收到的数据，可视化。
//                        binding.receiveText.append(Html.fromHtml(content))
//                        binding.receiveText.append("\n")
//
//                        // ScrollView自动滚动到最后一行：
//                        val scrollView = findViewById<ScrollView>(R.id.scrollView)
//                        scrollView.post {
//                            scrollView.fullScroll(View.FOCUS_DOWN)
//                        }
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
                    // 向蓝牙设备发送数据
                    1 -> {
                        // TODO：处理发送的数据
//                        val content = "<font color='blue'>[$curTime]" + getString(R.string.msgSend) + "${binding.inputEditText.text}</font>\n"
//                        binding.receiveText.append(Html.fromHtml(content))
//                        binding.receiveText.append("\n")
                    }
                }
            }
        }

        val btComm = BluetoothCommunication(handler)
        val device = MyApplication.bluetoothDevice
        val socket = MyApplication.bluetoothSocket

        // 电机使能
        val enableBtn = view.findViewById<Button>(R.id.enableBtn)
        enableBtn.setOnClickListener {
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

        // 停止使能
        val disableBtn = view.findViewById<Button>(R.id.disableBtn)
        disableBtn.setOnClickListener {
            if (device == null || MyApplication.bluetoothSocket == null) {
                getString(R.string.fail_no_connection).Toast()
                Log.e(TAG,"Main: StartBtn. Device or socket is null.\n" +
                        "device: $device\n" +
                        "socket: $socket")
            } else {
                val connectedThread = btComm.ConnectedThread(MyApplication.bluetoothSocket!!)
                val hexString = "CDA3D6B9CAB9C4DC0d0a"
                val bytes = hexString.hexStringToByteArray()
                connectedThread.write(bytes)
            }
        }

    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }


// -----------------------------------------  工具函数  -----------------------------------------
    private fun addData(chart: LineChart, entries: MutableList<Entry>) {

        val dataSets = mutableListOf<LineDataSet>() // 线集合

        val dataSet = LineDataSet(entries, "Data Set") // 一条线（点集合）
        dataSet.color = Color.RED
        dataSets.add(dataSet)

        val data = LineData(dataSets as List<ILineDataSet>?)
        chart.data = data
        chart.invalidate()
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
}