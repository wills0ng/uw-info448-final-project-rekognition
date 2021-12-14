package edu.uw.minh2804.rekognition.services

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.extensions.toString64
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface SuspendCallable { suspend fun requestAnnotation(image: Bitmap): AnnotateImageResponse }

data class Property(
    val name: String,
    val value: String
)

data class EntityAnnotation(
    val description: String,
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

interface AnnotationCallback {
    fun onResultNotFound(context: Context): String
    fun onResultReceived(result: AnnotateImageResponse): String?
    suspend fun requestAnnotation(image: Bitmap): AnnotateImageResponse
}

object FirebaseFunctionsService {
    private const val TAG = "FirebaseFunctionsService"
    private val functions = Firebase.functions

    object TextAnnotationCallback : AnnotationCallback {
        override suspend fun requestAnnotation(image: Bitmap) = requestAnnotation(
            "annotateImage", TextRecognitionRequest.createRequest(image.toString64())
        )

        override fun onResultReceived(result: AnnotateImageResponse): String? {
            return result.fullTextAnnotation?.text
        }

        override fun onResultNotFound(context: Context): String {
            return context.getString(R.string.camera_output_text_not_found)
        }
    }

    object ObjectAnnotationCallback : AnnotationCallback {
        override suspend fun requestAnnotation(image: Bitmap) = requestAnnotation(
            "annotateImage", ObjectRecognitionRequest.createRequest(image.toString64())
        )

        override fun onResultReceived(result: AnnotateImageResponse): String? {
            return if (result.labelAnnotations.any()) result.labelAnnotations.joinToString { it.description } else null
        }

        override fun onResultNotFound(context: Context): String {
            return context.getString(R.string.camera_output_object_not_found)
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
                .addOnSuccessListener {
                    continuation.resume(JsonParser.parseString(Gson().toJson(it.data)))
                }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
        return Gson().fromJson(result.asJsonArray.first(), AnnotateImageResponse::class.java)
    }
}