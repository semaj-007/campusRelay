package com.example.campusrelay.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * REQ-OFF-2 applied to delivery-request creation: if a user submits the "New Delivery
 * Request" wizard while offline, it's queued here instead of being lost, then flushed by
 * [com.example.campusrelay.sync.OfflineSyncWorker] once the network is back. This is a
 * separate table from [OfflineTransactionEntity] because it queues a different backend
 * call (POST /api/v1/deliveries) rather than the handoff sync endpoint from the spec.
 */
@Entity(tableName = "pending_delivery_requests")
data class PendingDeliveryRequestEntity(
    @PrimaryKey val localId: String,
    val senderId: String,
    val pickupBuilding: String,
    val dropoffBuilding: String,
    val itemDescription: String,
    val weightCategory: String,
    val rewardAmount: Double,
    val isEcoFriendlyRoute: Boolean,
    val createdAtIso: String
)
