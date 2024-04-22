package com.pnfmaster.android.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pnfmaster.android.MyApplication
import com.pnfmaster.android.R
import com.pnfmaster.android.databinding.ActivityChatBinding
import com.pnfmaster.android.utils.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        // Initialize adapter
        chatAdapter = ChatlistAdapter(this, mData)

//        // Add a loading message
//        val loading1 = Chatlist("PNF Master", "正在初始化...")
//        mData.add(loading1)
//        chatAdapter.update(mData)

        val initMsg = getString(R.string.initMsg)

        // Get answer from AI using Kotlin coroutine
//        CoroutineScope(Dispatchers.Main).launch {
//            val reply = withContext(Dispatchers.IO) {
//                try {
//                    Chatlist("PNF Master", AIAssistant().GetAnswer(initMsg))
//                } catch (e: Exception) {
//                    Log.e("ChatActivity", e.toString())
//                    Chatlist("PNF Master", "Error: $e")
//                }
//            }
//            // Remove the loading message
//            mData.removeAt(mData.size-1)
//
//            mData.add(reply)
//            chatAdapter.update(mData)
//        }

        mData.add(Chatlist("PNF Master", getString(R.string.how_can_I_help)))
        chatAdapter.update(mData)

        // Set adapter and manager for recyclerView
        val layoutManager = LinearLayoutManager(this)
        rcChatlist.adapter = chatAdapter
        rcChatlist.layoutManager = layoutManager
        rcChatlist.setHasFixedSize(true)

        // Send button
        binding.sendBtn.setOnClickListener {
            val input = binding.etChat.text.toString()

            // Check if input is empty
            if (input.isBlank()) {
                Toast.makeText(this, "输入不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the system language is English
            var setLanguage = ""
            if (MyApplication.language == "en") {
                setLanguage = " Reply me in English."
                "Language: English".Toast()
            }  else {
                setLanguage = " 回答请用中文。"
                "Language: Chinese".Toast()
            }

            val userAsk = initMsg + input + setLanguage

            val newChatlist = Chatlist(getString(R.string.you), input)
            // Clear EditText
            binding.etChat.setText("")

            // Update data
            mData.add(newChatlist)
            chatAdapter.update(mData)
            rcChatlist.adapter = chatAdapter

            // Add a loading message
            val loading = Chatlist("PNF Master", "正在思考中...")
            mData.add(loading)
            chatAdapter.update(mData)

            // Get answer from AI
            CoroutineScope(Dispatchers.Main).launch {
                val reply = withContext(Dispatchers.IO) {
                    try {
                        val ai = AIAssistant()
                        Chatlist("PNF Master", ai.GetAnswer(userAsk))
                    } catch (e: Exception) {
                        Log.e("ChatActivity", e.toString())
                        Chatlist("PNF Master", "Error: $e")
                    }
                }
                // Remove the loading message
                mData.removeAt(mData.size-1)

                mData.add(reply)
                chatAdapter.update(mData)
            }

        }
    } // onCrete

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return true
    }

}