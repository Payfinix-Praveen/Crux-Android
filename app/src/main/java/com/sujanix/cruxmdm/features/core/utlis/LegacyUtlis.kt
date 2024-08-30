package com.sujanix.cruxmdm.features.core.utlis

import android.content.ComponentName
import android.content.Context
import com.sujanix.cruxmdm.CruxDeviceAdminReceiver

object LegacyUtils {
    fun getAdminComponentName(context: Context): ComponentName {
        return ComponentName(context.applicationContext, CruxDeviceAdminReceiver::class.java)
    }
}