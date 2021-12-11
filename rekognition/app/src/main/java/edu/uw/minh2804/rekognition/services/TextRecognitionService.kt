package edu.uw.minh2804.rekognition.services

import android.graphics.Bitmap

// See more: https://cloud.google.com/vision/docs/reference/rest/v1/AnnotateImageResponse#textannotation
data class TextAnnotation(
    val text: String
)

interface OnTextProcessedCallback {
    fun onProcessed(textAnnotation: TextAnnotation)
    fun onError(e: Exception)
}

object TextRecognitionService {
    fun processImage(image: Bitmap, onTextProcessedCallback: OnTextProcessedCallback) {
        // Implementation here
    }
}