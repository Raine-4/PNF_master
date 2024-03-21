package com.pnfmaster.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.pnfmaster.android.database.MyDatabaseHelper
import com.pnfmaster.android.databinding.ActivityProfileBinding

class ProfileActivity : BaseActivity() {
    private lateinit var binding : ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarProfile)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(R.string.Profile)
        }

        binding.personInfo.setOnClickListener {
            // todo
        }

        // 创建数据库
        val dbHelper = MyDatabaseHelper(this, "information.db", MyApplication.DB_VERSION)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }
}