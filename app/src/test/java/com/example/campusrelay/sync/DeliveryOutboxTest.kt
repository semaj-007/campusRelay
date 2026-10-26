package com.example.campusrelay.sync

import com.example.campusrelay.data.local.entity.PendingDeliveryRequestEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

class DeliveryOutboxTest {
    private fun row(id: String) = PendingDeliveryRequestEntity(id, "user", "Library", "Lab", "Book", "Small", 5.0, true, "2026-01-01T00:00:00Z")

    @Test fun networkFailureRetainsUnacknowledgedStableIdsForRetry() = runBlocking {
        val rows = listOf(row("first"), row("second"))
        val acknowledged = mutableListOf<String>()
        try {
            flushDeliveryOutbox(rows, { if (it.localId == "second") throw IOException("offline") }, { acknowledged += it.localId }, { _, _ -> fail() })
            fail("Must propagate a retryable failure")
        } catch (_: IOException) { }
        assertEquals(listOf("first"), acknowledged)
        val retried = rows.filterNot { it.localId in acknowledged }
        val sent = mutableListOf<String>()
        assertEquals(1, flushDeliveryOutbox(retried, { sent += it.localId }, {}, { _, _ -> fail() }))
        assertEquals(listOf("second"), sent)
    }

    @Test fun validationFailureIsRetainedAsRejectedAndDoesNotBlockOtherRows() = runBlocking {
        val rejected = mutableListOf<String>()
        val acknowledged = mutableListOf<String>()
        val count = flushDeliveryOutbox(listOf(row("bad"), row("good")), {
            if (it.localId == "bad") throw HttpException(Response.error<Unit>(400, "invalid".toResponseBody()))
        }, { acknowledged += it.localId }, { row, error -> rejected += row.localId; assertEquals("HTTP 400", error) })
        assertEquals(1, count)
        assertEquals(listOf("bad"), rejected)
        assertEquals(listOf("good"), acknowledged)
    }

    @Test fun authenticationFailureDoesNotDeleteOrRejectWork() = runBlocking {
        try {
            flushDeliveryOutbox(listOf(row("one")), { throw HttpException(Response.error<Unit>(401, "expired".toResponseBody())) }, { fail() }, { _, _ -> fail() })
            fail()
        } catch (e: HttpException) { assertEquals(401, e.code()) }
    }

    @Test fun partialHandoffAcknowledgementExcludesEveryFailedId() {
        assertEquals(listOf("a", "c"), acknowledgedIds(listOf("a", "b", "c"), listOf("b")))
        assertTrue(acknowledgedIds(listOf("b"), listOf("b")).isEmpty())
    }
}
