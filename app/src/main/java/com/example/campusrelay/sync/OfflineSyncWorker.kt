package com.example.campusrelay.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.campusrelay.common.ServiceLocator
import com.example.campusrelay.data.remote.dto.OfflineSyncRequestDto
import com.example.campusrelay.data.remote.dto.OfflineTransactionDto
import kotlinx.coroutines.CancellationException

class OfflineSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val locator = ServiceLocator.getInstance(applicationContext)
        val prefs = locator.preferencesManager
        if (prefs.currentSessionToken() == null) return Result.success()
        return try {
            locator.deliveryRepository.flushPendingRequests()
            val user = prefs.currentUserId()
            val pending = locator.appDatabase.offlineTransactionDao().getPending().filter { it.courierId == user }
            if (pending.isNotEmpty()) {
                val response = locator.apiService.syncOfflineTransactions(OfflineSyncRequestDto(pending.map {
                    OfflineTransactionDto(it.localTransactionId, it.deliveryId, it.scannedQrHash, it.completedAtIso, it.courierId)
                }))
                val accepted = acknowledgedIds(pending.map { it.localTransactionId }, response.failedTransactions)
                locator.appDatabase.offlineTransactionDao().markSynced(accepted)
                if (response.failedTransactions.isNotEmpty()) return Result.retry()
            }
            Result.success()
        } catch (e: CancellationException) { throw e
        } catch (e: java.io.IOException) { Result.retry()
        } catch (e: retrofit2.HttpException) { Result.retry() }
    }
}

/** The API acknowledges individual records; a partial success never drops failed work. */
internal fun acknowledgedIds(submitted: List<String>, failed: List<String>): List<String> =
    submitted.filterNot { it in failed.toSet() }
