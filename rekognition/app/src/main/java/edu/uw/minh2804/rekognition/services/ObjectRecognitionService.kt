package edu.uw.minh2804.rekognition.services

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import edu.uw.minh2804.rekognition.extensions.toString64

// See more: https://cloud.google.com/vision/docs/reference/rest/v1/AnnotateImageResponse#EntityAnnotation
data class ObjectAnnotation(
    val description: String,
    val score: Double
)

interface OnObjectProcessedCallback {
    fun onResultFound(annotation: ObjectAnnotation)
    fun onResultNotFound()
    fun onError(exception: Exception)
}

object ObjectRecognitionService {
    private const val TAG = "ObjectRecognitionService"

    fun processImage(image: Bitmap, callback: OnObjectProcessedCallback) {
        if (!FirebaseAuthService.isSignedIn()) {
            callback.onError(FirebaseAuthService.UNAUTHORIZED_EXCEPTION)
            return
        }
        val request = ObjectRecognitionRequest.createRequest(image.toString64())
        // responses and labelAnnotations will each be a single-element array in our case
        labelImage(request.toString()).addOnSuccessListener { responses ->
            val labelAnnotations = responses.asJsonArray[0].asJsonObject["labelAnnotations"]
            if (labelAnnotations != null) {
                val label = labelAnnotations.asJsonArray[0].asJsonObject
                callback.onResultFound(ObjectAnnotation(
                    label["description"].asString,
                    label["score"].asDouble))
            } else {
                callback.onResultNotFound()
            }
        } .addOnFailureListener {
            callback.onError(it)
        }
    }

    private fun labelImage(requestJson: String): Task<JsonElement> {
        return Firebase
            .functions
            .getHttpsCallable("annotateImage")
            .call(requestJson)
            .continueWith { JsonParser.parseString(Gson().toJson(it.result?.data)) }
    }
}