package com.sujanix.cruxmdm.features.app_catalog.presentation.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sujanix.cruxmdm.features.app_catalog.data.model.Application
import com.sujanix.cruxmdm.databinding.ItemAppCatalogDetailBinding
import com.sujanix.cruxmdm.features.app_catalog.data.repository.AppCatalogRepository
import com.sujanix.cruxmdm.features.app_catalog.utlis.InstallUtils
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.core.utlis.Constant.APP_CATEGORY_ALL_APPS
import com.sujanix.cruxmdm.features.core.utlis.Constant.APP_CATEGORY_INSTALLED_APPS
import com.sujanix.cruxmdm.features.core.utlis.Constant.APP_CATEGORY_UPDATE_APPS
import com.sujanix.cruxmdm.features.core.utlis.DownloadListener
import com.sujanix.cruxmdm.features.core.utlis.DownloadTask
import com.sujanix.cruxmdm.features.core.utlis.Resource
import com.sujanix.cruxmdm.features.core.utlis.openApplication
import com.sujanix.cruxmdm.features.core.utlis.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AppCatalogAdapter(
    private val context: Context,
    private val category: String,
    private val repository: AppCatalogRepository?
): ListAdapter<Application, AppCatalogAdapter.ViewHolder>(DiffCallback()),
DownloadListener {

    private var enterpriseID: String = ""
    private val TAG: String = "AppCatalogAdapter"
    private val downloadTask = DownloadTask(context)
    private val downloadProgressChannel = Channel<Int>(Channel.CONFLATED)
    private val downloadCompletion = Channel<Pair<Boolean, String>>(Channel.CONFLATED)

    inner class ViewHolder(
        private val binding: ItemAppCatalogDetailBinding
    ): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {

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

        fun bind(application: Application, position: Int){
            Log.d(TAG, "bind: $application")
            binding.apply {
                if (application.iconUrl != null) {
                    Glide.with(context)
                        .load(application.iconUrl)
                        .into(ivAppImage)
//                    ivAppImage.setImageDrawable(application.icon)
                }

                Log.d(TAG, "bind: ${application.name} ${application.isInstalled}")

                btnUninstall.visible(application.isInstalled)

                if(application.type == "Self Hosted Private App") {
                    if(application.isInstalled){
                        if(application.isUpdateAvailable){
                            btnUpdate.visible(true)
                        } else {
                            btnInstall.visible(false)
                            btnOpen.visible(true)
                        }
                    } else {
                        if(application.apkPath?.isNotEmpty() == true) {
                            btnInstall.visible(true)
                            btnOpen.visible(false)
                        } else {
                            btnInstall.visible(false)
                            btnOpen.visible(false)
                            btnDownload.visible(true)
                        }
                    }
                } else {
                    btnInstall.visible(!application.isInstalled)
                    if(application.isInstalled) btnOpen.visible(true)
                }

//                if(application.isUpdateAvailable){
//                    btnUpdate.visible(true)
//                } else {
//                    btnUpdate.visible(false)
//                }

                tvAppName.text = application.name
                tvAppType.text = application.type

                btnInstall.setOnClickListener {
                    Log.d("FATAL", "install: ${application.apkPath}")
                    if(application.type == "Self Hosted Private App") {
                        if (application.apkPath != null) {
                            InstallUtils.showInstallOption(context, application.apkPath)
                        }
                    } else {
                        InstallUtils.openPlayStore(context, application.pkg!!)
                    }
                }
                btnUpdate.setOnClickListener {
                    Log.d("FATAL", "install: ${application.apkPath}, ${application.pkg}")
                    if(application.apkPath != null) {
                        InstallUtils.showInstallOption(context, application.apkPath)
                    }
                }
                btnOpen.setOnClickListener {
                    Log.d("FATAL", "Open: ${application.pkg}")
                    openApplication(
                        context,
                        application.pkg!!
                    )
                }
                btnUninstall.setOnClickListener {
                    Log.d("FATAL", "Uninstall: triggered")
                    InstallUtils.uninstallApp(context, application.pkg!!)
                }

                btnDownload.setOnClickListener {
                    btnDownload.text = "Downloading..."
                    Toast.makeText(
                        context,
                        "we will notify you when apk is downloaded",
                        Toast.LENGTH_SHORT
                    ).show()
                    CoroutineScope(Dispatchers.IO).launch {
                        getUrlAndDownloadApk(application, position)
                    }
                }
            }
        }
        private suspend fun getUrlAndDownloadApk(application: Application, position: Int) {

            val response = repository!!.getApkSignedUrl(enterpriseID, application.pkg!!)
            when (response) {
                is Resource.Success -> {
                    downloadTask.downloadFile(
                        fileName = application.name!!,
                        url = response.value.signed_url,
                        filePosition = position,
                        fileType = Constant.FILE_TYPE_APK,
                        downloadProgress = downloadProgressChannel,
                        downloadCompletion = downloadCompletion,
                        downloadListener = this@AppCatalogAdapter
                    )
                }

                else -> {}
            }
            CoroutineScope(Dispatchers.Main).launch {
                downloadCompletion.receiveAsFlow().collect { downloadCompletion ->
                    Log.d(TAG, "getUrlAndDownloadApk: $downloadCompletion")
                    if (downloadCompletion.first) {
                        binding.apply {
                            btnDownload.visible(false)
                            btnInstall.visible(true)
                            Toast.makeText(
                                context,
                                "${application.name} downloaded successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            repository.apkDownloaded(
                                downloadCompletion.second,
                                application.pkg,
                                true
                            )
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "error occurred while downloading APK",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.btnDownload.visible(true)
//                        binding.pbDownload.visible(false)
                    }
                }
            }

            downloadProgressChannel.receiveAsFlow().collect { progress ->
                Log.d(TAG, "getUrlAndDownloadApk: $progress")
            }
        }
    }

    fun setEnterpriseId(enterpriseId: String) {
        enterpriseID = enterpriseId
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
        holder.bind(currentApp, position)
    }

    override fun onDownloadProgressUpdate(position: Int, progress: Int) {
        notifyItemChanged(position)
    }
}