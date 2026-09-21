package com.example.campusrelay.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.campusrelay.common.Constants

/** Enqueues [OfflineSyncWorker] to run the next time the device has a network connection. */
object SyncScheduler {

    fun scheduleSyncWhenOnline(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<OfflineSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(Constants.SYNC_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }
}
