package com.sujanix.cruxmdm.features.content_management.presentation.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.ItemFolderLayoutBinding
import com.sujanix.cruxmdm.features.content_management.data.model.response.shared_file.Children
import com.sujanix.cruxmdm.features.content_management.data.model.response.shared_file.Data

class ContentFolderAdapter(
    private val context: Context,
    private val listener: Listener
): ListAdapter<Data, ContentFolderAdapter.ViewHolder>(DiffCallback()) {

    private lateinit var deviceID: String
    private val TAG: String = "ContentFolderAdapter"

    inner class ViewHolder(
        private val binding: ItemFolderLayoutBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Data) {
            binding.apply {

                val fileCount = if(data.shared_with.any { it.device_id == deviceID }) data.children.size.toString()
                else filterFileByDeviceId(deviceID, data.children).size.toString()

                tvFolderName.text = data.name
                tvFileCount.text = context.getString(R.string.files_count, fileCount)

                root.setOnClickListener {
                    Log.d(TAG, "bind: ${data.name} ${data.children.size}")
                    listener.onFolderClick(data)
                }
            }
        }
    }

    private fun filterFileByDeviceId(deviceId: String, files: List<Children>): List<Children> {
        return files.filter { file ->
            file.shared_with.any { it.device_id == deviceId }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFolderLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentApp = getItem(position)
        holder.bind(currentApp)
    }

    fun setDeviceId(deviceId: String) {
        deviceID = deviceId
    }

    class DiffCallback : DiffUtil.ItemCallback<Data>() {
        override fun areItemsTheSame(oldItem: Data, newItem: Data) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Data, newItem: Data) =
            oldItem == newItem
    }

    interface Listener {
        fun onFolderClick(data: Data)
    }
}