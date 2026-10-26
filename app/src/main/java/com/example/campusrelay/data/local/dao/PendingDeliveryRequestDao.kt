package com.example.campusrelay.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.campusrelay.data.local.entity.PendingDeliveryRequestEntity

@Dao
interface PendingDeliveryRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(request: PendingDeliveryRequestEntity)

    @Query("SELECT * FROM pending_delivery_requests")
    suspend fun getAll(): List<PendingDeliveryRequestEntity>

    @Query("UPDATE pending_delivery_requests SET lastError = :error WHERE localId = :id")
    suspend fun markFailed(id: String, error: String)

    @Query("SELECT * FROM pending_delivery_requests ORDER BY createdAtIso")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<PendingDeliveryRequestEntity>>

    @Delete
    suspend fun delete(request: PendingDeliveryRequestEntity)

    @Query("SELECT COUNT(*) FROM pending_delivery_requests")
    suspend fun pendingCount(): Int
}
