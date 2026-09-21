package com.example.campusrelay.data.model

import java.util.Date

/** Mirrors the `delivery_requests` table and REQ-DEL-1. */
data class DeliveryRequest(
    val id: String,
    val requesterId: String,
    val itemDescription: String,
    val itemPhotoUrls: List<String> = emptyList(),
    val weight: Weight,
    val pickup: GeoPoint,
    val dropoff: GeoPoint,
    val pickupBuildingName: String? = null,
    val dropoffBuildingName: String? = null,
    val windowStart: Date,
    val windowEnd: Date,
    val rewardAmount: Double = 0.0,
    val isEcoFriendlyRoute: Boolean = false,
    val status: DeliveryStatus = DeliveryStatus.ACTIVE
)

/** A lightweight projection of a delivery request used for the home feed / nearby list. */
data class DeliveryFeedItem(
    val deliveryId: String,
    val itemDescription: String,
    val pickupBuildingName: String,
    val dropoffBuildingName: String,
    val rewardAmount: Double,
    val distanceKm: Double,
    val status: DeliveryStatus
)
