package com.sujanix.cruxmdm.features.app_catalog.presentation.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sujanix.cruxmdm.databinding.ItemRequestedAppBinding
import com.sujanix.cruxmdm.features.app_catalog.data.data_source.local.entity.RequestedApp
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application

class RequestedAppAdapter(
    private val context: Context
): ListAdapter<RequestedApp, RequestedAppAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(binding: ItemRequestedAppBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(requestedApp: RequestedApp) {
//            binding.apply {
//                tvAppName.text = requestedApp.name
//                tvAppType.text = requestedApp.packageName
//            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<RequestedApp>() {
        override fun areItemsTheSame(oldItem: RequestedApp, newItem: RequestedApp) =
            oldItem.packageName == newItem.packageName

        override fun areContentsTheSame(oldItem: RequestedApp, newItem: RequestedApp) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}