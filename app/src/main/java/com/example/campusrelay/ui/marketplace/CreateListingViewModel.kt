package com.example.campusrelay.ui.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrelay.data.model.ListingCategory
import com.example.campusrelay.data.model.ListingCondition
import com.example.campusrelay.data.model.ListingStatus
import com.example.campusrelay.data.model.MarketplaceListing
import com.example.campusrelay.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/** ViewModel for creating marketplace listings. */
class CreateListingViewModel(
    private val marketplaceRepository: MarketplaceRepository
) : ViewModel() {

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    fun createListing(
        title: String,
        description: String,
        price: Double,
        category: ListingCategory,
        condition: ListingCondition,
        photoUrls: List<String>
    ) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            try {
                val listing = MarketplaceListing(
                    id = UUID.randomUUID().toString(),
                    sellerId = "current_user_id", // Will be replaced with actual user ID
                    title = title,
                    description = description,
                    price = price,
                    category = category,
                    condition = condition,
                    photoUrls = photoUrls,
                    status = ListingStatus.ACTIVE
                )
                marketplaceRepository.createListing(listing)
                _submitState.value = SubmitState.Success
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _submitState.value = SubmitState.Idle
    }
}
