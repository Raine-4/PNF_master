package com.pnfmaster.android

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pnfmaster.android.adapters.ParamsAdapter
import com.pnfmaster.android.chat.ChatActivity
import com.pnfmaster.android.databinding.ActivityTasksBinding

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
//            it.title = getString(R.string.my_param)
        }

        // Create empty data list and adapter
        val paramsList = ArrayList<ParamsGroup>()
        paramsAdapter = ParamsAdapter(paramsList)
        // TODO: 改成从数据库里读取数据
        paramsList.add(ParamsGroup(20, 50, 8000, 30))
        paramsList.add(ParamsGroup(25, 55, 8100, 35))
        paramsAdapter.update(paramsList)

        val layoutManager = LinearLayoutManager(this)
        rvParams.adapter = paramsAdapter
        rvParams.layoutManager = layoutManager

        if (paramsList.isNotEmpty()) {
            // Close the empty layout
            val emptyLayout = findViewById<View>(R.id.no_params_layout)
            emptyLayout.visibility = View.INVISIBLE

            val paramsAdapter = ParamsAdapter(paramsList)
            binding.rvParams.adapter = paramsAdapter

        }


        // Open Chatting Activity
        binding.fab.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
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
                val intent = Intent(this, AddParameterActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }
}