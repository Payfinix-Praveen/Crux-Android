package com.sujanix.cruxmdm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.sujanix.cruxmdm.features.core.utlis.NotificationHelper

class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent != null) {
            if(geofencingEvent.hasError()){
                val errorMessage = geofencingEvent.let {
                    GeofenceStatusCodes
                        .getStatusCodeString(it.errorCode)
                }
                Log.e("GeofenceReceiver", "onReceive: $errorMessage")
                return
            }
        }

        when (geofencingEvent?.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                NotificationHelper.showNotification(
                    context,
                    "Crux-MDM",
                    "You've entered the premises.",
                    100
                )
            }

            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                NotificationHelper.showNotification(context, "Crux-MDM", "You're in the premises.", 100)
            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                NotificationHelper.showNotification(
                    context,
                    "Crux-MDM",
                    "You've exited the premises.",
                    100
                )
            }

            else -> {
                Log.e("GeofenceReceiver", "Error in setting up the geofence")
            }
        }
    }
}