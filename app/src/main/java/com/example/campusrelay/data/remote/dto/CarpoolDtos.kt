package com.example.campusrelay.data.remote.dto

import com.example.campusrelay.data.model.RideOfferStatus
import com.example.campusrelay.data.model.VehicleType

/** DTOs for Carpool API endpoints. */
object CarpoolDtos {

    data class RideOfferDto(
        val id: String,
        val driverId: String,
        val originCity: String,
        val destinationCity: String,
        val departureTimeMillis: Long,
        val availableSeats: Int,
        val vehicleType: VehicleType,
        val pricePerSeat: Double,
        val isRecurring: Boolean = false,
        val recurrenceRule: String? = null,
        val status: RideOfferStatus = RideOfferStatus.ACTIVE
    )

    data class CreateRideOfferRequest(
        val originCity: String,
        val destinationCity: String,
        val departureTimeMillis: Long,
        val availableSeats: Int,
        val vehicleType: VehicleType,
        val pricePerSeat: Double,
        val isRecurring: Boolean = false,
        val recurrenceRule: String? = null
    )

    data class CreateRideOfferResponse(
        val id: String,
        val driverId: String,
        val originCity: String,
        val destinationCity: String,
        val departureTimeMillis: Long,
        val availableSeats: Int,
        val vehicleType: VehicleType,
        val pricePerSeat: Double,
        val isRecurring: Boolean = false,
        val recurrenceRule: String? = null,
        val status: RideOfferStatus = RideOfferStatus.ACTIVE
    )

    data class RequestSeatRequest(
        val rideId: String,
        val passengerId: String
    )

    data class RequestSeatResponse(
        val requestId: String,
        val rideId: String,
        val passengerId: String,
        val status: String
    )
}
