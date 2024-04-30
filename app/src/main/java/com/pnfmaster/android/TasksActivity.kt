package com.pnfmaster.android

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pnfmaster.android.adapters.ParamsAdapter
import com.pnfmaster.android.database.connect
import com.pnfmaster.android.databinding.ActivityTasksBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TasksActivity : BaseActivity() {

    private lateinit var binding: ActivityTasksBinding
    private lateinit var paramsAdapter : ParamsAdapter
    private lateinit var rvParams : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rvParams = binding.rvParams

        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = getString(R.string.my_param)
        }

        // Create empty data list and adapter
        val paramsList = ArrayList<ParamsGroup>()
        initParamsAdapter(paramsList)

        // 创建 ItemTouchHelper.SimpleCallback
        val itemTouchHelperCallback = object
            : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // 不处理拖拽排序
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                AlertDialog.Builder(this@TasksActivity)
                    .setTitle(getString(R.string.delete))
                    .setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_parameter))
                    .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                        // 获取被滑动的 item 的位置
                        val position = viewHolder.adapterPosition
                        // 从数据源中移除该 item 对应的数据
                        val paramsGroup = paramsAdapter.paramsList[position]
                        paramsAdapter.paramsList.removeAt(position)
                        // 通知 adapter 该位置的 item 已被移除
                        paramsAdapter.notifyItemRemoved(position)
                        // 从数据库中删除该参数
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                // 暴力重建Activity以关闭滑动操作
                                connect.deleteParams(paramsGroup.id)
                            }
                        }
                    }
                    .setNegativeButton(getString(R.string.No)) { _, _ ->
                        this@TasksActivity.recreate()
                    }
                .create().show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                val isCanceled = dX == 0f && !isCurrentlyActive

                if (isCanceled) {
                    clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    return
                }

                // Draw the red delete background
                val background = ColorDrawable()
                background.color = Color.RED
                background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                background.draw(c)

                // Calculate position of delete icon
                val deleteIcon = ContextCompat.getDrawable(this@TasksActivity, R.drawable.ic_delete)!!
                val deleteIconTop = itemView.top + (itemHeight - deleteIcon.intrinsicHeight) / 2
                val deleteIconMargin = (itemHeight - deleteIcon.intrinsicHeight) / 2
                val deleteIconLeft = itemView.right - deleteIconMargin - deleteIcon.intrinsicWidth
                val deleteIconRight = itemView.right - deleteIconMargin
                val deleteIconBottom = deleteIconTop + deleteIcon.intrinsicHeight

                // Draw the delete icon
                deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                deleteIcon.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
                c?.drawRect(left, top, right, bottom, Paint().apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                })
            }
        }

        // 将 ItemTouchHelper 附加到 RecyclerView
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rvParams)
    }

    private fun initParamsAdapter(paramsList: MutableList<ParamsGroup>) {
        lifecycleScope.launch {
            val paramsGroupList = withContext(Dispatchers.IO) {
                Log.d("TasksActivity", "正在查询参数...first")
                connect.queryParams()
            }
            withContext(Dispatchers.Main) {
                Log.d("TasksActivity", "开始初始化适配器")
                // 先用空列表初始化适配器
                paramsAdapter = ParamsAdapter(paramsList)
                // 开关Empty layout
                val emptyLayout = findViewById<View>(R.id.no_params_layout)
                if (paramsGroupList.isNotEmpty()) {
                    emptyLayout.visibility = View.INVISIBLE
                    Log.d("TasksActivity", "Parameters found")
                } else {
                    emptyLayout.visibility = View.VISIBLE
                    Log.d("TasksActivity", "No parameters found")
                }
                // 用查询到的数据更新适配器
                paramsAdapter.update(paramsGroupList)

                val layoutManager = LinearLayoutManager(this@TasksActivity)
                rvParams.adapter = paramsAdapter
                rvParams.layoutManager = layoutManager
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::paramsAdapter.isInitialized) {
            loadData()
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            val params = withContext(Dispatchers.IO) {
                connect.queryParams()
            }
            withContext(Dispatchers.Main) {
                paramsAdapter.update(params)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.plus_sign, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
            R.id.action_add -> {
                Log.d("TasksActivity", "Add button clicked")
                val intent = Intent(this, AddParameterActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }
}