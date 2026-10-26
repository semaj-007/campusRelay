package com.example.campusrelay.notifications

import android.content.Context
import androidx.work.*
import com.example.campusrelay.common.ServiceLocator
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class DeviceRegistrationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val locator = ServiceLocator.getInstance(applicationContext)
        if (locator.preferencesManager.currentSessionToken() == null) return Result.success()
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            locator.apiService.registerDevice(DeviceRegistration(token, locator.preferencesManager.notifyDeliveryEnabled.first()))
            Result.success()
        } catch (e: CancellationException) { throw e
        } catch (_: Exception) { Result.retry() }
    }
    companion object {
        fun schedule(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork("register-fcm-device", ExistingWorkPolicy.APPEND_OR_REPLACE,
                OneTimeWorkRequestBuilder<DeviceRegistrationWorker>()
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, java.util.concurrent.TimeUnit.SECONDS).build())
        }
    }
}
data class DeviceRegistration(val token: String, val deliveryEnabled: Boolean)
