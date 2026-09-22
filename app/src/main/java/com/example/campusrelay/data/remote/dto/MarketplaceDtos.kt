package com.example.campusrelay.data.remote.dto

import com.example.campusrelay.data.model.ListingCategory
import com.example.campusrelay.data.model.ListingCondition
import com.example.campusrelay.data.model.ListingStatus

/** DTOs for Marketplace API endpoints. */
object MarketplaceDtos {

    data class MarketplaceListingDto(
        val id: String,
        val sellerId: String,
        val title: String,
        val description: String,
        val price: Double,
        val category: ListingCategory,
        val condition: ListingCondition,
        val photoUrls: List<String> = emptyList(),
        val status: ListingStatus = ListingStatus.ACTIVE
    )

    data class CreateListingRequest(
        val title: String,
        val description: String,
        val price: Double,
        val category: ListingCategory,
        val condition: ListingCondition,
        val photoUrls: List<String> = emptyList()
    )

    data class CreateListingResponse(
        val id: String,
        val sellerId: String,
        val title: String,
        val description: String,
        val price: Double,
        val category: ListingCategory,
        val condition: ListingCondition,
        val photoUrls: List<String> = emptyList(),
        val status: ListingStatus = ListingStatus.ACTIVE
    )
}
