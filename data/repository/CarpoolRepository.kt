package com.example.campusrelay.data.repository

import com.example.campusrelay.data.model.RideOffer
import com.example.campusrelay.data.remote.ApiService
import com.example.campusrelay.data.remote.dto.CarpoolDtos
import java.util.Date

/**
 * Repository for carpool operations (REQ-CAR-1, REQ-CAR-2, REQ-CAR-3).
 */
class CarpoolRepository(
    private val apiService: ApiService
) {

    /** Get all ride offers. */
    suspend fun getAllRideOffers(): List<RideOffer> {
        return try {
            val response = apiService.getRideOffers()
            response.map { dto ->
                RideOffer(
                    id = dto.id,
                    driverId = dto.driverId,
                    originCity = dto.originCity,
                    destinationCity = dto.destinationCity,
                    departureTime = Date(dto.departureTimeMillis),
                    availableSeats = dto.availableSeats,
                    vehicleType = dto.vehicleType,
                    pricePerSeat = dto.pricePerSeat,
                    isRecurring = dto.isRecurring,
                    recurrenceRule = dto.recurrenceRule,
                    status = dto.status
                )
            }
        } catch (t: Throwable) {
            emptyList() // Return empty list on error
        }
    }

    /** Get a specific ride offer by ID. */
    suspend fun getRideOfferById(rideId: String): RideOffer? {
        return try {
            val response = apiService.getRideOfferById(rideId)
            RideOffer(
                id = response.id,
                driverId = response.driverId,
                originCity = response.originCity,
                destinationCity = response.destinationCity,
                departureTime = Date(response.departureTimeMillis),
                availableSeats = response.availableSeats,
                vehicleType = response.vehicleType,
                pricePerSeat = response.pricePerSeat,
                isRecurring = response.isRecurring,
                recurrenceRule = response.recurrenceRule,
                status = response.status
            )
        } catch (t: Throwable) {
            null
        }
    }

    /** Create a new ride offer. */
    suspend fun createRideOffer(rideOffer: RideOffer) {
        val dto = CarpoolDtos.CreateRideOfferRequest(
            originCity = rideOffer.originCity,
            destinationCity = rideOffer.destinationCity,
            departureTimeMillis = rideOffer.departureTime.time,
            availableSeats = rideOffer.availableSeats,
            vehicleType = rideOffer.vehicleType,
            pricePerSeat = rideOffer.pricePerSeat,
            isRecurring = rideOffer.isRecurring,
            recurrenceRule = rideOffer.recurrenceRule
        )
        apiService.createRideOffer(dto)
    }

    /** Search ride offers by origin, destination, and date. */
    suspend fun searchRideOffers(
        origin: String,
        destination: String,
        date: Date?
    ): List<RideOffer> {
        return getAllRideOffers().filter { ride ->
            (origin.isBlank() || ride.originCity.contains(origin, ignoreCase = true)) &&
                    (destination.isBlank() || ride.destinationCity.contains(destination, ignoreCase = true)) &&
                    (date == null || ride.departureTime == date)
        }.sortedBy { it.departureTime }
    }

    /** Request a seat on a ride offer. */
    suspend fun requestSeat(rideId: String, passengerId: String): Boolean {
        return try {
            val dto = CarpoolDtos.RequestSeatRequest(
                rideId = rideId,
                passengerId = passengerId
            )
            apiService.requestSeat(dto)
            true
        } catch (t: Throwable) {
            false
        }
    }
}
