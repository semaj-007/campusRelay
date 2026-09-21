package com.example.campusrelay.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Request body for POST /api/v1/deliveries — field names match the Part 1 spec exactly. */
data class CreateDeliveryRequestDto(
    @SerializedName("senderId") val senderId: String,
    @SerializedName("pickupBuilding") val pickupBuilding: String,
    @SerializedName("dropoffBuilding") val dropoffBuilding: String,
    @SerializedName("itemDescription") val itemDescription: String,
    @SerializedName("weightCategory") val weightCategory: String,
    @SerializedName("rewardAmount") val rewardAmount: Double,
    @SerializedName("isEcoFriendlyRoute") val isEcoFriendlyRoute: Boolean
)

/** 201 Created response body for POST /api/v1/deliveries. */
data class CreateDeliveryResponseDto(
    @SerializedName("deliveryId") val deliveryId: String,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("qrVerificationCode") val qrVerificationCode: String
)

/** A single item in the GET delivery feed used to populate the home dashboard. */
data class DeliveryFeedItemDto(
    @SerializedName("deliveryId") val deliveryId: String,
    @SerializedName("itemDescription") val itemDescription: String,
    @SerializedName("pickupBuilding") val pickupBuilding: String,
    @SerializedName("dropoffBuilding") val dropoffBuilding: String,
    @SerializedName("rewardAmount") val rewardAmount: Double,
    @SerializedName("distanceKm") val distanceKm: Double,
    @SerializedName("status") val status: String
)
