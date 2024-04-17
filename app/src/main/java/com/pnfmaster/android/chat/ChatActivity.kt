package com.pnfmaster.android.chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pnfmaster.android.R
import com.pnfmaster.android.databinding.ActivityChatBinding
import org.json.JSONException
import java.io.IOException

class ChatActivity : AppCompatActivity() {
    private lateinit var binding : ActivityChatBinding
    private lateinit var chatAdapter : ChatlistAdapter
    private lateinit var rc_chatlist : RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Interface
        binding = ActivityChatBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        rc_chatlist = binding.rcChatlist
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Greeting Message
        val mData = ArrayList<Chatlist>()
        val firstChat = Chatlist("PMaster: ", "你好！我是人工智能助理，请问你有什么需要帮助的呢？")
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

            // Get answer from AI
            Thread {
                var reply: Chatlist
                try {
                    val ai = AIAssistant()
                    reply = Chatlist("PNFMaster: ", ai.GetAnswer(user_ask))
                } catch (e: Exception) {
                    reply = Chatlist("PNFMaster: ", "Error")
                }
                mData.add(reply)
                chatAdapter.update(mData)

                // 子线程中不能直接更新UI，必须发给handler在主线程中完成
                val msg = Message()
                msg.what = MESSAGE_UPDATE
                myHandler().sendMessage(msg)
            }.start()
        }
    } // onCrete

    inner class myHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_UPDATE ->  rc_chatlist.adapter = chatAdapter
            }
        }
    }

    companion object {
        const val MESSAGE_UPDATE = 1
    }
}