package com.example.campusrelay.ui.carpool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrelay.data.model.RideOffer
import com.example.campusrelay.data.repository.CarpoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Date

/** ViewModel for the Carpool feature. */
class CarpoolViewModel(
    private val carpoolRepository: CarpoolRepository
) : ViewModel() {

    private val _rideOffers = MutableStateFlow<List<RideOffer>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _searchOrigin = MutableStateFlow("")
    private val _searchDestination = MutableStateFlow("")
    private val _searchDate = MutableStateFlow<Date?>(null)

    val uiState: StateFlow<CarpoolUiState> = combine(
        _rideOffers, _isLoading, _searchOrigin, _searchDestination, _searchDate
    ) { rideOffers, isLoading, origin, destination, date ->
        val filtered = rideOffers.filter { ride ->
            (origin.isBlank() || ride.originCity.contains(origin, ignoreCase = true)) &&
                    (destination.isBlank() || ride.destinationCity.contains(destination, ignoreCase = true)) &&
                    (date == null || ride.departureTime == date)
        }.sortedBy { it.departureTime }
        CarpoolUiState(filtered, isLoading)
    }.asStateFlow()

    init {
        loadRideOffers()
    }

    private fun loadRideOffers() {
        viewModelScope.launch {
            _isLoading.value = true
            _rideOffers.value = carpoolRepository.getAllRideOffers()
            _isLoading.value = false
        }
    }

    fun refresh() {
        loadRideOffers()
    }

    fun searchRides(origin: String, destination: String, date: Date?) {
        _searchOrigin.value = origin
        _searchDestination.value = destination
        _searchDate.value = date
    }
}

data class CarpoolUiState(
    val rideOffers: List<RideOffer>,
    val isLoading: Boolean
)
