package com.example.campusrelay.data.remote

import com.example.campusrelay.data.model.ListingCategory
import com.example.campusrelay.data.model.ListingCondition
import com.example.campusrelay.data.model.ListingStatus
import com.example.campusrelay.data.model.RideOfferStatus
import com.example.campusrelay.data.model.VehicleType
import com.example.campusrelay.data.remote.dto.AuthResponseDto
import com.example.campusrelay.data.remote.dto.CreateDeliveryRequestDto
import com.example.campusrelay.data.remote.dto.CreateDeliveryResponseDto
import com.example.campusrelay.data.remote.dto.DeliveryFeedItemDto
import com.example.campusrelay.data.remote.dto.DevLoginRequestDto
import com.example.campusrelay.data.remote.dto.DevLoginResponseDto
import com.example.campusrelay.data.remote.dto.MarketplaceDtos
import com.example.campusrelay.data.remote.dto.OfflineSyncRequestDto
import com.example.campusrelay.data.remote.dto.OfflineSyncResponseDto
import com.example.campusrelay.data.remote.dto.SsoLoginRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * The custom ASP.NET Core Web API described in the "REST API and Backend Architecture"
 * section of the Part 1 document. Every call goes over HTTPS (REQ-API-1) and every
 * response is plain JSON (REQ-API-2) - Retrofit + the Gson converter handle both for us.
 */
interface ApiService {

    // REQ-AUTH-1: Firebase SSO login - sends Firebase ID token to backend
    @POST("api/v1/auth/sso")
    suspend fun ssoLogin(@Body body: SsoLoginRequestDto): AuthResponseDto

    // Dev login for testing (creates demo user in backend)
    @POST("api/v1/auth/dev-login")
    suspend fun devLogin(@Body body: DevLoginRequestDto): DevLoginResponseDto

    // Endpoint 1: Create Delivery Request
    @POST("api/v1/deliveries")
    suspend fun createDeliveryRequest(@Body body: CreateDeliveryRequestDto): CreateDeliveryResponseDto

    // Backs the Home Dashboard's "nearby delivery requests" map/list (REQ-DEL-3, REQ-DEL-4)
    @GET("api/v1/deliveries/feed")
    suspend fun getDeliveryFeed(): List<DeliveryFeedItemDto>

    // Endpoint 2: Synchronize Offline Handoff Transactions (REQ-OFF-2)
    @POST("api/v1/deliveries/sync-offline")
    suspend fun syncOfflineTransactions(@Body body: OfflineSyncRequestDto): OfflineSyncResponseDto

    // Marketplace endpoints
    @GET("api/v1/marketplace/listings")
    suspend fun getMarketplaceListings(): List<MarketplaceDtos.MarketplaceListingDto>

    @GET("api/v1/marketplace/listings/{id}")
    suspend fun getMarketplaceListingById(@Path("id") id: String): MarketplaceDtos.MarketplaceListingDto

    @POST("api/v1/marketplace/listings")
    suspend fun createMarketplaceListing(@Body body: MarketplaceDtos.CreateListingRequest): MarketplaceDtos.MarketplaceListingDto

    // Carpool endpoints
    @GET("api/v1/carpool/ride-offers")
    suspend fun getRideOffers(): List<CarpoolDtos.RideOfferDto>

    @GET("api/v1/carpool/ride-offers/{id}")
    suspend fun getRideOfferById(@Path("id") id: String): CarpoolDtos.RideOfferDto

    @POST("api/v1/carpool/ride-offers")
    suspend fun createRideOffer(@Body body: CarpoolDtos.CreateRideOfferRequest): CarpoolDtos.RideOfferDto

    @POST("api/v1/carpool/ride-offers/{id}/request-seat")
    suspend fun requestSeat(@Path("id") id: String, @Body body: CarpoolDtos.RequestSeatRequest): CarpoolDtos.RequestSeatResponse
}
