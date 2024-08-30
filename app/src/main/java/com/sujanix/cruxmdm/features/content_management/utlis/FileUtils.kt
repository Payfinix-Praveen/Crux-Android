package com.sujanix.cruxmdm.features.content_management.utlis

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.sujanix.cruxmdm.R
import java.io.File
import java.net.URI
import java.util.Locale

object FileUtils {

    fun formatBytes(bytes: Long): Pair<Int,String> {
        if (bytes < 1024) return Pair(bytes.toInt(), "B")
        val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1] + "B"
        val unit = (bytes / Math.pow(1024.0, exp.toDouble())).toInt()
        return Pair(unit, pre)
    }

    fun getFileIcon(extension: String): Int {
        return when (extension.lowercase(Locale.ROOT)) {
            "pdf" -> R.drawable.ic_pdf
            "doc", "docx" -> R.drawable.ic_word
            "xls", "xlsx" -> R.drawable.ic_excel
            "ppt", "pptx" -> R.drawable.ic_power_point
            "jpg", "jpeg", "png", "gif" -> R.drawable.ic_img
            "mp3", "wav" -> R.drawable.ic_audio
            "mp4", "avi", "mkv" -> R.drawable.ic_video
            "zip", "rar" -> R.drawable.ic_zip
            else -> R.drawable.ic_generic_file
        }
    }

    fun openFile(context: Context, filePath: String) {
        val uri = getUriForFile(context, filePath)
        val mimeType = getMimeType(context, uri) ?: "*/*" // Use */* for unknown types
        Log.d("Content Management", "openFile: $mimeType $filePath")

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, mimeType)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Check if an app can handle the intent
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // No app installed to handle the file type
            Toast.makeText(context, "No app installed to open this file type", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUriForFile(context: Context, filePath: String): Uri {
        val file = File(URI(filePath))
        val authority = context.getString(R.string.file_provider_authority) // Replace with your authority string
        return FileProvider.getUriForFile(context, authority, file)
    }

    private fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    private fun getMimeType(filePath: String): String {
        val extension = filePath.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.ms-excel"
            "txt" -> "text/plain"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "mp4" -> "video/mp4"
            "avi" -> "video/x-msvideo"
            "mkv" -> "video/x-matroska"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "7z" -> "application/x-7z-compressed"
            else -> "*/*" // Default to any type if unknown
        }
    }
}