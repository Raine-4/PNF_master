package com.pnfmaster.android.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pnfmaster.android.AddParameterActivity
import com.pnfmaster.android.ParamsGroup
import com.pnfmaster.android.R

class ParamsAdapter(var paramsList: MutableList<ParamsGroup>) :
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
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val paramsGroup = paramsList[position]
        holder.paramsTitle.text = paramsGroup.title
        holder.lowerLimitValue.text = paramsGroup.lowerLimit.toString()
        holder.upperLimitValue.text = paramsGroup.upperLimit.toString()
        holder.positionValue.text = paramsGroup.motorPosition.toString()
        holder.timeValue.text = paramsGroup.trainingTime.toString()

        holder.paramsTitle.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddParameterActivity::class.java)
            intent.putExtra("flag","EDIT")
            intent.putExtra("paramsId", paramsGroup.id)
            intent.putExtra("title", paramsGroup.title)
            intent.putExtra("lowerlimit", paramsGroup.lowerLimit)
            intent.putExtra("upperlimit", paramsGroup.upperLimit)
            intent.putExtra("position", paramsGroup.motorPosition)
            intent.putExtra("time", paramsGroup.trainingTime)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = paramsList.size

    @SuppressLint("NotifyDataSetChanged")
    fun update(newData: MutableList<ParamsGroup>) {
        paramsList = newData
        notifyDataSetChanged()
    }
}

//class ParamsAdapter(
//        private var paramsList: List<ParamsGroup>,
//        private val paramsGroupId: Int,
//        private val title: String,
//        private val lowerlimit: Int,
//        private val upperlimit: Int,
//        private val position: Int,
//        private val time: Int,
//    ) : RecyclerView.Adapter<ParamsAdapter.ViewHolder>() {
//
//    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val paramsTitle: TextView = view.findViewById(R.id.paramsTitle)
//        val lowerLimitValue: TextView = view.findViewById(R.id.lowerLimitValue)
//        val upperLimitValue: TextView = view.findViewById(R.id.upperLimitValue)
//        val positionValue: TextView = view.findViewById(R.id.positionValue)
//        val timeValue: TextView = view.findViewById(R.id.timeValue)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        Log.d("Adapter", "paramsGroupId: $paramsGroupId")
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_params_rv, parent, false)
//        val viewHolder = ViewHolder(view)
//        viewHolder.paramsTitle.setOnClickListener {
//            val intent = Intent(parent.context, AddParameterActivity::class.java)
//            intent.putExtra("flag","EDIT")
//            intent.putExtra("paramsId", paramsGroupId)
//
//            intent.putExtra("title", title)
//            intent.putExtra("lowerlimit", lowerlimit)
//            intent.putExtra("upperlimit", upperlimit)
//            intent.putExtra("position", position)
//            intent.putExtra("time", time)
//
//            parent.context.startActivity(intent)
//        }
//        return viewHolder
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val paramsGroup = paramsList[position]
//        holder.paramsTitle.text = paramsGroup.title
//        holder.lowerLimitValue.text = paramsGroup.lowerLimit.toString()
//        holder.upperLimitValue.text = paramsGroup.upperLimit.toString()
//        holder.positionValue.text = paramsGroup.motorPosition.toString()
//        holder.timeValue.text = paramsGroup.trainingTime.toString()
//    }
//
//    override fun getItemCount() = paramsList.size
//
//    @SuppressLint("NotifyDataSetChanged")
//    fun update(newData: List<ParamsGroup>) {
//        paramsList = newData
//        notifyDataSetChanged()
//    }
//}