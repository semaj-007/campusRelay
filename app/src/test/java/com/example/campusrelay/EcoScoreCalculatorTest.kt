package com.example.campusrelay

import com.example.campusrelay.data.model.GeoPoint
import com.example.campusrelay.data.model.ListingCategory
import com.example.campusrelay.util.DistanceUtils
import com.example.campusrelay.util.EcoScoreCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the REQ-ECO-1 estimate logic and the REQ-DEL-3 distance helper. */
class EcoScoreCalculatorTest {

    @Test
    fun `delivery savings scale linearly with distance`() {
        val savings = EcoScoreCalculator.forDelivery(distanceKm = 2.0)
        assertEquals(0.384, savings, 0.0001)
    }

    @Test
    fun `carpool savings scale with shared passengers`() {
        val onePassenger = EcoScoreCalculator.forCarpool(distanceKm = 5.0, sharedPassengers = 1)
        val threePassengers = EcoScoreCalculator.forCarpool(distanceKm = 5.0, sharedPassengers = 3)
        assertEquals(onePassenger * 3, threePassengers, 0.0001)
    }

    @Test
    fun `marketplace savings vary by category`() {
        val books = EcoScoreCalculator.forMarketplace(ListingCategory.BOOKS)
        val electronics = EcoScoreCalculator.forMarketplace(ListingCategory.ELECTRONICS)
        assertTrue(electronics > books)
    }
}

class DistanceUtilsTest {

    @Test
    fun `distance to self is zero`() {
        val point = GeoPoint(-25.7545, 28.2314)
        assertEquals(0.0, DistanceUtils.haversineKm(point, point), 0.0001)
    }

    @Test
    fun `nearby campus buildings are within the 1km match radius`() {
        val engineering = GeoPoint(-25.7545, 28.2314)
        val library = GeoPoint(-25.7534, 28.2309)
        assertTrue(DistanceUtils.isWithinRadius(engineering, library, radiusKm = 1.0))
    }
}
