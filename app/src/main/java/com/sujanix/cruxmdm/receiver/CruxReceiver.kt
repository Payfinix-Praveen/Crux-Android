package com.sujanix.cruxmdm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.sujanix.cruxmdm.service.LocationService
import com.sujanix.cruxmdm.features.core.utlis.Constant

class CruxReceiver : BroadcastReceiver() {

    private val TAG:String = "CruxReceiver"

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        if(intent != null) {
            val socketResponse = intent.getStringExtra(Constant.SOCKET_MESSAGE)
            Log.d("FATAL", "onReceive: $socketResponse")
//            when(socketResponse?.type){
//                Constant.LOCATION_TRACKING -> {
//                    Intent(Constant.LOCATION_TRACKING).apply {
//                        context.startForegroundService(this)
//                    }
//                }
//            }
        }
    }
}