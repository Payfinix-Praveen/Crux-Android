package com.sujanix.cruxmdm.util

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.UserManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.OutputStreamWriter
import kotlin.math.floor


fun isDeviceOwner(context: Context): Boolean {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    return dpm.isDeviceOwnerApp(
        context.packageName
    )
}

fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun LinearLayout.setEnable(state: Boolean) {
    for (i in 0 until this.childCount) {
        val child = this.getChildAt(i)
        child.isEnabled = state
    }
}

fun JSONObject.toMap(): Map<String, *> = keys().asSequence().associateWith {
    when (val value = this[it]) {
        is JSONArray -> {
            val map = (0 until value.length()).associate { Pair(it.toString(), value[it]) }
            JSONObject(map).toMap().values.toList()
        }

        is JSONObject -> value.toMap()
        JSONObject.NULL -> null
        else -> value
    }
}

fun externalMemoryAvailable(): Boolean {
    return Environment.getExternalStorageState() ==
            Environment.MEDIA_MOUNTED
}

fun hourAndMinutesToMillis(hour: Int, minute: Int): Long {
    return (hour * 60 + minute) * 60 * 1000L
}

inline fun runOnInterval(crossinline block: () -> Unit, interval: Long): Runnable {
    val handler = Handler(Looper.getMainLooper())
    val runnable = object : Runnable {
        override fun run() {
            block()
            handler.postDelayed(this, interval)
        }
    }
    handler.post(runnable)
    return runnable
}

fun View.snackbar(message: String, action: (() -> Unit)? = null) {
    val snckbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
    action?.let {
        snckbar.setAction("Retry") {
            it()
        }
    }
    snckbar.show()
}

fun isPackageInstalled(context: Context, targetPackage: String?): Boolean {
    val pm = context.packageManager
    try {
        pm.getPackageInfo(targetPackage!!, PackageManager.GET_META_DATA)
    } catch (e: PackageManager.NameNotFoundException) {
        return false
    }
    return true
}

fun isApkDownloaded(context: Context, fileName: String?): Boolean {
    return try {
        val file = File(context.filesDir, fileName!!)
        if(file.exists())  true
        false
    } catch (e: Exception) {
        false
    }


}

fun isMiui(context: Context): Boolean {
    return isPackageInstalled(context, "com.miui.home") ||
            isPackageInstalled(context, "com.miui.securitycenter")
}

fun lockSafeBoot(context: Context): Boolean {
    if (!isDeviceOwner(context)) {
        return false
    }
    val devicePolicyManager = context.getSystemService(
        Context.DEVICE_POLICY_SERVICE
    ) as DevicePolicyManager
    val adminComponentName = LegacyUtils.getAdminComponentName(context)
    try {
        devicePolicyManager.addUserRestriction(adminComponentName, UserManager.DISALLOW_SAFE_BOOT)
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
    return true
}

fun writeStringToFile(fileName: String?, fileContent: String?, overwrite: Boolean): Boolean {
    return try {
        val file = File(fileName)
        if (file.exists()) {
            if (overwrite) {
                file.delete()
            } else {
                return false
            }
        }
        file.createNewFile()
        val fos = FileOutputStream(file)
        val writer = OutputStreamWriter(fos)
        writer.append(fileContent)
        writer.close()
        fos.close()
        return true
    } catch (e: java.lang.Exception) {
        false
    }
}

@Throws(IOException::class)
fun loadFileAsString(filePath: String?): String {
    val fileData = StringBuffer()
    val reader = BufferedReader(FileReader(filePath))
    val buf = CharArray(1024)
    var numRead = 0
    while (reader.read(buf).also { numRead = it } != -1) {
        val readData = String(buf, 0, numRead)
        fileData.append(readData)
    }
    reader.close()
    return fileData.toString()
}

inline fun showDialog(
    activity: Activity,
    title: String,
    message: String,
    positiveBtnText: String,
    negativeBtnText: String,
    cancelable: Boolean = false,
    crossinline positiveBtnListener: () -> Unit,
    crossinline negativeBtnListener: () -> Unit = {},
) {
    MaterialAlertDialogBuilder(activity)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveBtnText) { dialog, which ->
            positiveBtnListener()
        }
        .setNegativeButton(negativeBtnText) { dialog, which ->
            negativeBtnListener()
        }
        .setCancelable(cancelable)
        .create()
        .show()
}

fun canInstallPackages(context: Context): Boolean {
    return context.packageManager.canRequestPackageInstalls()
}

fun degreesToCompassDirection(degrees: Double): String {
    val normalizedDegrees = (degrees + 360) % 360  // Normalize angle to 0-359 range

    return when (normalizedDegrees) {
        in 337.5..360.0 -> "N"
        in 0.0..22.5 -> "N"
        in 22.5..67.5 -> "NE"
        in 67.5..112.5 -> "E"
        in 112.5..157.5 -> "SE"
        in 157.5..202.5 -> "S"
        in 202.5..247.5 -> "SW"
        in 247.5..292.5 -> "W"
        in 292.5..337.5 -> "NW"
        else -> "Unknown"  // Optional: Handle unexpected values
    }
}
