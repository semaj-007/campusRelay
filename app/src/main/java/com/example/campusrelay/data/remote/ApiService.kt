package com.example.campusrelay.data.remote

import com.example.campusrelay.data.remote.dto.AuthResponseDto
import com.example.campusrelay.data.remote.dto.CreateDeliveryRequestDto
import com.example.campusrelay.data.remote.dto.CreateDeliveryResponseDto
import com.example.campusrelay.data.remote.dto.DeliveryFeedItemDto
import com.example.campusrelay.data.remote.dto.OfflineSyncRequestDto
import com.example.campusrelay.data.remote.dto.OfflineSyncResponseDto
import com.example.campusrelay.data.remote.dto.SsoLoginRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * The custom ASP.NET Core Web API described in the "REST API and Backend Architecture"
 * section of the Part 1 document. Every call goes over HTTPS (REQ-API-1) and every
 * response is plain JSON (REQ-API-2) — Retrofit + the Gson converter handle both for us.
 */
interface ApiService {

    // REQ-AUTH-1 / REQ-AUTH-3
    @POST("api/v1/auth/sso")
    suspend fun ssoLogin(@Body body: SsoLoginRequestDto): AuthResponseDto

    // Endpoint 1: Create Delivery Request
    @POST("api/v1/deliveries")
    suspend fun createDeliveryRequest(@Body body: CreateDeliveryRequestDto): CreateDeliveryResponseDto

    // Backs the Home Dashboard's "nearby delivery requests" map/list (REQ-DEL-3, REQ-DEL-4)
    @GET("api/v1/deliveries/feed")
    suspend fun getDeliveryFeed(): List<DeliveryFeedItemDto>

    // Endpoint 2: Synchronize Offline Handoff Transactions (REQ-OFF-2)
    @POST("api/v1/deliveries/sync-offline")
    suspend fun syncOfflineTransactions(@Body body: OfflineSyncRequestDto): OfflineSyncResponseDto
}
