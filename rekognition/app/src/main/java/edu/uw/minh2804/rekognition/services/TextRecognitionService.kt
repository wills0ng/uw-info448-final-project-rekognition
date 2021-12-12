package edu.uw.minh2804.rekognition.services

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.*
import edu.uw.minh2804.rekognition.extensions.toString64

// @Tom - If we don't use more than one property from the TextAnnotation response object, then why don't we just return the string itself?
// If you think we should use this TextAnnotation class, then uncomment the commented code in this file.
// I also haven't been able to test this successfully yet, the firebase function log is giving me a 401 status code
// TODO: remove commented code
// See more: https://cloud.google.com/vision/docs/reference/rest/v1/AnnotateImageResponse#textannotation
//data class TextAnnotation(
//    val text: String
//)

private val FUNCTIONS = Firebase.functions
private val FUNCTION_NAME = "annotateImage"

interface OnTextProcessedCallback {
    // TODO: remove commented code
    // fun onProcessed(textAnnotation: TextAnnotation)
    fun onProcessed(string: String)
    fun onError(e: Exception)
}

// This object is a modification of the code from the firebase docs: https://firebase.google.com/docs/ml/android/recognize-text?authuser=0#1.-prepare-the-input-image
object TextRecognitionService {
    private const val TAG = "TextRecognitionService"

    fun processImage(image: Bitmap, callback: OnTextProcessedCallback) {
        if (!FirebaseAuthService.isSignedIn()) {
            callback.onError(Exception(UNAUTHORIZED_EXCEPTION))
            return
        }
        val request = createRequest(image.toString64())
        annotateImage(request.toString()).addOnCompleteListener { response ->
            if (response.isSuccessful) {
                Log.v(TAG, "Successful response $response")
                response.result!!.asJsonArray[0].asJsonObject["fullTextAnnotation"]?.let { rawAnnotation ->
                    val annotation = rawAnnotation.asJsonObject
                    Log.v(TAG, "Annotation: $annotation")
                    // TODO: remove commented code
                    // callback.onProcessed(TextAnnotation(annotation["text"].asString))
                    callback.onProcessed(annotation["text"].asString)
                } ?: callback.onProcessed("")
            } else {
                Log.e(TAG, "Task unsuccessful")
                callback.onError(response.exception!!)
            }
        }
    }

    private fun annotateImage(requestJson: String): Task<JsonElement> {
        return FUNCTIONS
            .getHttpsCallable(FUNCTION_NAME)
            .call(requestJson)
            .continueWith { response ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = response.result?.data
                JsonParser.parseString(Gson().toJson(result))
            }
    }

    private fun createRequest(base64encoded: String): JsonObject {
        val request = JsonObject().apply {
            val image = JsonObject().apply { add("content", JsonPrimitive(base64encoded)) }
            add("image", image)

            val feature = JsonObject().apply { add("type", JsonPrimitive("TEXT_DETECTION")) }
            val features = JsonArray().apply { add(feature) }
            add("features", features)

            val imageContext = JsonObject().apply {
                val languageHints = JsonArray().apply { add("en") }
                add("languageHints", languageHints)
            }

            add("imageContext", imageContext)
        }
        return request
    }
}