package com.example.campusrelay.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.campusrelay.MainActivity
import com.example.campusrelay.R

object NotificationHelper {
    private const val CHANNEL_ID = "delivery_status"
    fun ensureChannel(context: Context) {
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_channel), NotificationManager.IMPORTANCE_HIGH))
    }
    fun showDeliveryEvent(context: Context, event: String, deliveryId: String) {
        val locales = context.getSystemService(android.app.LocaleManager::class.java).applicationLocales
        val config = android.content.res.Configuration(context.resources.configuration)
        if (!locales.isEmpty) config.setLocales(locales)
        val localized = context.createConfigurationContext(config)
        val body = if (event == "delivery_fulfilled") R.string.notification_delivery_fulfilled else R.string.notification_delivery_created
        showDeliveryStatusNotification(localized, localized.getString(R.string.app_name), localized.getString(body), deliveryId.hashCode())
    }
    fun showDeliveryStatusNotification(context: Context, title: String, message: String, id: Int = 0) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        ensureChannel(context)
        val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_delivery_box).setContentTitle(title).setContentText(message)
            .setContentIntent(pending).setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
