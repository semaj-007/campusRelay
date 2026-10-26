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
    private val preferencesManager: PreferencesManager,
    private val scheduleSync: () -> Unit
) {
    val pendingRequests = pendingDeliveryRequestDao.observeAll()

    /** Home Dashboard feed: try the network, cache what comes back, and fall back to the
     * local cache when offline. */
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
                    status = runCatching { DeliveryStatus.valueOf(it.status.uppercase(Locale.ROOT)) }.getOrDefault(DeliveryStatus.ACTIVE)
                )
            }
            cachedDeliveryDao.replaceAll(items.map(::toEntity))
            Resource.Success(items)
        } catch (t: retrofit2.HttpException) {
            Resource.Success(cachedDeliveryDao.getAll().map(::toModel), fromCache = true)
        } catch (t: java.io.IOException) {
            val cached = cachedDeliveryDao.getAll()
            val items = cached.map(::toModel)
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
        val senderId = preferencesManager.currentUserId()
            ?: return Resource.Error("Sign in before creating a delivery")
        val request = PendingDeliveryRequestEntity(
            localId = UUID.randomUUID().toString(), senderId = senderId,
            pickupBuilding = pickupBuilding, dropoffBuilding = dropoffBuilding,
            itemDescription = itemDescription, weightCategory = weightCategory,
            rewardAmount = rewardAmount, isEcoFriendlyRoute = isEcoFriendlyRoute,
            createdAtIso = java.time.Instant.now().toString()
        )
        // Durable outbox first: process death or a lost HTTP response cannot lose the action.
        pendingDeliveryRequestDao.enqueue(request)
        scheduleSync()
        if (preferencesManager.currentSessionToken() == null) return Resource.Success(false)
        return try {
            send(request)
            pendingDeliveryRequestDao.delete(request)
            awardEcoScoreIfApplicable(pickupBuilding, dropoffBuilding, isEcoFriendlyRoute)
            Resource.Success(true)
        } catch (e: kotlinx.coroutines.CancellationException) { throw e
        } catch (e: retrofit2.HttpException) {
            if (e.code() in listOf(401, 403) || e.code() == 408 || e.code() == 429 || e.code() >= 500) Resource.Success(false)
            else {
                pendingDeliveryRequestDao.markFailed(request.localId, "HTTP ${e.code()}")
                Resource.Error("Request rejected: HTTP ${e.code()}")
            }
        } catch (e: java.io.IOException) { Resource.Success(false) }
    }

    private suspend fun send(request: PendingDeliveryRequestEntity) {
        check(preferencesManager.currentSessionToken() != null) { "Unlock your session before syncing" }
        apiService.createDeliveryRequest(CreateDeliveryRequestDto(
            senderId = request.senderId, pickupBuilding = request.pickupBuilding,
            dropoffBuilding = request.dropoffBuilding, itemDescription = request.itemDescription,
            weightCategory = request.weightCategory, rewardAmount = request.rewardAmount,
            isEcoFriendlyRoute = request.isEcoFriendlyRoute, clientRequestId = request.localId
        ))
    }

    /** Only the signed-in account's queue is sent. Rejected rows remain available locally. */
    suspend fun flushPendingRequests(): Int {
        val user = preferencesManager.currentUserId() ?: return 0
        return com.example.campusrelay.sync.flushDeliveryOutbox(
            pendingDeliveryRequestDao.getAll().filter { it.senderId == user && it.lastError == null },
            send = ::send,
            acknowledge = { pendingDeliveryRequestDao.delete(it) },
            reject = { request, error -> pendingDeliveryRequestDao.markFailed(request.localId, error) }
        )
    }

    // REQ-ECO-1 / REQ-ECO-2
    private suspend fun awardEcoScoreIfApplicable(pickup: String, dropoff: String, isEcoFriendlyRoute: Boolean) {
        if (!isEcoFriendlyRoute) return
        val distanceKm = CampusBuildings.distanceKmBetween(pickup, dropoff)
        val kgSaved = EcoScoreCalculator.forDelivery(distanceKm)
        preferencesManager.addEcoScore(kgSaved)
    }

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
