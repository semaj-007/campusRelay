package com.example.campusrelay.data.remote.dto

import com.google.gson.annotations.SerializedName

/** One queued handoff, matching the Part 1 "Synchronize Offline Handoff Transactions" spec. */
data class OfflineTransactionDto(
    @SerializedName("localTransactionId") val localTransactionId: String,
    @SerializedName("deliveryId") val deliveryId: String,
    @SerializedName("scannedQrHash") val scannedQrHash: String,
    @SerializedName("completedAt") val completedAt: String,
    @SerializedName("courierId") val courierId: String
)

/** Request body for POST /api/v1/deliveries/sync-offline. */
data class OfflineSyncRequestDto(
    @SerializedName("offlineTransactions") val offlineTransactions: List<OfflineTransactionDto>
)

/** 200 OK response body for POST /api/v1/deliveries/sync-offline. */
data class OfflineSyncResponseDto(
    @SerializedName("syncedCount") val syncedCount: Int,
    @SerializedName("failedTransactions") val failedTransactions: List<String>
)
