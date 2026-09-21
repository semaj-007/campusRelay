package com.example.campusrelay.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.campusrelay.data.local.entity.CachedDeliveryEntity

@Dao
interface CachedDeliveryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(deliveries: List<CachedDeliveryEntity>)

    @Query("SELECT * FROM cached_deliveries ORDER BY distanceKm ASC")
    suspend fun getAll(): List<CachedDeliveryEntity>

    @Query("DELETE FROM cached_deliveries")
    suspend fun clear()
}
