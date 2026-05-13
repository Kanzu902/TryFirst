package com.tryfirst.api.services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class GeminiRequest(val contents: List<Content>) {
    @Serializable data class Content(val parts: List<Part>)
    @Serializable data class Part(val text: String)
}

@Serializable
private data class GeminiResponse(val candidates: List<Candidate>? = null) {
    @Serializable data class Candidate(val content: Content? = null)
    @Serializable data class Content(val parts: List<Part>? = null)
    @Serializable data class Part(val text: String? = null)
}

class GeminiService(private val apiKey: String) {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val baseUrl =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    private suspend fun generate(prompt: String): Result<String> {
        return try {
            val response: GeminiResponse = client.post(baseUrl) {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(
                    GeminiRequest(
                        contents = listOf(
                            GeminiRequest.Content(parts = listOf(GeminiRequest.Part(prompt)))
                        )
                    )
                )
            }.body()

            val text = response.candidates
                ?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Tidak ada feedback tersedia."

            Result.success(text)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mendapat feedback: ${e.message}"))
        }
    }

    // ✏️ Feedback Writing umum
    suspend fun getWritingFeedback(userText: String): Result<String> = generate("""
        Kamu adalah guru bahasa Inggris yang ramah untuk pelajar Indonesia.
        Pelajar menulis kalimat ini: "$userText"
        Berikan feedback dalam format berikut (gunakan Bahasa Indonesia):
        1. ✅ Yang sudah benar:
        2. ❌ Yang perlu diperbaiki:
        3. 💡 Kalimat yang benar:
        4. 📚 Tips singkat:
        Buat feedback singkat, jelas, dan menyemangati pelajar.
    """.trimIndent())

    // 🎤 Feedback Speaking
    suspend fun getSpeakingFeedback(spokenText: String): Result<String> = generate("""
        Kamu adalah guru speaking bahasa Inggris untuk pelajar Indonesia.
        Pelajar mengucapkan kalimat ini (hasil speech-to-text): "$spokenText"
        Berikan feedback dalam format berikut (gunakan Bahasa Indonesia):
        1. ✅ Kalimat yang terdeteksi:
        2. ❌ Kesalahan grammar:
        3. 💡 Versi yang benar:
        4. 🎯 Cara pengucapan:
        Buat feedback singkat dan menyemangati.
    """.trimIndent())

    // 📝 Feedback Writing dengan Soal
    suspend fun getWritingFeedbackWithQuestion(userText: String, question: String): Result<String> = generate("""
        Kamu adalah guru bahasa Inggris yang ramah untuk pelajar Indonesia.
        Soal: "$question"
        Jawaban pelajar: "$userText"
        Berikan feedback dalam format berikut (gunakan Bahasa Indonesia):
        1. ✅ Yang sudah benar:
        2. ❌ Yang perlu diperbaiki:
        3. 💡 Kalimat yang benar:
        4. 📚 Tips singkat:
        5. 🎯 Skor: (X/100)
        Buat feedback singkat, jelas, dan menyemangati pelajar.
    """.trimIndent())
}
