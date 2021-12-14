package edu.uw.minh2804.rekognition.services

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import edu.uw.minh2804.rekognition.extensions.toString64
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface SimpleCallable { suspend fun apply(image: Bitmap): AnnotateImageResponse }

data class Property(
    val name: String,
    val value: String
)

data class EntityAnnotation(
    val properties: List<Property>,
    val score: Double
)

data class TextAnnotation(
    val text: String
)

// See more: https://cloud.google.com/vision/docs/reference/rest/v1/AnnotateImageResponse#textannotation
data class AnnotateImageResponse(
    val fullTextAnnotation: TextAnnotation?,
    val labelAnnotations: List<EntityAnnotation>
)

object FirebaseFunctionsService {
    private val functions = Firebase.functions

    enum class Endpoint : SimpleCallable {
        TEXT {
            override suspend fun apply(image: Bitmap) = requestAnnotation(
                "annotateImage", TextRecognitionRequest.createRequest(image.toString64())
            )
        },
        OBJECT {
            override suspend fun apply(image: Bitmap) = requestAnnotation(
                "annotateImage", ObjectRecognitionRequest.createRequest(image.toString64())
            )
        }
    }

    private suspend fun requestAnnotation(endpoint: String, body: JsonObject): AnnotateImageResponse {
        if (!FirebaseAuthService.isSignedIn()) {
            FirebaseAuthService.signIn()
        }
        val result = suspendCoroutine<JsonElement> { continuation ->
            functions
                .getHttpsCallable(endpoint)
                .call(body.toString())
                .addOnSuccessListener { continuation.resume(JsonParser.parseString(Gson().toJson(it.data))) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
        return Gson().fromJson(result.asJsonArray.first(), AnnotateImageResponse::class.java)
    }
}