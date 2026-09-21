package com.example.campusrelay.data.model

/** Matches the pickup/drop-off "custom object: {type: Point, coordinates: [lng, lat]}" from REQ-DEL-1. */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

/** Matches the item weight "custom object: {value, unit}" from REQ-DEL-1. */
data class Weight(
    val value: Double,
    val unit: String = "kg"
)

enum class DeliveryStatus { ACTIVE, MATCHED, FULFILLED, CANCELLED }

enum class ListingCategory { BOOKS, FURNITURE, ELECTRONICS, CLOTHING, OTHER }
enum class ListingCondition { NEW, LIKE_NEW, GOOD, FAIR }
enum class ListingStatus { ACTIVE, PENDING, SOLD, EXPIRED, HOLD }

enum class VehicleType { CAR, SUV, VAN }
enum class RideOfferStatus { ACTIVE, FULL, CANCELLED }

enum class TransactionType { DELIVERY, MARKETPLACE, CARPOOL }
enum class TransactionStatus { PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED, DISPUTED }
enum class EscrowStatus { NOT_FUNDED, FUNDED, RELEASED, REFUNDED }

enum class ModerationReasonCode { SPAM, FRAUD, HARASSMENT, INAPPROPRIATE_CONTENT, OTHER }
enum class ModerationStatus { OPEN, UNDER_REVIEW, RESOLVED, DISMISSED }
