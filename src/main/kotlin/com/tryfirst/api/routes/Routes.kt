package com.tryfirst.api.routes

import com.tryfirst.api.models.*
import com.tryfirst.api.services.GeminiService
import com.tryfirst.api.services.PracticeRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val apiKey = System.getenv("GEMINI_API_KEY") ?: "AIzaSyA5abN-HUIoGqk8r7YD_Q_Dzu7zs5j8IjA"
    val geminiService = GeminiService(apiKey = apiKey)
    val repository = PracticeRepository()

    routing {
        get("/") {
            call.respond(mapOf("status" to "TryFirst API is running! 🚀"))
        }
        route("/feedback") {
            post("/writing") {
                val request = call.receive<FeedbackRequest>()
                if (request.userInput.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse<Nothing>(success = false, error = "userInput tidak boleh kosong"))
                    return@post
                }
                val result = if (request.question != null) {
                    geminiService.getWritingFeedbackWithQuestion(request.userInput, request.question)
                } else {
                    geminiService.getWritingFeedback(request.userInput)
                }
                result.fold(
                    onSuccess = { feedback ->
                        repository.save("writing", request.userInput, feedback)
                        call.respond(ApiResponse(success = true, data = FeedbackResponse(feedback = feedback, type = "writing")))
                    },
                    onFailure = { error ->
                        call.respond(HttpStatusCode.InternalServerError, ApiResponse<Nothing>(success = false, error = error.message))
                    }
                )
            }
            post("/speaking") {
                val request = call.receive<FeedbackRequest>()
                if (request.userInput.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse<Nothing>(success = false, error = "userInput tidak boleh kosong"))
                    return@post
                }
                val result = geminiService.getSpeakingFeedback(request.userInput)
                result.fold(
                    onSuccess = { feedback ->
                        repository.save("speaking", request.userInput, feedback)
                        call.respond(ApiResponse(success = true, data = FeedbackResponse(feedback = feedback, type = "speaking")))
                    },
                    onFailure = { error ->
                        call.respond(HttpStatusCode.InternalServerError, ApiResponse<Nothing>(success = false, error = error.message))
                    }
                )
            }
        }
        route("/history") {
            get {
                call.respond(ApiResponse(success = true, data = repository.getAll()))
            }
            get("/stats") {
                call.respond(ApiResponse(success = true, data = repository.getStats()))
            }
            post {
                val request = call.receive<SaveHistoryRequest>()
                val saved = repository.save(request.type, request.userInput, request.feedback)
                call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = saved))
            }
            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiResponse<Nothing>(success = false, error = "ID tidak valid"))
                val deleted = repository.delete(id)
                if (deleted) call.respond(ApiResponse<Nothing>(success = true))
                else call.respond(HttpStatusCode.NotFound, ApiResponse<Nothing>(success = false, error = "Data tidak ditemukan"))
            }
            delete {
                repository.deleteAll()
                call.respond(ApiResponse<Nothing>(success = true))
            }
        }
    }
}