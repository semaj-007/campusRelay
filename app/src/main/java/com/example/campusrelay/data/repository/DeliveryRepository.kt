package com.example.campusrelay.data.repository

import com.example.campusrelay.common.PreferencesManager
import com.example.campusrelay.common.Resource
import com.example.campusrelay.data.local.dao.CachedDeliveryDao
import com.example.campusrelay.data.local.dao.PendingDeliveryRequestDao
import com.example.campusrelay.data.local.entity.CachedDeliveryEntity
import com.example.campusrelay.data.local.entity.PendingDeliveryRequestEntity
import com.example.campusrelay.data.model.DeliveryFeedItem
import com.example.campusrelay.data.model.DeliveryStatus
import com.example.campusrelay.data.remote.ApiService
import com.example.campusrelay.data.remote.dto.CreateDeliveryRequestDto
import com.example.campusrelay.util.CampusBuildings
import com.example.campusrelay.util.EcoScoreCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Mediates between the REST API (REQ-API-1/2), the RoomDB cache (REQ-OFF-1) and the
 * offline queue (REQ-OFF-2) for everything delivery-related (REQ-DEL-1..4).
 */
class DeliveryRepository(
    private val apiService: ApiService,
    private val cachedDeliveryDao: CachedDeliveryDao,
    private val pendingDeliveryRequestDao: PendingDeliveryRequestDao,
    private val preferencesManager: PreferencesManager
) {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    /** Home Dashboard feed: try the network, cache what comes back, and fall back to the
     * local cache (or, on a completely fresh install, a small seed list) when offline. */
    suspend fun getFeed(): Resource<List<DeliveryFeedItem>> {
        return try {
            val remote = apiService.getDeliveryFeed()
            val items = remote.map {
                DeliveryFeedItem(
                    deliveryId = it.deliveryId,
                    itemDescription = it.itemDescription,
                    pickupBuildingName = it.pickupBuilding,
                    dropoffBuildingName = it.dropoffBuilding,
                    rewardAmount = it.rewardAmount,
                    distanceKm = it.distanceKm,
                    status = runCatching { DeliveryStatus.valueOf(it.status) }.getOrDefault(DeliveryStatus.ACTIVE)
                )
            }
            cachedDeliveryDao.upsertAll(items.map(::toEntity))
            Resource.Success(items)
        } catch (t: Throwable) {
            val cached = cachedDeliveryDao.getAll()
            val items = if (cached.isNotEmpty()) cached.map(::toModel) else seedFeed()
            Resource.Success(items, fromCache = true)
        }
    }

    /** REQ-DEL-1: create a delivery request; queue it locally if the network isn't reachable. */
    suspend fun createDeliveryRequest(
        pickupBuilding: String,
        dropoffBuilding: String,
        itemDescription: String,
        weightCategory: String,
        rewardAmount: Double,
        isEcoFriendlyRoute: Boolean
    ): Resource<Boolean> {
        val senderId = "usr_local_demo"
        val dto = CreateDeliveryRequestDto(
            senderId = senderId,
            pickupBuilding = pickupBuilding,
            dropoffBuilding = dropoffBuilding,
            itemDescription = itemDescription,
            weightCategory = weightCategory,
            rewardAmount = rewardAmount,
            isEcoFriendlyRoute = isEcoFriendlyRoute
        )

        return try {
            apiService.createDeliveryRequest(dto)
            awardEcoScoreIfApplicable(pickupBuilding, dropoffBuilding, isEcoFriendlyRoute)
            Resource.Success(true)
        } catch (t: Throwable) {
            pendingDeliveryRequestDao.enqueue(
                PendingDeliveryRequestEntity(
                    localId = UUID.randomUUID().toString(),
                    senderId = senderId,
                    pickupBuilding = pickupBuilding,
                    dropoffBuilding = dropoffBuilding,
                    itemDescription = itemDescription,
                    weightCategory = weightCategory,
                    rewardAmount = rewardAmount,
                    isEcoFriendlyRoute = isEcoFriendlyRoute,
                    createdAtIso = isoFormat.format(Date())
                )
            )
            awardEcoScoreIfApplicable(pickupBuilding, dropoffBuilding, isEcoFriendlyRoute)
            Resource.Success(false) // false == queued offline, not yet confirmed by the server
        }
    }

    /** Called by [com.example.campusrelay.sync.OfflineSyncWorker] once connectivity returns. */
    suspend fun flushPendingRequests(): Int {
        val pending = pendingDeliveryRequestDao.getAll()
        var flushed = 0
        for (request in pending) {
            val dto = CreateDeliveryRequestDto(
                senderId = request.senderId,
                pickupBuilding = request.pickupBuilding,
                dropoffBuilding = request.dropoffBuilding,
                itemDescription = request.itemDescription,
                weightCategory = request.weightCategory,
                rewardAmount = request.rewardAmount,
                isEcoFriendlyRoute = request.isEcoFriendlyRoute
            )
            val succeeded = runCatching { apiService.createDeliveryRequest(dto) }.isSuccess
            if (succeeded) {
                pendingDeliveryRequestDao.delete(request)
                flushed++
            }
        }
        return flushed
    }

    // REQ-ECO-1 / REQ-ECO-2
    private suspend fun awardEcoScoreIfApplicable(pickup: String, dropoff: String, isEcoFriendlyRoute: Boolean) {
        if (!isEcoFriendlyRoute) return
        val distanceKm = CampusBuildings.distanceKmBetween(pickup, dropoff)
        val kgSaved = EcoScoreCalculator.forDelivery(distanceKm)
        preferencesManager.addEcoScore(kgSaved)
    }

    private fun seedFeed(): List<DeliveryFeedItem> = listOf(
        DeliveryFeedItem("del_seed_1", "PROG7311 Textbook & Notes", "Engineering Block B", "Merensky Library", 35.0, 0.6, DeliveryStatus.ACTIVE),
        DeliveryFeedItem("del_seed_2", "Forgotten laptop charger", "Res Village A", "Client Service Centre", 25.0, 1.4, DeliveryStatus.ACTIVE),
        DeliveryFeedItem("del_seed_3", "Signed permission form", "Student Centre", "Law Building", 15.0, 0.9, DeliveryStatus.MATCHED)
    )

    private fun toEntity(item: DeliveryFeedItem) = CachedDeliveryEntity(
        deliveryId = item.deliveryId,
        itemDescription = item.itemDescription,
        pickupBuilding = item.pickupBuildingName,
        dropoffBuilding = item.dropoffBuildingName,
        rewardAmount = item.rewardAmount,
        distanceKm = item.distanceKm,
        status = item.status.name
    )

    private fun toModel(entity: CachedDeliveryEntity) = DeliveryFeedItem(
        deliveryId = entity.deliveryId,
        itemDescription = entity.itemDescription,
        pickupBuildingName = entity.pickupBuilding,
        dropoffBuildingName = entity.dropoffBuilding,
        rewardAmount = entity.rewardAmount,
        distanceKm = entity.distanceKm,
        status = runCatching { DeliveryStatus.valueOf(entity.status) }.getOrDefault(DeliveryStatus.ACTIVE)
    )
}
