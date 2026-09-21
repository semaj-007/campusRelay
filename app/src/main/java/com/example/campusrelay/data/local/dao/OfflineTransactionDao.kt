package com.example.campusrelay.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.campusrelay.data.local.entity.OfflineTransactionEntity

@Dao
interface OfflineTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(transaction: OfflineTransactionEntity)

    @Query("SELECT * FROM offline_transactions WHERE synced = 0")
    suspend fun getPending(): List<OfflineTransactionEntity>

    @Query("UPDATE offline_transactions SET synced = 1 WHERE localTransactionId IN (:localIds)")
    suspend fun markSynced(localIds: List<String>)

    @Query("SELECT COUNT(*) FROM offline_transactions WHERE synced = 0")
    suspend fun pendingCount(): Int
}
