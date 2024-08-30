package com.sujanix.cruxmdm.features.app_catalog.utlis

interface DownloadProgress {
    fun onDownloadProgress(progress: Int, total: Long, current: Long)
}