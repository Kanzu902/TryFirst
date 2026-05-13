
package com.tryfirst.api.models

import kotlinx.serialization.Serializable

// =============================================
// REQUEST MODELS (dikirim dari Android ke Server)
// =============================================

// Untuk endpoint /feedback/writing dan /feedback/speaking
@Serializable
data class FeedbackRequest(
    val userInput: String,          // teks yang ditulis/diucapkan user
    val question: String? = null    // soal (khusus writing, boleh null)
)

// =============================================
// RESPONSE MODELS (dikirim dari Server ke Android)
// =============================================

@Serializable
data class FeedbackResponse(
    val feedback: String,           // hasil feedback dari Gemini
    val type: String                // "writing" atau "speaking"
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

// =============================================
// HISTORY MODELS (sama persis dengan PracticeEntity di Android)
// =============================================

@Serializable
data class PracticeHistoryItem(
    val id: Int,
    val type: String,               // "writing" atau "speaking"
    val userInput: String,
    val feedback: String,
    val timestamp: Long
)

@Serializable
data class SaveHistoryRequest(
    val type: String,
    val userInput: String,
    val feedback: String
)

@Serializable
data class StatsResponse(
    val totalCount: Int,
    val writingCount: Int,
    val speakingCount: Int
)