package com.pnfmaster.android

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pnfmaster.android.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    private lateinit var binding : ActivityTestBinding
    private lateinit var containerLayout: LinearLayout
    private lateinit var starButton: Button
    private lateinit var mask: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        containerLayout = binding.containerLayout
        starButton = binding.starButton
        mask = binding.mask

        binding.addButton.setOnClickListener {
            addEditText()
        }

        binding.starButton.setOnClickListener {
            // 处理收集内容的逻辑
            // 星号按钮状态切换
            starButton.isSelected = !starButton.isSelected
            // 显示 Toast，表示收集成功
            if (starButton.isSelected) {
                Toast.makeText(this, "收集成功", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addEditText() {
        // 创建一个新的 EditText 视图
        val editText = EditText(this)
        // 设置布局参数
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // 添加到容器中
        containerLayout.addView(editText, layoutParams)

        // 设置 EditText 的监听器，当失去焦点时转换为 TextView
        editText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                val text = (view as EditText).text.toString()
                // 创建一个新的 TextView 视图
                val textView = TextView(this)
                textView.text = text
                // 替换 EditText
                val index = containerLayout.indexOfChild(view)
                containerLayout.removeViewAt(index)
                containerLayout.addView(textView, index)
                // 显示 Toast，表示添加成功
                Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show()
            }
        }

        // 遮罩层的点击事件
        mask.setOnClickListener {
            editText.clearFocus()
        }

    }
}
