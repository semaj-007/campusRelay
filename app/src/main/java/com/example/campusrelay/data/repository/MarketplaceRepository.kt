package com.example.campusrelay.data.repository

import com.example.campusrelay.data.model.MarketplaceListing
import com.example.campusrelay.data.remote.ApiService
import com.example.campusrelay.data.remote.dto.MarketplaceDtos

/**
 * Repository for marketplace operations (REQ-MKT-1, REQ-MKT-2, REQ-MKT-3).
 */
class MarketplaceRepository(
    private val apiService: ApiService
) {

    /** Get all marketplace listings. */
    suspend fun getAllListings(): List<MarketplaceListing> {
        return try {
            val response = apiService.getMarketplaceListings()
            response.map { dto ->
                MarketplaceListing(
                    id = dto.id,
                    sellerId = dto.sellerId,
                    title = dto.title,
                    description = dto.description,
                    price = dto.price,
                    category = dto.category,
                    condition = dto.condition,
                    photoUrls = dto.photoUrls,
                    status = dto.status
                )
            }
        } catch (t: Throwable) {
            emptyList() // Return empty list on error
        }
    }

    /** Get a specific listing by ID. */
    suspend fun getListingById(listingId: String): MarketplaceListing? {
        return try {
            val response = apiService.getMarketplaceListingById(listingId)
            MarketplaceListing(
                id = response.id,
                sellerId = response.sellerId,
                title = response.title,
                description = response.description,
                price = response.price,
                category = response.category,
                condition = response.condition,
                photoUrls = response.photoUrls,
                status = response.status
            )
        } catch (t: Throwable) {
            null
        }
    }

    /** Create a new marketplace listing. */
    suspend fun createListing(listing: MarketplaceListing) {
        val dto = MarketplaceDtos.CreateListingRequest(
            title = listing.title,
            description = listing.description,
            price = listing.price,
            category = listing.category,
            condition = listing.condition,
            photoUrls = listing.photoUrls
        )
        apiService.createMarketplaceListing(dto)
    }

    /** Search listings by query. */
    suspend fun searchListings(query: String): List<MarketplaceListing> {
        return getAllListings().filter { listing ->
            listing.title.contains(query, ignoreCase = true) ||
                    listing.description.contains(query, ignoreCase = true)
        }
    }

    /** Filter listings by category. */
    suspend fun getListingsByCategory(category: com.example.campusrelay.data.model.ListingCategory): List<MarketplaceListing> {
        return getAllListings().filter { it.category == category }
    }
}
