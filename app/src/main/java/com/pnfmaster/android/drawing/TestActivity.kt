package com.pnfmaster.android.drawing

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.pnfmaster.android.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding
    private lateinit var chart: LineChart
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chart = binding.lineChart

        // 设置描述、坐标轴等
        chart.description.isEnabled = false
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

        // 添加限制线
        val limitLine = LimitLine(30F, "尽量高于此线")
        limitLine.apply {
            lineWidth = 1.5f
            textSize = 10f
            textColor = Color.RED
            lineColor = Color.BLUE
        }

        // 设置Y轴
        chart.axisRight.isEnabled = false

        chart.axisLeft.apply {
            isEnabled = true
            addLimitLine(limitLine)
            axisMinimum = 0f
            axisMaximum = 100f
            granularity = 1f
            labelCount = 11
            textColor = Color.BLUE
        }

        // 设置图例
        chart.legend.isEnabled = false

        // 设置描述
        val description = Description()
        description.apply {
            text = "X轴描述"
            textColor = Color.RED
        }
        chart.description = description

        // 添加随机数据
        val entries = mutableListOf<Entry>()
        for (i in 0 until 100) {
            entries.add(Entry(i.toFloat(), (Math.random() * 100f).toFloat()))
        }
        addData(entries)

    }

    private fun addData(entries: MutableList<Entry>) {

        val dataSets = mutableListOf<LineDataSet>() // 线集合

        val dataSet = LineDataSet(entries, "Data Set") // 一条线（点集合）
        dataSet.color = Color.RED
        dataSets.add(dataSet)

        val data = LineData(dataSets as List<ILineDataSet>?)
        chart.data = data
        chart.invalidate()
    }
}