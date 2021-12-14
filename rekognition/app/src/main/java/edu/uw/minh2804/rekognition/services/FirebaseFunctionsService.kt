package edu.uw.minh2804.rekognition.services

import android.content.Context
import android.graphics.Bitmap
import android.os.Parcelable
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.extensions.toString64
import kotlinx.parcelize.Parcelize
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface Annotator {
    fun getResultType(context: Context): String
    fun formatResult(result: AnnotateImageResponse): String?
    suspend fun annotate(image: Bitmap): AnnotateImageResponse
}

data class Property(
    val name: String,
    val value: String
)

@Parcelize
data class EntityAnnotation(
    val description: String,
    val score: Double
) : Parcelable

@Parcelize
data class TextAnnotation(
    val text: String
) : Parcelable

// See more: https://cloud.google.com/vision/docs/reference/rest/v1/AnnotateImageResponse#textannotation
@Parcelize
data class AnnotateImageResponse(
    val fullTextAnnotation: TextAnnotation?,
    val labelAnnotations: List<EntityAnnotation>
) : Parcelable

object FirebaseFunctionsService {
    private const val TAG = "FirebaseFunctionsService"
    private val functions = Firebase.functions

    enum class Endpoint : Annotator {
        TEXT {
            override fun getResultType(context: Context): String {
                return context.getString(R.string.camera_text_recognition)
            }
            override fun formatResult(result: AnnotateImageResponse): String? {
                return result.fullTextAnnotation?.text
            }
            override suspend fun annotate(image: Bitmap) = requestAnnotation(
                "annotateImage", TextRecognitionRequest.createRequest(image.toString64())
            )
        },
        OBJECT {
            override fun getResultType(context: Context): String {
                return context.getString(R.string.camera_image_labeling)
            }
            override fun formatResult(result: AnnotateImageResponse): String? {
                return if (result.labelAnnotations.any()) {
                    result.labelAnnotations.joinToString { it.description }
                } else null
            }
            override suspend fun annotate(image: Bitmap) = requestAnnotation(
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
                .addOnSuccessListener {
                    continuation.resume(JsonParser.parseString(Gson().toJson(it.data)))
                }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
        return Gson().fromJson(result.asJsonArray.first(), AnnotateImageResponse::class.java)
    }
}