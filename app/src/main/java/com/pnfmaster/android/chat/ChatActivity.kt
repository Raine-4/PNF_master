package com.pnfmaster.android.chat

import android.annotation.SuppressLint
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

class ChatActivity : AppCompatActivity() {
    private lateinit var binding : ActivityChatBinding
    private lateinit var chatAdapter : ChatlistAdapter
    private lateinit var rcChatlist : RecyclerView
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Interface
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rcChatlist = binding.rcChatlist
        setSupportActionBar(binding.toolbarChat)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = getString(R.string.app_name)
        }

        val mData = ArrayList<Chatlist>()
        chatAdapter = ChatlistAdapter(this, mData) // Initialize adapter

        // Add a loading message
        val loading = Chatlist("Master: ", "正在初始化...")
        mData.add(loading)
        chatAdapter.update(mData)

//        val initMsg = getString(R.string.initMsg)
        val initMsg = "今天天气怎么样？"

        CoroutineScope(Dispatchers.Main).launch {
            val reply = withContext(Dispatchers.IO) {
                try {
                    Chatlist("Master: ", AIAssistant().GetAnswer(initMsg))
                } catch (e: Exception) {
                    Log.e("ChatActivity", e.toString())
                    Chatlist("Master: ", "Error: $e")
                }
            }
            mData.add(reply)
            chatAdapter.update(mData)
            // chatAdapter.notifyDataSetChanged() // 我也不知道为什么加上这一句就可以自动显示了。
        }

        // Remove the loading message
        mData.removeAt(mData.size-1)
        chatAdapter.update(mData)


        // Set adapter and manager for recyclerView
        val layoutManager = LinearLayoutManager(this)
        rcChatlist.adapter = chatAdapter
        rcChatlist.layoutManager = layoutManager
//        rcChatlist.hasFixedSize() = true

        // Send button
        binding.sendBtn.setOnClickListener {
            val user_ask = binding.etChat.text.toString()
            val newChatlist = Chatlist(getString(R.string.you_), user_ask)
            // Clear EditText
            binding.etChat.setText("")

            // Update data
            mData.add(newChatlist)
            chatAdapter.update(mData)
            rcChatlist.adapter = chatAdapter

            // Add a loading message
            val loading = Chatlist("Master: ", "正在思考中...")
            mData.add(loading)
            chatAdapter.update(mData)

            // Get answer from AI
            CoroutineScope(Dispatchers.Main).launch {
                val reply = withContext(Dispatchers.IO) {
                    try {
                        val ai = AIAssistant()
                        Chatlist("Master: ", ai.GetAnswer(user_ask))
                    } catch (e: Exception) {
                        Log.e("ChatActivity", e.toString())
                        Chatlist("Master: ", "Error")
                    }
                }
                mData.add(reply)
                chatAdapter.update(mData)
            }

            // Remove the loading message
            mData.removeAt(mData.size-1)
            chatAdapter.update(mData)

        }
    } // onCrete

//    private class MyHandler(activity: ChatActivity) : Handler(Looper.getMainLooper()) {
//        // 弱引用防止内存泄漏，并且在ChatActivity被销毁后，MyHandler不会尝试错误地更新UI。
//        private val activityReference = WeakReference(activity)
//        @Suppress("UNCHECKED_CAST")
//        override fun handleMessage(msg: Message) {
//            val activity = activityReference.get() ?: return
//            when (msg.what) {
//                MESSAGE_UPDATE -> {
//                    val mData = msg.obj as List<Chatlist>
//                    activity.chatAdapter.update(mData)
//                    activity.rcChatlist.adapter = activity.chatAdapter
//                }
//            }
//        }
//    }

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