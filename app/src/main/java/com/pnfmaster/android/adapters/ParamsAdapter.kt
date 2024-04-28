package com.pnfmaster.android.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pnfmaster.android.ParamsGroup
import com.pnfmaster.android.R

class ParamsAdapter(private var paramsList: List<ParamsGroup>) :
    RecyclerView.Adapter<ParamsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val paramsTitle: TextView = view.findViewById(R.id.paramsTitle)
        val lowerLimitValue: TextView = view.findViewById(R.id.lowerLimitValue)
        val upperLimitValue: TextView = view.findViewById(R.id.upperLimitValue)
        val positionValue: TextView = view.findViewById(R.id.positionValue)
        val timeValue: TextView = view.findViewById(R.id.timeValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_params_rv, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.paramsTitle.setOnClickListener {
            // TODO: 设置点击事件
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val paramsGroup = paramsList[position]
        holder.paramsTitle.text = paramsGroup.title
        holder.lowerLimitValue.text = paramsGroup.lowerLimit.toString()
        holder.upperLimitValue.text = paramsGroup.upperLimit.toString()
        holder.positionValue.text = paramsGroup.motorPosition.toString()
        holder.timeValue.text = paramsGroup.trainingTime.toString()
    }

    override fun getItemCount() = paramsList.size

    @SuppressLint("NotifyDataSetChanged")
    fun update(newData: List<ParamsGroup>) {
        paramsList = newData
        notifyDataSetChanged()
    }
}