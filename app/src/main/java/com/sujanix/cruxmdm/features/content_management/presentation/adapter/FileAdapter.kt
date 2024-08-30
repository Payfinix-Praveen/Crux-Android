package com.sujanix.cruxmdm.features.content_management.presentation.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.databinding.ItemContentLayoutBinding
import com.sujanix.cruxmdm.features.content_management.data.data_source.local.FileData
import com.sujanix.cruxmdm.features.content_management.data.model.response.shared_file.Children
import com.sujanix.cruxmdm.features.content_management.data.repository.ContentManagementRepository
import com.sujanix.cruxmdm.features.content_management.utlis.FileUtils.formatBytes
import com.sujanix.cruxmdm.features.content_management.utlis.FileUtils.getFileIcon
import com.sujanix.cruxmdm.features.content_management.utlis.Utils.visible
import com.sujanix.cruxmdm.features.core.utlis.Constant
import com.sujanix.cruxmdm.features.core.utlis.DownloadListener
import com.sujanix.cruxmdm.features.core.utlis.DownloadTask
import com.sujanix.cruxmdm.features.core.utlis.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class FileAdapter(
    private val context: Context,
    private val listener: Listener,
    private val repository: ContentManagementRepository
): ListAdapter<Children, FileAdapter.ViewHolder>(DiffCallback()),
    DownloadListener {

    private val TAG: String = "FileAdapter"
    private val downloadTask = DownloadTask(context)
    private val downloadProgressChannel = Channel<Int>(Channel.CONFLATED)
    private val downloadCompletion = Channel<Pair<Boolean, String>>(Channel.CONFLATED)

    inner class ViewHolder(
        private val binding: ItemContentLayoutBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Children, position: Int) {
            binding.apply {
                val fileSize = formatBytes(data.file_size)

                tvContentName.text = data.name
                tvContentCount.text = context.getString(
                    R.string.content_size,
                    fileSize.first.toString(),
                    fileSize.second
                )
                val fileExtension = data.name.substringAfterLast(".")
                val fileIcon = getFileIcon(fileExtension)
                ivFileIcon.setImageResource(fileIcon)

                root.setOnClickListener {

                    Log.d(TAG, "bind: ${data.name} ${fileSize.first}${fileSize.second} ${data.fileKey}")
                    listener.onFileClick(data)
                }

                btnDownload.setOnClickListener {
                    btnDownload.visible(false)
                    pbDownload.visible(true)

                    Log.d(TAG, "bind: ${data.fileKey} $position $adapterPosition")
                    getUrlAndDownloadFile(data, position)
                }

                if(data.isDownloaded) {
                    btnDownload.visible(false)
                    ivDownloaded.visible(true)
                }
            }
        }

        private fun getUrlAndDownloadFile(data: Children, position: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                val response = repository.getSharedFileUrl(data.fileKey)

                when (response) {
                    is Resource.Success -> {
                        Log.d(TAG, "onDownloadClick: ${response.value.signed_url}")
                        if (response.value.signed_url != null) {
                            val url = response.value.signed_url
                            response.value.signed_url.let {
                                downloadTask.downloadFile(
                                    fileName = data.name,
                                    url = url,
                                    filePosition = position,
                                    fileKey = data.fileKey,
                                    repository = repository,
                                    downloadProgress = downloadProgressChannel,
                                    downloadCompletion = downloadCompletion,
                                    downloadListener = this@FileAdapter
                                )
                            }
                        }
                    }
                    else -> {}
                }

                downloadProgressChannel.receiveAsFlow().collect { progress ->
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.apply {
//                            progressMax = 100f
//                            progress = progres.toFloat()
//                            setProgressWithAnimation(65f, 1000)
//                            // Set ProgressBar Color
//                            progressBarColor = context.getColor(R.color.colorPrimary)
//                            // or with gradient
//                            progressBarColorStart = Color.GRAY
//                            progressBarColorEnd = Color.GREEN
//                            progressBarColorDirection = CircularProgressBar.GradientDirection.TOP_TO_BOTTOM
//
//                            // Set Width
//                            progressBarWidth = 7f // in DP
//                            backgroundProgressBarWidth = 3f // in DP
                            Log.d(TAG, "getUrlAndDownloadFile: $progress")
                            pbDownload.setProgress(progress)
//                            ivDownloaded.visible(false)
//                            notifyItemChanged(position)
                        }
                    }
                }
            }

            CoroutineScope(Dispatchers.IO).launch{
                downloadCompletion.receiveAsFlow().collect { downloadCompletion ->
                    CoroutineScope(Dispatchers.Main).launch {
                        if (downloadCompletion.first) {
                            binding.ivDownloaded.visible(true)
                            binding.pbDownload.visible(false)
//                            notifyItemChanged(position)
//                            repository.insertFile(FileData(
//                                data.name,
//                                downloadCompletion.second,
//                                data.fileKey
//                            ))
                        } else {
                            Toast.makeText(
                                context,
                                "error occurred while downloading file",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.btnDownload.visible(true)
                            binding.pbDownload.visible(false)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContentLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileAdapter.ViewHolder, position: Int) {
        val currentApp = getItem(position)
        holder.bind(currentApp, position)
    }

    class DiffCallback : DiffUtil.ItemCallback<Children>() {
        override fun areItemsTheSame(oldItem: Children, newItem: Children) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Children, newItem: Children) =
            oldItem == newItem
    }

    interface Listener {
        fun onFileClick(data: Children)
    }

    override fun onDownloadProgressUpdate(position: Int, progress: Int) {
        notifyItemChanged(position)
    }
}