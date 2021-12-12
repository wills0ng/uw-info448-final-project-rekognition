package edu.uw.minh2804.rekognition.services

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

object TextRecognitionRequest {
    private val feature = JsonObject().apply { add("type", JsonPrimitive("TEXT_DETECTION")) }
    private val features = JsonArray().apply { add(feature) }

    private val languageHints = JsonArray().apply { add("en") }
    private val imageContext = JsonObject().apply { add("languageHints", languageHints) }

    fun createRequest(base64encoded: String): JsonObject {
        val request = JsonObject().apply {
            val image = JsonObject().apply { add("content", JsonPrimitive(base64encoded)) }
            add("image", image)
            add("features", features)
            add("imageContext", imageContext)
        }
        return request
    }
}