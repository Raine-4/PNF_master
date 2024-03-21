package com.pnfmaster.android.adapters

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pnfmaster.android.BtConnection.BluetoothScanActivity
import com.pnfmaster.android.MyApplication
import com.pnfmaster.android.databinding.ItemDeviceRvBinding
import java.util.*

/**
 * Ble蓝牙适配器
 */
class btDeviceAdapter(
    private val mDevices: List<BluetoothDevice>
) : RecyclerView.Adapter<btDeviceAdapter.ViewHolder>() {

    private var mOnItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(mOnItemClickListener: BluetoothScanActivity) {
        this.mOnItemClickListener = mOnItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(ItemDeviceRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        viewHolder.binding.itemDevice.setOnClickListener { v ->
            if (mOnItemClickListener != null) {
                mOnItemClickListener!!.onItemClick(v, viewHolder.adapterPosition)
            }
        }
        return viewHolder
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val btDevice: BluetoothDevice = mDevices[position]
        val socket = MyApplication.bluetoothSocket
        Log.d("adapter", "name is ${btDevice.name}")
        //设备名称
        holder.binding.tvDeviceName.text = btDevice.name
        //Mac地址
        holder.binding.tvMacAddress.text = btDevice.address
        // 信号强度
        if (socket != null) {
            holder.binding.tvRssi.text = socket.remoteDevice.bondState.toString()
        }
    }

    override fun getItemCount() = mDevices.size

    class ViewHolder(itemView: ItemDeviceRvBinding) : RecyclerView.ViewHolder(itemView.root) {
        var binding: ItemDeviceRvBinding
        init { binding = itemView }
    }
}