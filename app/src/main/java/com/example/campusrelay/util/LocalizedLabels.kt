package com.example.campusrelay.util

import android.content.Context
import com.example.campusrelay.R
import com.example.campusrelay.data.model.*

fun Context.categoryLabel(value: ListingCategory): String = getString(when (value) {
    ListingCategory.BOOKS -> R.string.marketplace_filter_books
    ListingCategory.FURNITURE -> R.string.marketplace_filter_furniture
    ListingCategory.ELECTRONICS -> R.string.marketplace_filter_electronics
    ListingCategory.CLOTHING -> R.string.marketplace_filter_clothing
    ListingCategory.OTHER -> R.string.marketplace_filter_other
})
fun Context.conditionLabel(value: ListingCondition): String = getString(when (value) {
    ListingCondition.NEW -> R.string.condition_new
    ListingCondition.LIKE_NEW -> R.string.condition_like_new
    ListingCondition.GOOD -> R.string.condition_good
    ListingCondition.FAIR -> R.string.condition_fair
})
fun Context.vehicleLabel(value: VehicleType): String = getString(when (value) {
    VehicleType.CAR -> R.string.vehicle_car
    VehicleType.SUV -> R.string.vehicle_suv
    VehicleType.VAN -> R.string.vehicle_van
})
