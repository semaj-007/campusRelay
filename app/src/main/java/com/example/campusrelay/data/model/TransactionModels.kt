package com.example.campusrelay.data.model

import java.util.Date

/** Mirrors the `transactions` table — the polymorphic record shared by delivery/marketplace/carpool. */
data class TransactionRecord(
    val id: String,
    val type: TransactionType,
    val status: TransactionStatus,
    val requesterId: String,
    val providerId: String,
    val serviceRefId: String,
    val agreedPrice: Double,
    val escrowStatus: EscrowStatus = EscrowStatus.NOT_FUNDED
)

/** Mirrors the `messages` table. */
data class ChatMessage(
    val id: String,
    val transactionId: String,
    val senderId: String,
    val content: String,
    val sentAt: Date
)

/** Mirrors the `reviews` table. */
data class Review(
    val id: String,
    val transactionId: String,
    val reviewerId: String,
    val revieweeId: String,
    val rating: Int,
    val comment: String? = null
)
