package com.sujanix.cruxmdm.features.core.data.model.socket

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class Configuration {

    @Parcelize
    data class LocationTrackingConfig(
        val enabled: Boolean,
        val history_enabled: Boolean,
        val range: Int
    ): Configuration(), Parcelable

    @Parcelize
    data class BatteryTrackingConfig(
        val enabled: Boolean,
        val hour: Int,
        val minutes: Int,
        val type: String
    ): Configuration(), Parcelable

    companion object {
        fun fromMap(map: Map<String, Any>): Configuration {
            val enabled = map["enabled"] as Boolean
            return if(map.containsKey("range")){
                LocationTrackingConfig(
                    enabled,
                    map["history_enabled"] as Boolean,
                    map["range"] as Int
                )
            }
            else if(map.containsKey("hour") && map.containsKey("minutes")) {
                BatteryTrackingConfig(
                    enabled,
                    map["hour"] as Int,
                    map["minutes"] as Int,
                    map["type"] as String
                )
            } else {
                throw IllegalArgumentException("Invalid configuration data structure")
            }
        }
    }
}
