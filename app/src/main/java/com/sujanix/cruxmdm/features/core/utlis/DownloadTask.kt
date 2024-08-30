package com.sujanix.cruxmdm.features.core.utlis

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.sujanix.cruxmdm.features.content_management.data.data_source.local.FileData
import com.sujanix.cruxmdm.features.content_management.data.repository.ContentManagementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class DownloadTask(private val context: Context) {

    private val TAG: String = "DownloadTask"
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val downloadMap: MutableMap<String, Pair<Int, Channel<Int>>> = mutableMapOf()

    suspend fun downloadFile(
        fileName: String,
        url: String,
        filePosition: Int,
        fileType: String = Constant.FILE_TYPE_FILE,
        fileKey: String? = null,
        repository: ContentManagementRepository? = null,
        downloadProgress: Channel<Int>,
        downloadCompletion: Channel<Pair<Boolean, String>>,
        downloadListener: DownloadListener
    ) {

        val name = fileName.replace(" ", "_")
        val destination = if(fileType == Constant.FILE_TYPE_APK){
            "file://${context.getExternalFilesDir(null)}/$name.apk"
        } else {
            "file://${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$name"
        }
        val request = createDownloadRequest(fileName, url, destination)
        val downloadId = downloadManager.enqueue(request)

        while (true) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            cursor.moveToFirst()

            if (cursor.isAfterLast) {
                cursor.close()
                break
            }

            val bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

            val progress = (bytesDownloaded * 100f / bytesTotal).toInt()
            CoroutineScope(Dispatchers.IO).launch {
                downloadProgress.send(progress)
                val position = downloadMap[url]?.first ?: return@launch
                downloadListener.onDownloadProgressUpdate(filePosition, progress) // Notify listener
            }

            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                downloadCompletion.send(Pair(true, destination))
                if(fileType == Constant.FILE_TYPE_FILE) {
                    repository?.insertFile(
                        FileData(
                            fileName,
                            destination,
                            fileKey!!
                        )
                    )
                }
                cursor.close()
                break
            } else if (status == DownloadManager.STATUS_FAILED) {
                downloadCompletion.send(Pair(false, ""))
                cursor.close()
                break
            }

            cursor.close()
        }
    }

    private fun createDownloadRequest(fileName: String, url: String, destination: String): DownloadManager.Request {
        val downloadUri = Uri.parse(url)

        val request = DownloadManager.Request(downloadUri)
//        request.setMimeType(MIME_TYPE) // Set appropriate MIME type
        request.setTitle("CRUX MDM")
        request.setDescription("Downloading $fileName...")
        request.setDestinationUri(Uri.parse(destination))
        return request
    }
}