package com.example.campusrelay.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.campusrelay.common.ServiceLocator
import com.example.campusrelay.data.remote.dto.OfflineSyncRequestDto
import com.example.campusrelay.data.remote.dto.OfflineTransactionDto
import com.example.campusrelay.util.NotificationHelper

/**
 * REQ-OFF-2: runs once WorkManager sees a network connection again (see [SyncScheduler])
 * and flushes both offline queues:
 *  1. confirmed handoffs waiting on POST /api/v1/deliveries/sync-offline
 *  2. delivery requests that were created while offline, via POST /api/v1/deliveries
 */
class OfflineSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val locator = ServiceLocator.getInstance(applicationContext)

        val handoffResult = runCatching {
            val pendingHandoffs = locator.appDatabase.offlineTransactionDao().getPending()
            if (pendingHandoffs.isNotEmpty()) {
                val response = locator.apiService.syncOfflineTransactions(
                    OfflineSyncRequestDto(
                        offlineTransactions = pendingHandoffs.map {
                            OfflineTransactionDto(
                                localTransactionId = it.localTransactionId,
                                deliveryId = it.deliveryId,
                                scannedQrHash = it.scannedQrHash,
                                completedAt = it.completedAtIso,
                                courierId = it.courierId
                            )
                        }
                    )
                )
                locator.appDatabase.offlineTransactionDao()
                    .markSynced(pendingHandoffs.map { it.localTransactionId })
                response.syncedCount
            } else 0
        }.getOrDefault(0)

        val flushedRequests = runCatching { locator.deliveryRepository.flushPendingRequests() }.getOrDefault(0)

        if (handoffResult > 0 || flushedRequests > 0) {
            NotificationHelper.showDeliveryStatusNotification(
                applicationContext,
                title = "CampusRelay sync complete",
                message = "Synced $handoffResult handoff(s) and $flushedRequests queued request(s)."
            )
        }

        return Result.success()
    }
}
