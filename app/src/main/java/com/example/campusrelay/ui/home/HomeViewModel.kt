package com.example.campusrelay.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusrelay.data.model.DeliveryFeedItem
import com.example.campusrelay.data.model.DeliveryStatus
import com.example.campusrelay.data.repository.DeliveryRepository
import com.example.campusrelay.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HomeFilter { DELIVERIES, REQUESTS, MY_ROUTES }

data class HomeUiState(
    val tasks: List<DeliveryFeedItem> = emptyList(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val filter: HomeFilter = HomeFilter.DELIVERIES
)

class HomeViewModel(
    private val deliveryRepository: DeliveryRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val allTasks = MutableStateFlow<List<DeliveryFeedItem>>(emptyList())
    private val isLoading = MutableStateFlow(false)
    private val isOffline = MutableStateFlow(false)
    private val filter = MutableStateFlow(HomeFilter.DELIVERIES)

    val uiState: StateFlow<HomeUiState> = combine(allTasks, isLoading, isOffline, filter) { tasks, loading, offline, currentFilter ->
        HomeUiState(
            tasks = applyFilter(tasks, currentFilter),
            isLoading = loading,
            isOffline = offline,
            filter = currentFilter
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    // REQ-ECO-3: eco_score breakdown shown on the profile / dashboard.
    val ecoScoreKg: StateFlow<Double> = settingsRepository.ecoScoreKg
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading.value = true
            when (val result = deliveryRepository.getFeed()) {
                is com.example.campusrelay.common.Resource.Success -> {
                    allTasks.value = result.data
                    isOffline.value = result.fromCache
                }
                else -> Unit
            }
            isLoading.value = false
        }
    }

    fun setFilter(newFilter: HomeFilter) {
        filter.value = newFilter
    }

    // "My Routes" (carpool matches on the user's own timetable/route) isn't wired up to a
    // real matching engine in this prototype yet — REQ-CAR-2/3 land in a later iteration.
    private fun applyFilter(tasks: List<DeliveryFeedItem>, currentFilter: HomeFilter): List<DeliveryFeedItem> =
        when (currentFilter) {
            HomeFilter.DELIVERIES -> tasks
            HomeFilter.REQUESTS -> tasks.filter { it.status == DeliveryStatus.ACTIVE }
            HomeFilter.MY_ROUTES -> emptyList()
        }
}
