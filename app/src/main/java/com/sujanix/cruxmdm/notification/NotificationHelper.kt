package com.sujanix.cruxmdm.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.text.Html
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.sujanix.cruxmdm.R
import com.sujanix.cruxmdm.presentation.view.activity.MainActivity

object NotificationHelper {

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        notification_id: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, GEOFENCE_NOTIFICATION_CHANNEL_ID)
            .setColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                GEOFENCE_NOTIFICATION_CHANNEL_ID,
                "Geofence",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notification_id, notificationBuilder.build())
    }

    const val GEOFENCE_NOTIFICATION_CHANNEL_ID = "Geofence"
}