package com.example.campusrelay.ui.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrelay.common.Resource
import com.example.campusrelay.data.repository.DeliveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SubmitState {
    data object Idle : SubmitState()
    data object Loading : SubmitState()
    data class Submitted(val queuedOffline: Boolean) : SubmitState()
    data class Error(val message: String) : SubmitState()
}

class CreateDeliveryViewModel(private val deliveryRepository: DeliveryRepository) : ViewModel() {

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    // REQ-DEL-1
    fun submit(
        pickupBuilding: String?,
        dropoffBuilding: String?,
        itemDescription: String,
        weightCategory: String,
        tipAmount: Double
    ) {
        if (pickupBuilding.isNullOrBlank() || dropoffBuilding.isNullOrBlank() || itemDescription.isBlank()) {
            _submitState.value = SubmitState.Error("validation")
            return
        }

        _submitState.value = SubmitState.Loading
        viewModelScope.launch {
            val result = deliveryRepository.createDeliveryRequest(
                pickupBuilding = pickupBuilding,
                dropoffBuilding = dropoffBuilding,
                itemDescription = itemDescription,
                weightCategory = weightCategory,
                rewardAmount = tipAmount,
                // Every CampusRelay delivery moves on foot or by bike between buildings on
                // the same campus, so it's eco-friendly by construction (matches the sample
                // payload in the Part 1 spec, which also sets this true).
                isEcoFriendlyRoute = true
            )
            _submitState.value = when (result) {
                is Resource.Success -> SubmitState.Submitted(queuedOffline = !result.data)
                is Resource.Error -> SubmitState.Error(result.message)
                Resource.Loading -> SubmitState.Loading
            }
        }
    }

    fun resetState() {
        _submitState.value = SubmitState.Idle
    }
}
