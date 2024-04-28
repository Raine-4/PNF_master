package com.pnfmaster.android

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.pnfmaster.android.chat.ChatActivity
import com.pnfmaster.android.databinding.ActivityTasksBinding

class TasksActivity : BaseActivity() {

    private lateinit var binding: ActivityTasksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = getString(R.string.my_param)
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