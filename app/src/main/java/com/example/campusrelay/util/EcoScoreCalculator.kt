package com.example.campusrelay.util

import com.example.campusrelay.data.model.ListingCategory
import com.example.campusrelay.data.model.TransactionType

/**
 * REQ-ECO-1: estimate CO2 saved for a completed transaction.
 *
 * The numbers below are simple, documented estimates for a student prototype — not a
 * scientifically validated emissions model:
 *  - Delivery: walking/cycling a parcel instead of a solo car trip saves ~0.192 kg CO2 per km
 *    (average passenger car tailpipe emissions figure).
 *  - Carpool: each extra passenger who didn't drive separately saves the same per-km figure,
 *    multiplied by the distance actually shared.
 *  - Marketplace (buying used instead of new): a flat estimate per category standing in for
 *    the embodied manufacturing carbon that's avoided.
 */
object EcoScoreCalculator {

    private const val KG_CO2_PER_KM_CAR = 0.192

    private val MARKETPLACE_ESTIMATE_KG = mapOf(
        ListingCategory.BOOKS to 2.5,
        ListingCategory.FURNITURE to 45.0,
        ListingCategory.ELECTRONICS to 60.0,
        ListingCategory.CLOTHING to 8.0,
        ListingCategory.OTHER to 5.0
    )

    fun forDelivery(distanceKm: Double): Double = distanceKm * KG_CO2_PER_KM_CAR

    fun forCarpool(distanceKm: Double, sharedPassengers: Int): Double =
        distanceKm * KG_CO2_PER_KM_CAR * sharedPassengers.coerceAtLeast(1)

    fun forMarketplace(category: ListingCategory): Double = MARKETPLACE_ESTIMATE_KG[category] ?: 5.0

    fun forTransaction(type: TransactionType, distanceKm: Double = 0.0, sharedPassengers: Int = 1, category: ListingCategory? = null): Double =
        when (type) {
            TransactionType.DELIVERY -> forDelivery(distanceKm)
            TransactionType.CARPOOL -> forCarpool(distanceKm, sharedPassengers)
            TransactionType.MARKETPLACE -> forMarketplace(category ?: ListingCategory.OTHER)
        }
}
