package com.example.campusrelay.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * REQ-OFF-2: queued when a package handoff is confirmed while offline. [synced] flips to
 * true once [com.example.campusrelay.sync.OfflineSyncWorker] has pushed it to
 * POST /api/v1/deliveries/sync-offline.
 */
@Entity(tableName = "offline_transactions")
data class OfflineTransactionEntity(
    @PrimaryKey val localTransactionId: String,
    val deliveryId: String,
    val scannedQrHash: String,
    val completedAtIso: String,
    val courierId: String,
    val synced: Boolean = false
)
