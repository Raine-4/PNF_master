package com.pnfmaster.android.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pnfmaster.android.R
import com.pnfmaster.android.databinding.ActivityChatBinding

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

        // Initialize Greeting Message
        val mData = ArrayList<Chatlist>()
        val firstChat = Chatlist("PNFMaster: ", "你好！我是PNF Master，请问你有什么需要帮助的呢？")
        mData.add(firstChat)

        // Set adapter and manager for recyclerView
        chatAdapter = ChatlistAdapter(this, mData)
        val layoutManager = LinearLayoutManager(this)
        rc_chatlist.adapter = chatAdapter
        rc_chatlist.layoutManager = layoutManager
//        rc_chatlist.hasFixedSize() = true

        // Send button
        binding.sendBtn.setOnClickListener {
            val user_ask = binding.etChat.text.toString()
            val newChatlist = Chatlist("User: ", user_ask)
            mData.add(newChatlist)
            // Update data
            chatAdapter.update(mData)
            rc_chatlist.adapter = chatAdapter
            // Clear EditText
            binding.etChat.setText("")

            // Get answer from AI
            Thread {
                val reply = try {
                    val ai = AIAssistant()
                    Chatlist("PNFMaster: ", ai.GetAnswer(user_ask))
                } catch (e: Exception) {
                    Log.e("ChatActivity", e.toString())
                    Chatlist("PNFMaster: ", "Error")
                }
                mData.add(reply)

                // 子线程中不能直接更新UI，必须发给handler在主线程中完成
                val msg = Message()
                msg.what = MESSAGE_UPDATE
                msg.obj = mData
                myHandler().sendMessage(msg)
            }.start()
        }
    } // onCrete

    @SuppressLint("HandlerLeak")
    inner class myHandler : Handler(Looper.getMainLooper()) {
        @Suppress("UNCHECKED_CAST")
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_UPDATE ->  {
                    val mData = msg.obj as List<Chatlist>
                    chatAdapter.update(mData)
                    rc_chatlist.adapter = chatAdapter
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