package com.example.campusrelay.data.model

/** Mirrors the `users` table from the Part 1 data model. */
data class User(
    val id: String,
    val ssoSub: String,
    val email: String,
    val fullName: String,
    val avatarUrl: String? = null,
    val ratingSum: Int = 0,
    val ratingCount: Int = 0,
    val ecoScore: Int = 0,
    val walletBalance: Double = 0.0
) {
    val averageRating: Double
        get() = if (ratingCount == 0) 0.0 else ratingSum.toDouble() / ratingCount
}
