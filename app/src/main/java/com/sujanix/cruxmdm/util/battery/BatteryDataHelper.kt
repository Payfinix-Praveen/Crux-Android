package com.sujanix.cruxmdm.util.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.sujanix.cruxmdm.data.model.socket.battery.BatteryData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BatteryDataHelper @Inject constructor(
    @ApplicationContext context: Context
) {

    private val appContext = context.applicationContext

    fun getBatteryData(): BatteryData {
        val batteryStatus = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { iFilter ->
            appContext.registerReceiver(null, iFilter)
        }

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val usbCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
        val acCharge: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_AC

        val batteryPercentage =  batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }

        Log.d("FATAL", "getBatteryStatus: \nisCharging: $isCharging usbCharge: $usbCharge acCharge: $acCharge")
        return BatteryData(
            batteryPercentage!!,
            if(isCharging) "Charging" else "Not Charging"
        )
    }
}