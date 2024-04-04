package com.pnfmaster.android

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.pnfmaster.android.BtConnection.BluetoothCommunication
import com.pnfmaster.android.BtConnection.BluetoothScanActivity
import com.pnfmaster.android.control.CurrentFragment
import com.pnfmaster.android.control.PositionFragment
import com.pnfmaster.android.control.TorqueFragment
import com.pnfmaster.android.control.VelocityFragment
import com.pnfmaster.android.databinding.ActivityMainBinding
import com.pnfmaster.android.utils.Toast
import java.nio.charset.Charset
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置顶部导航栏
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_menu)
            it.title = getString(R.string.Control)
        }

        // 设置返回回调
        onBackPressedDispatcher.addCallback(this) {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle(getString(R.string.Hint))
                .setMessage(getString(R.string.logout))
                .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                    val logOutIntent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(logOutIntent)
                    finish()
                }
                .setNegativeButton(getString(R.string.No),null)
                .create()
                .show()
        }

        // 设置左滑菜单响应事件
        val navView = binding.navView
        navView.setCheckedItem(R.id.navControl)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navProfile -> jumpToActivity(this, "Profile")
                R.id.navControl -> jumpToActivity(this, "Control")
                R.id.navTasks -> jumpToActivity(this, "Tasks")
                R.id.navLogout -> jumpToActivity(this, "Logout")
            }
            true
        }

        val navHeaderView = binding.navView.getHeaderView(0)
        val userAccount = intent.getStringExtra("userAccount")
        val tvUserName = navHeaderView.findViewById<TextView>(R.id.userName)
        // 如果不是未登录状态，则在nav_header中显示用户名
        if (!MyApplication.isSkipped) {
            tvUserName.text = userAccount
        }









//  --------------------------------------  以下为基础设置  ------------------------------------------

        // 设置底部导航栏
        val bottomNavView = binding.bottomNavigation
        bottomNavView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_position -> { showFragment(PositionFragment())
                    true}
                R.id.nav_velocity -> { showFragment(VelocityFragment())
                    true}
                R.id.nav_current -> { showFragment(CurrentFragment())
                    true}
                R.id.nav_torque -> { showFragment(TorqueFragment())
                    true}
                else -> { false }
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.commit()
    }

    // 右上角三个按钮的点击事件
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 打开左滑菜单
            android.R.id.home -> binding.drawerLayout.openDrawer(GravityCompat.START)
            // 右上角按钮
            R.id.bluetooth -> {
                val device = MyApplication.bluetoothDevice
                Log.d(TAG, "Main: 调用MyApplication.bluetoothDevice = $device")
                if (device == null) {
                    Log.d(TAG, "Device is null. Start the BluetoothScanActivity")
                    val intent = Intent(this, BluetoothScanActivity::class.java)
                    startActivity(intent)
//                    finish()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.currentDevice))
                        .setMessage(getString(R.string.currentDevice) + "：${device.name}\n" +
                                getString(R.string.MacAddress) + "：${device.address}")
                        .setPositiveButton(getString(R.string.Yes),null)
                        .setNegativeButton(getString(R.string.break_connection)) { _, _ ->
                            Log.d(TAG, "Main: onOptionsItemSelected. User cancelled the bluetooth.")
//                            binding.curDevice.text = " ${getString(R.string.None)}"
                            MyApplication.bluetoothSocket!!.close()
                            MyApplication.bluetoothDevice = null
                        }
                        .create()
                        .show()
                }
            }

            R.id.help -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.intro))
                    .setMessage(R.string.usage)
                    .setPositiveButton(getString(R.string.Yes)) { _, _ -> }
                    .create()
                    .show()
            }

            R.id.developerMode -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.Hint))
                    .setMessage(getString(R.string.developerMode))
                    .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                        val intent = Intent(this, ControlActivity::class.java)
                        startActivity(intent)
                    }
                    .setNegativeButton(getString(R.string.No),null)
                    .create()
                    .show()
            }

            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    // 左侧滑动菜单中的点击事件
    private fun jumpToActivity(context: Context, targetActivity: String) {
        when (targetActivity) {
            "Profile" -> {
                if (!judgeIfSkipped()) {
                    val account = intent.getStringExtra("userAccount")
                    val profileIntent = Intent(this, ProfileActivity::class.java)
                    profileIntent.putExtra("userAccount", account)
                    startActivity(profileIntent)
                }
            }
            "Control" -> {
                // 如果当前界面不在Control界面中，则跳转至Control界面
                // TODO：目前MainActivity是作为开发者模式存在的，之后可能需要更改要跳转的Activity
                if (javaClass.simpleName != ControlActivity::class.java.simpleName) {
                    val intent = Intent(this, ControlActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            "Tasks" -> {
                if (!judgeIfSkipped()){
                    val account = intent.getStringExtra("userAccount")
                    val id = intent.getIntExtra("userId", -1)
                    val tasksIntent = Intent(this, TasksActivity::class.java)
                    tasksIntent.putExtra("userAccount", account)
                    tasksIntent.putExtra("userId", id)
                    startActivity(tasksIntent)
                }
            }
            "Logout" -> { // 返回登录菜单
                if (!judgeIfSkipped()) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(getString(R.string.Hint))
                        .setMessage(getString(R.string.logout))
                        .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                            val logOutIntent = Intent(context, LoginActivity::class.java)
                            startActivity(logOutIntent)
                            finish()
                        }
                        .setNegativeButton(getString(R.string.No),null)
                        .create()
                        .show()
                } else {
                    val logOutIntent = Intent(context, LoginActivity::class.java)
                    startActivity(logOutIntent)
                    finish()
                }
            }
        }
    }

    // 判断用户是否跳过登录，如果跳过则返回 true
    private fun judgeIfSkipped(): Boolean {
        val isSkipped = MyApplication.isSkipped
        if (isSkipped) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.Hint))
                .setMessage(getString(R.string.notLoginYet))
                .setPositiveButton(getString(R.string.Yes)) { _, _ ->
                    MyApplication.isSkipped = true
                    val backIntent = Intent(this, LoginActivity::class.java)
                    startActivity(backIntent)
                    finish()
                }
                .setNegativeButton(getString(R.string.No), null)
                .create()
                .show()
            return true
        }
        return false
    }

}