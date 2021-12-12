package edu.uw.minh2804.rekognition.services

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.*
import edu.uw.minh2804.rekognition.extensions.toString64

// See more: https://cloud.google.com/vision/docs/reference/rest/v1/AnnotateImageResponse#textannotation
data class TextAnnotation(
    val text: String
)

interface OnTextProcessedCallback {
    fun onResultFound(annotation: TextAnnotation)
    fun onResultNotFound()
    fun onError(exception: Exception)
}

// This object is a modification of the code from the firebase docs: https://firebase.google.com/docs/ml/android/recognize-text?authuser=0#1.-prepare-the-input-image
object TextRecognitionService {
    fun processImage(image: Bitmap, callback: OnTextProcessedCallback) {
        if (!FirebaseAuthService.isSignedIn()) {
            callback.onError(FirebaseAuthService.UNAUTHORIZED_EXCEPTION)
            return
        }
        val request = TextRecognitionRequest.createRequest(image.toString64())
        annotateImage(request.toString()).addOnSuccessListener {
            val annotationElement = it.asJsonArray[0].asJsonObject["fullTextAnnotation"]
            if (annotationElement != null) {
                val text = annotationElement.asJsonObject["text"].asString
                callback.onResultFound(TextAnnotation(text))
            } else {
                callback.onResultNotFound()
            }
        } .addOnFailureListener {
            callback.onError(it)
        }
    }

    private fun annotateImage(requestJson: String): Task<JsonElement> {
        return Firebase
            .functions
            .getHttpsCallable("annotateImage")
            .call(requestJson)
            .continueWith { JsonParser.parseString(Gson().toJson(it.result?.data)) }
    }
}