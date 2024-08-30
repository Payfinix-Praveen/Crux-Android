package com.sujanix.cruxmdm.features.notification_policyActivity.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sujanix.cruxmdm.features.notification_policyActivity.data.data_source.local.Notification
import com.sujanix.cruxmdm.databinding.ItemNotificationBinding

class NotificationsAdapter:
    ListAdapter<Notification, NotificationsAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemNotificationBinding
    ): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                //check for unread notification and activities, highlight them if any
            }
        }

        fun bind(notification: Notification){
            binding.apply {
                tvNotificationTitle.text = notification.title
                tvDateTime.text = notification.createdDateFormatted
                tvDescription.text = notification.message
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentApp = getItem(position)
        holder.bind(currentApp)
    }
}