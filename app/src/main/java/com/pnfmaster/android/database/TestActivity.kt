package com.pnfmaster.android.database

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pnfmaster.android.databinding.ActivityTestBinding
import java.sql.SQLException

class TestActivity : AppCompatActivity() {

    companion object {
        var isConnected = 0 //用于判断连接是否成功
        var password_receive: String? = null //用于接收数据库查询的返回数据
    }

    private lateinit var binding : ActivityTestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.IDinput
        val password = binding.codeInput

        binding.conn.setBackgroundColor(Color.RED) //默认设成红色

        val handler1 = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (isConnected) {
                    1 -> {
                        binding.conn.text = "网络连接成功"
                        binding.conn.setBackgroundColor(Color.GREEN)
                    }

                    0 -> binding.conn.text = "网络连接失败"
                }
            }
        }

        Thread {
            val msg = Message()
            try {
                connect.setConnection("pnf_master")
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            handler1.sendMessage(msg) //跳转到handler1
        }.start()

        binding.logOnButton.setOnClickListener {
            Thread {
                try {
                    connect.insertUser(
                        username.getText().toString(),
                        password.getText().toString()
                    )
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }.start()
        }

        val handler2 = android.os.Handler {
            if (password_receive == password.getText().toString()) // 判断输入密码与取得的密码是否相同
                Toast.makeText(this@TestActivity, "登陆成功", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this@TestActivity, "密码错误", Toast.LENGTH_SHORT).show()
            false
        }

        binding.logOn.setOnClickListener {
            Thread {
                val msg = Message()
                try {
                    password_receive =
                        connect.queryPassword(username.getText().toString()) // 调用查询语句，获得账号对应的密码
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
                handler2.sendMessage(msg) //跳转到handler2
            }.start()
        }
    }
}

