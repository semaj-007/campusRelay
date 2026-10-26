package com.example.campusrelay.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.campusrelay.common.ServiceLocator
import com.example.campusrelay.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RelayMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) { DeviceRegistrationWorker.schedule(this) }

    override fun onMessageReceived(message: RemoteMessage) {
        // Data messages are rendered locally so privacy, notification preferences and locale
        // apply in both foreground and background. Tokens/content are never logged.
        val event = message.data["event"] ?: return
        if (event !in setOf("delivery_created", "delivery_fulfilled")) return
        val prefs = ServiceLocator.getInstance(this).preferencesManager
        kotlinx.coroutines.runBlocking {
            if (prefs.currentUserId() != message.data["userId"] || !prefs.notifyDeliveryEnabled.first()) return@runBlocking
            NotificationHelper.showDeliveryEvent(this@RelayMessagingService, event, message.data["deliveryId"].orEmpty())
        }
    }
}
