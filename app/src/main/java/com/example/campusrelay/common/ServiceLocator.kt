package com.example.campusrelay.common

import android.content.Context
import com.example.campusrelay.data.local.AppDatabase
import com.example.campusrelay.data.remote.ApiService
import com.example.campusrelay.data.remote.RetrofitProvider
import com.example.campusrelay.data.repository.AuthRepository
import com.example.campusrelay.data.repository.DeliveryRepository
import com.example.campusrelay.data.repository.SettingsRepository
import kotlinx.coroutines.runBlocking

/**
 * A small hand-rolled dependency container. A production app would reach for Hilt/Koin,
 * but for a single-module prototype a plain singleton object keeps the moving parts easy
 * to follow end to end.
 */
class ServiceLocator private constructor(context: Context) {

    val preferencesManager: PreferencesManager = PreferencesManager(context)
    val appDatabase: AppDatabase = AppDatabase.getInstance(context)

    val apiService: ApiService = RetrofitProvider.create(
        sessionTokenProvider = { runBlocking { preferencesManager.currentSessionToken() } }
    )

    val authRepository: AuthRepository by lazy { 
        AuthRepository(apiService, preferencesManager, null) 
    }

    val deliveryRepository: DeliveryRepository by lazy {
        DeliveryRepository(
            apiService = apiService,
            cachedDeliveryDao = appDatabase.cachedDeliveryDao(),
            pendingDeliveryRequestDao = appDatabase.pendingDeliveryRequestDao(),
            preferencesManager = preferencesManager
        )
    }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(preferencesManager) }

    companion object {
        @Volatile private var instance: ServiceLocator? = null

        fun getInstance(context: Context): ServiceLocator =
            instance ?: synchronized(this) {
                instance ?: ServiceLocator(context.applicationContext).also { instance = it }
            }
    }
}
