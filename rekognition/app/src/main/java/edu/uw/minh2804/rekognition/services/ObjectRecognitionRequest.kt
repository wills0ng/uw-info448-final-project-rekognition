package edu.uw.minh2804.rekognition.services

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

object ObjectRecognitionRequest {
    private val feature = JsonObject().also {
        it.add("type", JsonPrimitive("LABEL_DETECTION"))
        it.add("maxResults", JsonPrimitive(3))
    }
    private val features = JsonArray().also { it.add(feature) }

    fun createRequest(base64encoded: String): JsonObject {
        return JsonObject().also { request ->
            val image = JsonObject().also { it.add("content", JsonPrimitive(base64encoded)) }
            request.add("image", image)
            request.add("features", features)
        }
    }
}