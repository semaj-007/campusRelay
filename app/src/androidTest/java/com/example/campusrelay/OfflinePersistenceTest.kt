package com.example.campusrelay

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import com.example.campusrelay.data.local.AppDatabase
import com.example.campusrelay.data.local.entity.PendingDeliveryRequestEntity
import com.example.campusrelay.security.SessionVault
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflinePersistenceTest {
    @Test fun outboxSurvivesDatabaseReopen() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "outbox-test.db"
        context.deleteDatabase(name)
        val row = PendingDeliveryRequestEntity("stable-id", "user", "Library", "Lab", "Book", "Small", 5.0, true, "2026-01-01T00:00:00Z")
        var db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        try {
            db.pendingDeliveryRequestDao().enqueue(row)
            db.close()
            db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            assertEquals(listOf(row), db.pendingDeliveryRequestDao().getAll())
            db.pendingDeliveryRequestDao().markFailed(row.localId, "HTTP 400")
            assertEquals("HTTP 400", db.pendingDeliveryRequestDao().getAll().single().lastError)
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun sessionTokenIsEncryptedAndSurvivesVaultReopen() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Run only on a test installation: this clears that installation's session.
        val vault = SessionVault(context)
        vault.clear()
        try {
            vault.save("test-session-token")
            val stored = context.getSharedPreferences("session_vault", 0).getString("ciphertext", "")
            assertNotEquals("test-session-token", stored)
            vault.lock()
            assertEquals("test-session-token", SessionVault(context).token())
        } finally { vault.clear() }
    }
}
