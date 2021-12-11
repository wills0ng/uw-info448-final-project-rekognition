package edu.uw.minh2804.rekognition.services

import android.graphics.Bitmap

// See more: https://cloud.google.com/vision/docs/reference/rest/v1/AnnotateImageResponse#EntityAnnotation
data class ObjectAnnotation(
    val description: String,
    val score: Double
)

interface OnObjectProcessedCallback {
    fun onProcessed(objectAnnotation: ObjectAnnotation)
    fun onError(e: Exception)
}

object ObjectRecognitionService {
    fun processImage(image: Bitmap, onObjectProcessedCallback: OnObjectProcessedCallback) {
        // Implementation here
    }
}