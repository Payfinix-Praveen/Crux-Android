package com.sujanix.cruxmdm.presentation.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sujanix.cruxmdm.data.model.Application
import com.sujanix.cruxmdm.data.model.DeviceDetail
import com.sujanix.cruxmdm.databinding.ItemAppCatalogDetailBinding
import com.sujanix.cruxmdm.databinding.ItemDeviceDetailBinding
import com.sujanix.cruxmdm.util.visible

class DeviceDetailAdapter(
    private val context: Context
): ListAdapter<DeviceDetail, DeviceDetailAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemDeviceDetailBinding
    ): RecyclerView.ViewHolder(binding.root) {

//        init {
//            binding.apply {
//
//            }
//        }

        fun bind(detail: DeviceDetail){
            binding.apply {
                ivDeviceDetail.setImageDrawable(context.getDrawable(detail.icon))
                tvDeviceDetailTitle.text = detail.title
                tvDeviceDetail.text = detail.detail
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DeviceDetail>() {
        override fun areItemsTheSame(oldItem: DeviceDetail, newItem: DeviceDetail) =
            oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: DeviceDetail, newItem: DeviceDetail) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeviceDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentApp = getItem(position)
        holder.bind(currentApp)
    }
}