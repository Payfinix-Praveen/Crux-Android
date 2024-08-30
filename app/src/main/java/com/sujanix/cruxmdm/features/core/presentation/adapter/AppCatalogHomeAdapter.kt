package com.sujanix.cruxmdm.features.core.presentation.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application
import com.sujanix.cruxmdm.databinding.ItemAppCatalogBinding

class AppCatalogHomeAdapter(
    private val context: Context
): ListAdapter<Application, AppCatalogHomeAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemAppCatalogBinding
    ): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
//                btnInstall.setOnClickListener {
//                    Log.d("FATAL", "Install: triggered")
//                }
//                btnOpen.setOnClickListener {
//                    Log.d("FATAL", "Open: triggered")
//                }
//                btnUninstall.setOnClickListener {
//                    Log.d("FATAL", "Uninstall: triggered")
//                }
//                btnUpdate.setOnClickListener {
//                    Log.d("FATAL", "Update: triggered")
//                }
//
//                when (category) {
//                    "ALL_APPS" -> {
//                        btnUpdate.visible(false)
//                        btnInstall.visible(false)
//                    }
//
//                    "INSTALLED_APPS" -> {
//                        btnUpdate.visible(false)
//                        btnInstall.visible(false)
//                    }
//
//                    "UPDATE_APPS" -> {
//                        btnOpen.visible(false)
//                        btnInstall.visible(false)
//                    }
//                }
            }
        }

        fun bind(application: Application){
            binding.apply {
                if (application.iconUrl != null) {
                    try {
                        Glide.with(context)
                            .load(application.iconUrl)
                            .into(ivAppImage)
                    }catch (e: Exception){
                        e.printStackTrace()
                        Log.d("TAG", "bind: ${e.message}")
                    }

                }
                tvAppName.text = application.name
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
        val binding = ItemAppCatalogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentApp = getItem(position)
        holder.bind(currentApp)
    }
}