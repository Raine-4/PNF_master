package com.pnfmaster.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.snackbar.Snackbar
import com.pnfmaster.android.databinding.ActivityTasksBinding

class TasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarTasks)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_menu)
            it.title = "我的任务"
        }

        val navView = binding.navView
        navView.setCheckedItem(R.id.navTasks)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navProfile -> jumpToActivity(this, "Profile")
                R.id.navControl -> jumpToActivity(this, "Control")
                R.id.navLogout -> jumpToActivity(this, "Logout")
            }
            true
        }

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 打开左滑菜单
            android.R.id.home -> binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }

    // 左侧滑动菜单中的点击事件，已进入Tasks无需验证是否Login
    private fun jumpToActivity(context: Context, targetActivity: String) {
        when (targetActivity) {
            "Profile" -> {
                val account = intent.getStringExtra("userAccount")
                val id = intent.getIntExtra("userId", -1)
                val profileIntent = Intent(this, ProfileActivity::class.java)
                profileIntent.putExtra("userAccount", account)
                profileIntent.putExtra("userId", id)
                startActivity(profileIntent)
                finish()
            }

            "Control" -> {
                // TODO：目前MainActivity是作为开发者模式存在的，之后可能需要更改要跳转的Activity
                val intent = Intent(this, ControlActivity::class.java)
                startActivity(intent)
                finish()
            }

            "Logout" -> {
                val logOutIntent = Intent(context, LoginActivity::class.java)
                startActivity(logOutIntent)
                finish()
            }
        }
    }
}