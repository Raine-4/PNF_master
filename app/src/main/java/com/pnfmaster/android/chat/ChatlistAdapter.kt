package com.pnfmaster.android.chat

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pnfmaster.android.R
import com.pnfmaster.android.utils.Toast

@SuppressLint("NotifyDataSetChanged")
class ChatlistAdapter(val context: Context, private var mData: List<Chatlist>) :
    RecyclerView.Adapter<ChatlistAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val speakerName: TextView = view.findViewById(R.id.speaker_name)
        val speakContent: TextView = view.findViewById(R.id.speak_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_item, parent, false)
        val viewHolder = ViewHolder(view)

        viewHolder.speakContent.setOnClickListener {
            // 点击复制
            val text = viewHolder.speakContent.text.toString()
            val clipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", text)
            clipboardManager.setPrimaryClip(clipData)
            context.getString(R.string.copied_to_clipboard).Toast()
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mData[position]
        holder.speakerName.text = data.speakerName
        holder.speakContent.text = data.speakContent
        // Set drawableStartCompat based on the speaker
        val drawable = if (data.speakerName.startsWith("PNF Master")) {
            ContextCompat.getDrawable(context, R.drawable.ic_robot)
        } else {
            ContextCompat.getDrawable(context, R.drawable.ic_username)
        }
        holder.speakerName.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }

    override fun getItemCount(): Int = mData.size

    fun update(newData: List<Chatlist>) {
        mData = newData
        notifyDataSetChanged()
    }

}

