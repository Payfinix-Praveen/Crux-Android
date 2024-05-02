package com.sujanix.cruxmdm.feature.device_detail.utlis

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.feature.device_detail.data.model.DeviceDetail
import com.sujanix.cruxmdm.feature.common.data.model.NetworkConnectivity
import com.sujanix.cruxmdm.feature.common.utlis.externalMemoryAvailable
import com.sujanix.cruxmdm.feature.common.utlis.loadFileAsString
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import java.io.IOException


@SuppressLint("HardwareIds")
fun getDeviceDetails(context: Context, activity: Activity): Pair<JSONObject, MutableList<DeviceDetail>> {
    val deviceDetails = JSONObject()
    val deviceDetail = mutableListOf<DeviceDetail>()
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.READ_PHONE_STATE),
            100
        )
    } else {
        val subscriptionInfos = SubscriptionManager.from(context).activeSubscriptionInfoList
        val deviceName =
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
//            val imei = Settings.Global.getString(activity?.contentResolver, "imei")
        val name = BluetoothAdapter.getDefaultAdapter().name

        val deviceStorageDetails = getTotalExternalMemorySize()

        if (subscriptionInfos.isNotEmpty()) {
            for (i in subscriptionInfos.indices) {
                val lsuSubscriptionInfo = subscriptionInfos[i]
                deviceDetails.put("networkName", lsuSubscriptionInfo.carrierName)
                deviceDetails.put("getCountryIso", lsuSubscriptionInfo.countryIso)
                deviceDetail.add(0, DeviceDetail(R.drawable.ic_file, "Network ${i + 1} name", "${lsuSubscriptionInfo.carrierName}\n${lsuSubscriptionInfo.countryIso.toString().uppercase()}"))
            }
        } else {
            deviceDetails.put("networkName", "No carrier found")
            deviceDetails.put("getCountryIso", "No carrier found")
            deviceDetail.add(0, DeviceDetail(R.drawable.ic_file, "Network name", "No carrier found"))
        }
        deviceDetails.put("MODEL", Build.MODEL)
        deviceDetails.put("ID", Build.ID)
        deviceDetails.put("Manufacturer", Build.MANUFACTURER)
        deviceDetails.put("brand", Build.BRAND)
        deviceDetails.put("type", Build.TYPE)
        deviceDetails.put("user", Build.USER)
        deviceDetails.put("BASE", Build.VERSION_CODES.BASE)
        deviceDetails.put("INCREMENTAL", Build.VERSION.INCREMENTAL)
        deviceDetails.put("SDK", Build.VERSION.SDK)
        deviceDetails.put("BOARD", Build.BOARD)
        deviceDetails.put("HOST", Build.HOST)
        deviceDetails.put("FINGERPRINT", Build.FINGERPRINT)
        deviceDetails.put("Version Code", Build.VERSION.RELEASE)
        deviceDetails.put("DeviceName", deviceName)
        deviceDetails.put("TotalStorage", deviceStorageDetails?.first)
        deviceDetails.put("AvailableStorage", deviceStorageDetails?.second)
        deviceDetails.put("blutoothName", name)
        deviceDetail.add(1, DeviceDetail(R.drawable.ic_file, "Model", Build.MODEL))
        deviceDetail.add(2, DeviceDetail(R.drawable.ic_file, "Manufacturer", Build.MANUFACTURER))
        deviceDetail.add(3, DeviceDetail(R.drawable.ic_file, "Brand", Build.BRAND))
        deviceDetail.add(4, DeviceDetail(R.drawable.ic_file, "Brand", Build.BRAND))
        deviceDetail.add(5, DeviceDetail(R.drawable.ic_file, "Android version", "${Build.VERSION.RELEASE} ${Build.ID}"))
        deviceDetail.add(6, DeviceDetail(R.drawable.ic_file, "Device name", deviceName))
        deviceDetail.add(7,
            DeviceDetail(
                R.drawable.ic_file,
                "Device Storage",
                "${deviceStorageDetails?.first} GB (Total)\n" +
                        "${deviceStorageDetails?.second} GB (Available)"
            )
        )
        deviceDetail.add(8, DeviceDetail(R.drawable.ic_file, "FINGERPRINT", Build.FINGERPRINT))
        getDeviceRAM(context)
    }
    return Pair(deviceDetails, deviceDetail)
}

fun getTotalExternalMemorySize(): Pair<Long, Long>? {
    return try {
        if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val availableBytes = stat.availableBytes
            val totalBytes = stat.totalBytes
            val totalStorage = totalBytes / (1024 * 1024 * 1024)
            val availableStorage = availableBytes / (1024 * 1024 * 1024)
            Pair(totalStorage, availableStorage)
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getNetworkInfo(context: Context): Flow<NetworkConnectivity> {
    return callbackFlow {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // Check for initial connectivity
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            var networkType = activeNetworkInfo?.typeName ?: "NONE"
            val networkInfo = activeNetworkInfo?.extraInfo

            val networkConnectivity = NetworkConnectivity(
                false,
                "NONE"
            )
            if (activeNetworkInfo?.isConnected == true) {
                checkNetworkType(context, networkType, networkConnectivity)

            } else {
                trySend(networkConnectivity)
            }

            val networkCallback = object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    Log.d("FATAL", "onAvailable: ${connectivityManager}")
                    networkType = activeNetworkInfo?.typeName!!
                    checkNetworkType(context, networkType, networkConnectivity)
                }

                override fun onLost(network: Network) {
                    trySend(networkConnectivity)
                }
            }

            val request = NetworkRequest.Builder().build()
            connectivityManager.registerNetworkCallback(request, networkCallback)

            awaitClose {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            }
        } catch (e: Exception) {
            Log.d("FATAL", "getNetworkInfoError: ${e}")
            val networkConnectivity = NetworkConnectivity(
                false,
                "NONE"
            )
            trySend(networkConnectivity)
        }
    }
}

private fun ProducerScope<NetworkConnectivity>.checkNetworkType(
    context: Context,
    networkType: String,
    networkConnectivity: NetworkConnectivity
) {
    if (networkType == "WIFI") {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ssid = wifiInfo.getSSID()

        trySend(networkConnectivity.copy(isConnected = true, networkType = ssid))
        // WiFi network
    } else if (networkType == "MOBILE") {
        // Mobile network
        trySend(networkConnectivity.copy(isConnected = true))
    } else {
        // Other network type
        trySend(networkConnectivity.copy(isConnected = true, networkType = "OTHER"))
    }
}

fun getDeviceRAM(context: Context){
    val mi = ActivityManager.MemoryInfo()
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    activityManager!!.getMemoryInfo(mi)
    val availableMegs = mi.availMem / 0x100000L

    getMacAddress()
    Log.d("FATAL", "onViewCreated: ${availableMegs / 1024}GB ")
}

fun getMacAddress(): String? {
    return try {
        loadFileAsString("/sys/class/net/eth0/address")
            .uppercase().substring(0, 17)
    } catch (e: IOException) {

        e.printStackTrace()
        null
    }
}