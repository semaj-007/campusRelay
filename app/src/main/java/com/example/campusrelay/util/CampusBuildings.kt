package com.example.campusrelay.util

import com.example.campusrelay.data.model.GeoPoint

/**
 * A small fixed set of campus buildings used to populate the pickup/drop-off dropdowns
 * (REQ-DEL-1) and to derive a realistic-looking distance for the eco-score calculation
 * and the home feed. The coordinates are illustrative offsets around a fictitious campus
 * centre point, not a real surveyed campus map.
 */
object CampusBuildings {

    data class Building(val name: String, val location: GeoPoint)

    val ALL: List<Building> = listOf(
        Building("Engineering Block B", GeoPoint(-25.7545, 28.2314)),
        Building("Merensky Library", GeoPoint(-25.7534, 28.2309)),
        Building("Client Service Centre", GeoPoint(-25.7551, 28.2325)),
        Building("Student Centre", GeoPoint(-25.7529, 28.2331)),
        Building("Res Village A", GeoPoint(-25.7561, 28.2298)),
        Building("Sports Grounds", GeoPoint(-25.7572, 28.2340)),
        Building("Law Building", GeoPoint(-25.7538, 28.2318))
    )

    val NAMES: List<String> = ALL.map { it.name }

    fun locationOf(name: String): GeoPoint? = ALL.firstOrNull { it.name == name }?.location

    fun distanceKmBetween(pickupName: String, dropoffName: String): Double {
        val pickup = locationOf(pickupName) ?: return 0.0
        val dropoff = locationOf(dropoffName) ?: return 0.0
        return DistanceUtils.haversineKm(pickup, dropoff)
    }
}
