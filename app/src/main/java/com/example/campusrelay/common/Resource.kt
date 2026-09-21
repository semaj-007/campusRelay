package com.example.campusrelay.common

/**
 * Thin wrapper so repositories can tell the UI layer whether data came back fine,
 * came from local cache because the network failed, or failed outright.
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T, val fromCache: Boolean = false) : Resource<T>()
    data class Error(val message: String, val cachedData: Any? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}
