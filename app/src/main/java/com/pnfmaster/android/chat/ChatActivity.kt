package com.pnfmaster.android.chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pnfmaster.android.MyApplication.Companion.context
import com.pnfmaster.android.R
import com.pnfmaster.android.databinding.ActivityChatBinding
import com.pnfmaster.android.utils.Toast
import java.lang.ref.WeakReference

class ChatActivity : AppCompatActivity() {
    private lateinit var binding : ActivityChatBinding
    private lateinit var chatAdapter : ChatlistAdapter
    private lateinit var rc_chatlist : RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Interface
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rc_chatlist = binding.rcChatlist
        setSupportActionBar(binding.toolbarChat)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = getString(R.string.app_name)
        }

        // Initialize user background and generate greeting message
        val mData = ArrayList<Chatlist>()
        val initMsg = getString(R.string.initMsg)
        Thread {
            val reply = try {
                runOnUiThread {
                    "正在加载中，请稍后..".Toast(Toast.LENGTH_LONG)
                }
                Chatlist("Master: ", AIAssistant().GetAnswer(initMsg))
            } catch (e: Exception) {
                Log.e("ChatActivity", e.toString())
                Chatlist("Master: ", "Error")
            }
            mData.add(reply)
        }.start()
//        val firstChat = Chatlist("Master: ", getString(R.string.how_can_I_help))
//        mData.add(firstChat)

        // Set adapter and manager for recyclerView
        chatAdapter = ChatlistAdapter(this, mData)
        val layoutManager = LinearLayoutManager(this)
        rc_chatlist.adapter = chatAdapter
        rc_chatlist.layoutManager = layoutManager
//        rc_chatlist.hasFixedSize() = true

        // Send button
        binding.sendBtn.setOnClickListener {
            val user_ask = binding.etChat.text.toString()
            val newChatlist = Chatlist(getString(R.string.you_), user_ask)
            // Clear EditText
            binding.etChat.setText("")

            // 设置TextView的drawableStart为用户头像
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_username)
            val textview = findViewById<TextView>(R.id.speaker_name)
            textview.setCompoundDrawablesWithIntrinsicBounds(drawable,null,null,null)
            mData.add(newChatlist)

            // Update data
            chatAdapter.update(mData)
            rc_chatlist.adapter = chatAdapter

            // Get answer from AI
            Thread {
                val reply = try {
                    val ai = AIAssistant()
                    runOnUiThread {
                        "正在思考中...".Toast(Toast.LENGTH_LONG)
                    }
                    Chatlist("Master: ", ai.GetAnswer(user_ask))
                } catch (e: Exception) {
                    Log.e("ChatActivity", e.toString())
                    Chatlist("Master: ", "Error")
                }
                mData.add(reply)

                // 子线程中不能直接更新UI，必须发给handler在主线程中完成
                val msg = Message()
                msg.what = MESSAGE_UPDATE
                msg.obj = mData
                MyHandler(this).sendMessage(msg)
            }.start()
        }
    } // onCrete

    private class MyHandler(activity: ChatActivity) : Handler(Looper.getMainLooper()) {
        // 弱引用防止内存泄漏，并且在ChatActivity被销毁后，MyHandler不会尝试错误地更新UI。
        private val activityReference = WeakReference(activity)
        @Suppress("UNCHECKED_CAST")
        override fun handleMessage(msg: Message) {
            val activity = activityReference.get() ?: return
            when (msg.what) {
                MESSAGE_UPDATE -> {
                    // 设置TextView的drawableStart为AI头像
                    val drawable = ContextCompat.getDrawable(activity, R.drawable.ic_robot)
                    val textview = activity.findViewById<TextView>(R.id.speaker_name)
                    textview?.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

                    val mData = msg.obj as List<Chatlist>
                    activity.chatAdapter.update(mData)
                    activity.rc_chatlist.adapter = activity.chatAdapter
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return true
    }

    companion object {
        const val MESSAGE_UPDATE = 1
    }
}