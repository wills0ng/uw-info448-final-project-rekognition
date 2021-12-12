package edu.uw.minh2804.rekognition.services

import android.graphics.Bitmap
import edu.uw.minh2804.rekognition.extensions.scaleDown
import edu.uw.minh2804.rekognition.extensions.toString64

// See more: https://cloud.google.com/vision/docs/reference/rest/v1/AnnotateImageResponse#EntityAnnotation
data class ObjectAnnotation(
    val description: String,
    val score: Double
)

interface OnObjectProcessedCallback : RecognitionServiceCallback<ObjectAnnotation>

object ObjectRecognitionService {
    private const val TAG = "ObjectRecognitionService"

    fun processImage(image: Bitmap, callback: OnObjectProcessedCallback) {
        if (!FirebaseAuthService.isSignedIn()) {
            callback.onError(FirebaseAuthService.UNAUTHORIZED_EXCEPTION)
            return
        }
        val request = ObjectRecognitionRequest.createRequest(image.scaleDown(ImageRecognitionSetting.MAX_DIMENSION).toString64())
        val response = FirebaseFunctionsService.callFunction("labelImage", request)

        // responses and labelAnnotations will each be a single-element array in our case
        response.addOnSuccessListener { responses ->
            val labelAnnotations = responses.asJsonArray[0].asJsonObject["labelAnnotations"]
            if (labelAnnotations != null) {
                val label = labelAnnotations.asJsonArray[0].asJsonObject
                callback.onResultFound(ObjectAnnotation(
                    label["description"].asString,
                    label["score"].asDouble))
            } else {
                callback.onResultNotFound()
            }
        }
        response.addOnFailureListener {
            callback.onError(it)
        }
    }
}