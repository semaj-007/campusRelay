package com.example.campusrelay.data.model

/** Mirrors the `marketplace_listings` table. */
data class MarketplaceListing(
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
