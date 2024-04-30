package com.pnfmaster.android.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pnfmaster.android.AddParameterActivity
import com.pnfmaster.android.MyApplication
import com.pnfmaster.android.ParamsGroup
import com.pnfmaster.android.R

/**
 * Adapter for the RecyclerView that displays ParamsGroup items.
 * @property paramsList MutableList<ParamsGroup> The list of ParamsGroup items to display.
 */
class ParamsAdapter(var paramsList: MutableList<ParamsGroup>) :
    RecyclerView.Adapter<ParamsAdapter.ViewHolder>() {

    /**
     * ViewHolder for the ParamsGroup items.
     * @property paramsTitle TextView The title of the ParamsGroup.
     * @property lowerLimitValue TextView The lower limit value of the ParamsGroup.
     * @property upperLimitValue TextView The upper limit value of the ParamsGroup.
     * @property positionValue TextView The position value of the ParamsGroup.
     * @property timeValue TextView The time value of the ParamsGroup.
     * @property radioButton ImageView The radio button for the ParamsGroup.
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val paramsTitle: TextView = view.findViewById(R.id.paramsTitle)
        val lowerLimitValue: TextView = view.findViewById(R.id.lowerLimitValue)
        val upperLimitValue: TextView = view.findViewById(R.id.upperLimitValue)
        val positionValue: TextView = view.findViewById(R.id.positionValue)
        val timeValue: TextView = view.findViewById(R.id.timeValue)
        val radioButton : ImageView = view.findViewById(R.id.radioButton)
    }

    /**
     * Creates a new ViewHolder for a ParamsGroup item.
     * @return ViewHolder The newly created ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_params_rv, parent, false)
        return ViewHolder(view)
    }

    /**
     * Binds a ViewHolder to a ParamsGroup item.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val paramsGroup = paramsList[position]
        holder.paramsTitle.text = paramsGroup.title
        holder.lowerLimitValue.text = paramsGroup.lowerLimit.toString()
        holder.upperLimitValue.text = paramsGroup.upperLimit.toString()
        holder.positionValue.text = paramsGroup.motorPosition.toString()
        holder.timeValue.text = paramsGroup.trainingTime.toString()

        // Set onClickListener for the title, which starts AddParameterActivity with the ParamsGroup's details.
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

        // Set onClickListener for the radio button, which saves the selected ParamsGroup's id to SharedPreferences.
        holder.radioButton.setOnClickListener {
            holder.radioButton.setImageResource(R.drawable.ic_radio_button_checked)
            MyApplication.id = paramsGroup.id
            val sharedPreferences = holder.itemView.context.getSharedPreferences("RadioBtnState", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("selectedRadioButton", paramsGroup.id)
            editor.apply()
        }

        // Check SharedPreferences for the selected ParamsGroup's id and set the radio button's image accordingly.
        val sharedPreferences = holder.itemView.context.getSharedPreferences("RadioBtnState", Context.MODE_PRIVATE)
        val selectedRadioButtonId = sharedPreferences.getInt("selectedRadioButton", -1)
        if (paramsGroup.id == selectedRadioButtonId) {
            holder.radioButton.setImageResource(R.drawable.ic_radio_button_checked)
        } else {
            holder.radioButton.setImageResource(R.drawable.ic_radio_button_unchecked)
        }
    }

    /**
     * Returns the number of ParamsGroup items.
     * @return Int The number of ParamsGroup items.
     */
    override fun getItemCount() = paramsList.size

    /**
     * Updates the list of ParamsGroup items and notifies the adapter of the change.
     * @param newData MutableList<ParamsGroup> The new list of ParamsGroup items.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun update(newData: MutableList<ParamsGroup>) {
        paramsList = newData
        notifyDataSetChanged()
    }
}