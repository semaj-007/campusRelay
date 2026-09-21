package com.example.campusrelay.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.campusrelay.R

/**
 * REQ-NOT-1: high-priority push notifications for delivery status changes.
 *
 * This posts a *local* notification so the flow is demoable without a Firebase project.
 * To go from this to real push notifications: add the Firebase BoM + Cloud Messaging
 * dependency, drop your project's google-services.json into app/, and forward incoming
 * [com.google.firebase.messaging.FirebaseMessagingService.onMessageReceived] payloads into
 * [showDeliveryStatusNotification] below.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "delivery_status"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Delivery status updates",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun showDeliveryStatusNotification(context: Context, title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_delivery_box)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
