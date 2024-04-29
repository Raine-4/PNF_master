package com.pnfmaster.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pnfmaster.android.adapters.ParamsAdapter
import com.pnfmaster.android.chat.ChatActivity
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
        paramsAdapter = ParamsAdapter(paramsList)

        val emptyLayout = findViewById<View>(R.id.no_params_layout)

        lifecycleScope.launch {
            val paramsGroupList = withContext(Dispatchers.IO) {
                connect.queryParams()
            }
            withContext(Dispatchers.Main) {
                if (paramsGroupList.isNotEmpty()) {
                    // Close the empty layout
                    Log.d("TasksActivity", "Parameters found")
                    emptyLayout.visibility = View.INVISIBLE
                } else {
                    // Show the empty layout
                    emptyLayout.visibility = View.VISIBLE
                    Log.d("TasksActivity", "No parameters found")
                }
                paramsAdapter.update(paramsGroupList)
            }
        }

//        模拟添加数据的操作
//        paramsList.add(ParamsGroup("Parameters Set 1",20, 50, 8000, 30))
//        paramsList.add(ParamsGroup("Parameters Set 2",25, 55, 8100, 35))
//        paramsAdapter.update(paramsList)

        val layoutManager = LinearLayoutManager(this)
        rvParams.adapter = paramsAdapter
        rvParams.layoutManager = layoutManager

        // Open Chatting Activity
        binding.fab.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        loadData()
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