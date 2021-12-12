package edu.uw.minh2804.rekognition.services

import android.graphics.Bitmap

// See more: https://cloud.google.com/vision/docs/reference/rest/v1/AnnotateImageResponse#EntityAnnotation
data class ObjectAnnotation(
    val description: String,
    val score: Double
)

interface OnObjectProcessedCallback {
    fun onProcessed(annotation: ObjectAnnotation)
    fun onError(exception: Exception)
}

object ObjectRecognitionService {
    private const val TAG = "ObjectRecognitionService"

    fun processImage(image: Bitmap, callback: OnObjectProcessedCallback) {
        // Implementation here
    }
}