package com.example.campusrelay.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** REQ-OFF-1: local cache of active delivery jobs so the Home Dashboard has something to show offline. */
@Entity(tableName = "cached_deliveries")
data class CachedDeliveryEntity(
    @PrimaryKey val deliveryId: String,
    val itemDescription: String,
    val pickupBuilding: String,
    val dropoffBuilding: String,
    val rewardAmount: Double,
    val distanceKm: Double,
    val status: String
)
