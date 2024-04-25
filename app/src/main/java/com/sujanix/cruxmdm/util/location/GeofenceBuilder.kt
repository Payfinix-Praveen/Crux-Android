package com.sujanix.cruxmdm.util.location

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.sujanix.cruxmdm.receiver.GeofenceReceiver
import com.sujanix.cruxmdm.util.Constant
import com.sujanix.cruxmdm.util.Constant.GEO_FENCE
import com.sujanix.cruxmdm.util.Constant.PREFERENCE_NAME
import javax.inject.Inject


class GeofenceBuilder @Inject constructor(
    private val context: Context,
    private val latitude: Double,
    private val longitude: Double
): LocationUtils.LocationListener {

    private var geofenceClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    private val geofenceList = mutableListOf<Geofence>()
    private var lat: Double = 0.0
    private var lon: Double = 0.0

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    init {
        LocationUtils.getLocation(
            context,
            this
        )
        createAndAddGeofence()
    }

    private fun createAndAddGeofence() {

        geofenceList.add(
            Geofence.Builder()
                .setRequestId("key.id")
                .setCircularRegion(latitude, longitude, 50f)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        )

        createLocationRequest()
    }

    private fun createLocationRequest(){
        val locationRequest = LocationRequest.create().apply {
            interval = Constant.LOCATION_INTERVAL
            fastestInterval = Constant.LOCATION_FASTEST_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val client = LocationServices.getSettingsClient(context as Activity)
        client.checkLocationSettings(locationSettingsRequest).apply {
            addOnFailureListener { exception ->
                if(exception is ResolvableApiException){
                    try {
                        exception.startResolutionForResult(
                            context,
                            100
                        )
                    } catch (sendEx: IntentSender.SendIntentException){
                        Log.d(
                            "createLocationRequest",
                            "createLocationRequest: ${sendEx.message}"
                        )
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Enable your location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            addOnSuccessListener { response ->
                Log.i("Location", "Enable Successful: ${response}")
                addGeofenceRequest()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofenceRequest() {
        geofenceClient.addGeofences(getGeofenceRequest(), pendingIntent).run {
            addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Geofence is added successfully",
                    Toast.LENGTH_SHORT
                ).show()

                updateGeoFencePreference()
            }
            addOnFailureListener {
                Log.e("Error", it.toString())
                Toast.makeText(context, "${it.message} geofence", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateGeoFencePreference() {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(GEO_FENCE, true)
        editor.apply()
    }

    private fun getGeofenceRequest() = GeofencingRequest.Builder().apply {
        setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        addGeofences(geofenceList)
    }.build()

    override fun onLocationReceived(latitude_: String, longitude_: String) {
        lat = latitude_.toDouble()
        lon = longitude_.toDouble()
    }

    override fun onLocationError() {
        Toast.makeText(context as Activity, "Unable to fetch location", Toast.LENGTH_SHORT).show()
    }
}