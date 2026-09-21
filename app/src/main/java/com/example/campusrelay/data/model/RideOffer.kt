package com.example.campusrelay.data.model

import java.util.Date

/** Mirrors the `ride_offers` table and REQ-CAR-1. */
data class RideOffer(
    val id: String,
    val driverId: String,
    val originCity: String,
    val destinationCity: String,
    val departureTime: Date,
    val availableSeats: Int,
    val vehicleType: VehicleType,
    val pricePerSeat: Double,
    val isRecurring: Boolean = false,
    val recurrenceRule: String? = null,
    val status: RideOfferStatus = RideOfferStatus.ACTIVE
)
