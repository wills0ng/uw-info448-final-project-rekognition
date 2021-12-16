/** Shane Fretwell: I was responsible for the contents of this file, except for the data classes which Tom implemented **/

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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.parcelize.Parcelize

// This interface is used to map callbacks with a user's selected option in the tab bar.
interface Annotator {
    // Gets a unique identifier of the type of annotation this Annotator provides
    fun getType(context: Context): String
    // Formats the annotation response into a string, if the response is valid
    fun onAnnotated(result: AnnotateImageResponse): String?
    // Annotates an image with a description
    suspend fun annotate(image: Bitmap): AnnotateImageResponse
}

// This service is responsible for invoking web requests to Firebase.
object FirebaseFunctionsService {
    private val functions = Firebase.functions

    // This enum class encapsulates the differences between the text and object recognition
    // endpoints, allowing for code referencing these endpoints to be agnostic of these differences
    enum class Annotator : edu.uw.minh2804.rekognition.services.Annotator {
        TEXT {
            // The string resource labelling each tab in the camera activity is a unique identifier
            // of the endpoint, and is widely accessible across the code base.
            override fun getType(context: Context): String {
                return context.getString(R.string.tab_item_text_annotation)
            }

            override fun onAnnotated(result: AnnotateImageResponse): String? {
                return result.fullTextAnnotation?.text
            }

            override suspend fun annotate(image: Bitmap) = requestAnnotation(
                "annotateImage", TextRecognitionRequest.createRequest(image.toString64())
            )
        },
        OBJECT {
            override fun getType(context: Context): String {
                return context.getString(R.string.tab_item_object_annotation)
            }

            override fun onAnnotated(result: AnnotateImageResponse): String? {
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
        if (!FirebaseAuthService.isAuthenticated()) {
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

// These data classes are used to serialize the Json response from Firebase functions.
// See more: https://cloud.google.com/vision/docs/reference/rest/v1/AnnotateImageResponse#textannotation

@Parcelize
data class EntityAnnotation(
    val description: String,
    val score: Double
) : Parcelable

@Parcelize
data class TextAnnotation(
    val text: String
) : Parcelable

@Parcelize
data class AnnotateImageResponse(
    val fullTextAnnotation: TextAnnotation?,
    val labelAnnotations: List<EntityAnnotation>
) : Parcelable