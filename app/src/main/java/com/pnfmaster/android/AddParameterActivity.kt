package com.pnfmaster.android

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.pnfmaster.android.databinding.ActivityAddParameterBinding
import kotlin.random.Random

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
            // Generate random parameters
            val upperLimit = Random.nextInt(100, 200)
            val lowerLimit = Random.nextInt(0, 100)
            val motorPosition = Random.nextInt(0, 100)
            val trainingTime = Random.nextInt(1, 60)

            // Update EditTexts
            binding.forceUpperLimit.setText(upperLimit.toString())
            binding.forceLowerLimit.setText(lowerLimit.toString())
            binding.motorPosition.setText(motorPosition.toString())
            binding.trainingTime.setText(trainingTime.toString())

            // Update chart data
            val entries = ArrayList<Entry>()
            entries.add(Entry(0f, lowerLimit.toFloat()))
            entries.add(Entry(1f, upperLimit.toFloat()))

            val dataSet = LineDataSet(entries, "Force")
            val data = LineData(dataSet)
            binding.chart.data = data
            binding.chart.invalidate() // refresh chart
        }
    }
}