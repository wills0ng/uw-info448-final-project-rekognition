/** Shane Fretwell: I was responsible for the contents of this file, and Tom refactored it to use more concise syntax **/

package edu.uw.minh2804.rekognition.services

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

// This is a builder class to build the http request body for the text recognition Firebase's function.
object TextRecognitionRequest {
    private val feature = JsonObject().also { it.add("type", JsonPrimitive("TEXT_DETECTION")) }
    private val features = JsonArray().also { it.add(feature) }

    private val languageHints = JsonArray().also { it.add("en") }
    private val imageContext = JsonObject().also { it.add("languageHints", languageHints) }

    fun createRequest(base64encoded: String): JsonObject {
        return JsonObject().also { request ->
            val image = JsonObject().also { it.add("content", JsonPrimitive(base64encoded)) }
            request.add("image", image)
            request.add("features", features)
            request.add("imageContext", imageContext)
        }
    }
}