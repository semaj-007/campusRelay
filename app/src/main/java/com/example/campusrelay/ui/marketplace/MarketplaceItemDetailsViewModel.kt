package com.example.campusrelay.ui.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrelay.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for marketplace item details. */
class MarketplaceItemDetailsViewModel(
    private val marketplaceRepository: MarketplaceRepository,
    private val listingId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<MarketplaceItemDetailsUiState>(MarketplaceItemDetailsUiState.Loading)
    val uiState: StateFlow<MarketplaceItemDetailsUiState> = _uiState.asStateFlow()

    init {
        loadListing()
    }

    private fun loadListing() {
        viewModelScope.launch {
            try {
                val listing = marketplaceRepository.getListingById(listingId)
                if (listing != null) {
                    _uiState.value = MarketplaceItemDetailsUiState.Success(listing)
                } else {
                    _uiState.value = MarketplaceItemDetailsUiState.Error("Listing not found")
                }
            } catch (e: Exception) {
                _uiState.value = MarketplaceItemDetailsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class MarketplaceItemDetailsUiState {
    object Loading : MarketplaceItemDetailsUiState()
    data class Success(val listing: com.example.campusrelay.data.model.MarketplaceListing) : MarketplaceItemDetailsUiState()
    data class Error(val message: String) : MarketplaceItemDetailsUiState()
}
