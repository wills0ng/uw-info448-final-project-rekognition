package edu.uw.minh2804.rekognition.services

import android.graphics.Bitmap
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

// See more: https://cloud.google.com/vision/docs/reference/rest/v1/AnnotateImageResponse#EntityAnnotation
data class ObjectAnnotation(
    val description: String,
    val score: Double
)

private const val FUNCTION_NAME = "labelimage" //TODO: make sure this is the right name
private const val RESPONSE_KEY = "labelAnnotations"
private val FUNCTIONS = Firebase.functions

interface OnObjectProcessedCallback {
    fun onProcessed(annotation: ObjectAnnotation)
    fun onError(exception: Exception)
}

object ObjectRecognitionService {
    private const val TAG = "ObjectRecognitionService"

    fun processImage(image: Bitmap, callback: OnObjectProcessedCallback) {
        // Implementation here
    }

    private fun createRequest(base64encoded: String): JsonObject {
        val request = JsonObject().apply {
            val image = JsonObject().apply { add("content", JsonPrimitive(base64encoded)) }
            add("image", image)

            val feature = JsonObject().apply {
                add("type", JsonPrimitive("LABEL_DETECTION"))
                add("maxResults", JsonPrimitive(1))
            }
            val features = JsonArray().apply { add(feature) }
            add("features", features)

            val imageContext = JsonObject().apply {
                val languageHints = JsonArray().apply { add("en") }
                add("languageHints", languageHints)
            }

            add("imageContext", imageContext)
        }
        return request
    }
}