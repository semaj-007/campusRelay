package com.example.campusrelay.ui.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrelay.data.model.ListingCategory
import com.example.campusrelay.data.model.MarketplaceListing
import com.example.campusrelay.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** ViewModel for the Marketplace feature. */
class MarketplaceViewModel(
    private val marketplaceRepository: MarketplaceRepository
) : ViewModel() {

    private val _listings = MutableStateFlow<List<MarketplaceListing>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<ListingCategory?>(null)

    val uiState: StateFlow<MarketplaceUiState> = combine(
        _listings, _isLoading, _searchQuery, _selectedCategory
    ) { listings, isLoading, searchQuery, selectedCategory ->
        val filtered = listings.filter { listing ->
            (selectedCategory == null || listing.category == selectedCategory) &&
                    (searchQuery.isBlank() || 
                     listing.title.contains(searchQuery, ignoreCase = true) ||
                     listing.description.contains(searchQuery, ignoreCase = true))
        }
        MarketplaceUiState(filtered, isLoading)
    }.asStateFlow()

    init {
        loadListings()
    }

    private fun loadListings() {
        viewModelScope.launch {
            _isLoading.value = true
            _listings.value = marketplaceRepository.getAllListings()
            _isLoading.value = false
        }
    }

    fun refresh() {
        loadListings()
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun filterByCategory(category: ListingCategory?) {
        _selectedCategory.value = category
    }
}

data class MarketplaceUiState(
    val listings: List<MarketplaceListing>,
    val isLoading: Boolean
)
