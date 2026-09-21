package com.example.campusrelay

import android.app.Application
import com.example.campusrelay.common.ServiceLocator
import com.example.campusrelay.sync.SyncScheduler
import com.example.campusrelay.util.NotificationHelper

class CampusRelayApp : Application() {

    val serviceLocator: ServiceLocator by lazy { ServiceLocator.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        // REQ-OFF-2: opportunistically flush anything still queued from a previous session
        // as soon as the app is next launched with connectivity.
        SyncScheduler.scheduleSyncWhenOnline(this)
    }
}
