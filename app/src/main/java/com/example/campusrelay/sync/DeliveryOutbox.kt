package com.example.campusrelay.sync

import com.example.campusrelay.data.local.entity.PendingDeliveryRequestEntity
import retrofit2.HttpException

/** Acknowledgement occurs only after server success. Cancellation/transport failures
 * leave the original stable ID intact for a safe retry after process death. */
internal suspend fun flushDeliveryOutbox(
    requests: List<PendingDeliveryRequestEntity>,
    send: suspend (PendingDeliveryRequestEntity) -> Unit,
    acknowledge: suspend (PendingDeliveryRequestEntity) -> Unit,
    reject: suspend (PendingDeliveryRequestEntity, String) -> Unit
): Int {
    var count = 0
    for (request in requests) {
        try {
            send(request)
            acknowledge(request)
            count++
        } catch (e: HttpException) {
            if (e.code() in listOf(401, 403, 408, 429) || e.code() >= 500) throw e
            reject(request, "HTTP ${e.code()}")
        }
    }
    return count
}
