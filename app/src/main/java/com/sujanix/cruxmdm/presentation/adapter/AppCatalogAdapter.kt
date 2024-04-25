package com.sujanix.cruxmdm.presentation.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sujanix.cruxmdm.data.model.Application
import com.sujanix.cruxmdm.databinding.ItemAppCatalogDetailBinding
import com.sujanix.cruxmdm.util.Constant.APP_CATEGORY_ALL_APPS
import com.sujanix.cruxmdm.util.Constant.APP_CATEGORY_INSTALLED_APPS
import com.sujanix.cruxmdm.util.Constant.APP_CATEGORY_UPDATE_APPS
import com.sujanix.cruxmdm.util.visible

class AppCatalogAdapter(
    private val context: Context,
    private val category: String
): ListAdapter<Application, AppCatalogAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemAppCatalogDetailBinding
    ): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                btnInstall.setOnClickListener {
                    Log.d("FATAL", "Install: triggered")
                }
                btnOpen.setOnClickListener {
                    Log.d("FATAL", "Open: triggered")
                }
                btnUninstall.setOnClickListener {
                    Log.d("FATAL", "Uninstall: triggered")
                }
                btnUpdate.setOnClickListener {
                    Log.d("FATAL", "Update: triggered")
                }

                when (category) {
                    APP_CATEGORY_ALL_APPS -> {
                        btnUpdate.visible(false)
                        btnInstall.visible(false)
                    }

                    APP_CATEGORY_INSTALLED_APPS -> {
                        btnUpdate.visible(false)
                        btnInstall.visible(false)
                    }

                    APP_CATEGORY_UPDATE_APPS -> {
                        btnOpen.visible(false)
                        btnInstall.visible(false)
                    }
                }
            }
        }

        fun bind(application: Application){
            binding.apply {
                if (application.iconUrl != null) {
                    Glide.with(context)
                        .load(application.iconUrl)
                        .into(ivAppImage)
//                    ivAppImage.setImageDrawable(application.icon)
                }
                tvAppName.text = application.name
                tvAppType.text = application.type
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Application>() {
        override fun areItemsTheSame(oldItem: Application, newItem: Application) =
            oldItem.pkg == newItem.pkg

        override fun areContentsTheSame(oldItem: Application, newItem: Application) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppCatalogDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentApp = getItem(position)
        holder.bind(currentApp)
    }
}