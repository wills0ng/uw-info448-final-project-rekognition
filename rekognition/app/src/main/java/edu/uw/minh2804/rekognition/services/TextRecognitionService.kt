package edu.uw.minh2804.rekognition.services

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.*
import edu.uw.minh2804.rekognition.extensions.toString64

// See more: https://cloud.google.com/vision/docs/reference/rest/v1/AnnotateImageResponse#textannotation
data class TextAnnotation(
    val text: String
)

private const val FUNCTION_NAME = "annotateImage"
private const val RESPONSE_KEY = "fullTextAnnotation"
private val FUNCTIONS = Firebase.functions

interface OnTextProcessedCallback {
    fun onProcessed(annotation: TextAnnotation)
    fun onError(exception: Exception)
}

// This object is a modification of the code from the firebase docs: https://firebase.google.com/docs/ml/android/recognize-text?authuser=0#1.-prepare-the-input-image
object TextRecognitionService {
    val RESULT_NOT_FOUND_EXCEPTION = Exception("Result not found")
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
                response.result!!.asJsonArray[0].asJsonObject[RESPONSE_KEY]?.let { rawAnnotation ->
                    val annotation = rawAnnotation.asJsonObject
                    Log.v(TAG, "Annotation: $annotation")
                     callback.onProcessed(TextAnnotation(annotation["text"].asString))
                } ?: callback.onError(RESULT_NOT_FOUND_EXCEPTION)
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