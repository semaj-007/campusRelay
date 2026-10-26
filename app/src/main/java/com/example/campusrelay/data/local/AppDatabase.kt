package com.example.campusrelay.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.campusrelay.common.Constants
import com.example.campusrelay.data.local.dao.CachedDeliveryDao
import com.example.campusrelay.data.local.dao.OfflineTransactionDao
import com.example.campusrelay.data.local.dao.PendingDeliveryRequestDao
import com.example.campusrelay.data.local.entity.CachedDeliveryEntity
import com.example.campusrelay.data.local.entity.OfflineTransactionEntity
import com.example.campusrelay.data.local.entity.PendingDeliveryRequestEntity

/** REQ-OFF-1: RoomDB cache for active delivery jobs and queued offline handoffs. */
@Database(
    entities = [
        OfflineTransactionEntity::class,
        CachedDeliveryEntity::class,
        PendingDeliveryRequestEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun offlineTransactionDao(): OfflineTransactionDao
    abstract fun cachedDeliveryDao(): CachedDeliveryDao
    abstract fun pendingDeliveryRequestDao(): PendingDeliveryRequestDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    Constants.DATABASE_NAME
                ).addMigrations(object : androidx.room.migration.Migration(1, 2) {
                    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        db.execSQL("ALTER TABLE pending_delivery_requests ADD COLUMN lastError TEXT DEFAULT NULL")
                    }
                }).build().also { instance = it }
            }
    }
}
