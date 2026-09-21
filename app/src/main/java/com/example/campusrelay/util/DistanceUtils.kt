package com.example.campusrelay.util

import com.example.campusrelay.data.model.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** REQ-DEL-3: geospatial helper for matching requests to offers within a radius. */
object DistanceUtils {

    private const val EARTH_RADIUS_KM = 6371.0

    fun haversineKm(a: GeoPoint, b: GeoPoint): Double {
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)

        val h = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(h), sqrt(1 - h))
        return EARTH_RADIUS_KM * c
    }

    fun isWithinRadius(a: GeoPoint, b: GeoPoint, radiusKm: Double): Boolean =
        haversineKm(a, b) <= radiusKm
}
