package com.sujanix.cruxmdm.features.core.utlis

interface DownloadListener {
    fun onDownloadProgressUpdate(position: Int, progress: Int)
}