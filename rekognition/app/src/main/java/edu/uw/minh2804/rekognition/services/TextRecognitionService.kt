package edu.uw.minh2804.rekognition.services

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.gson.*

// @Tom - If we don't use more than one property from the TextAnnotation response object, then why don't we just return the string itself?
// If you think we should use this TextAnnotation class, then uncomment the commented code in this file.
// I also haven't been able to test this successfully yet, the firebase function log is giving me a 401 status code
// TODO: remove commented code
// See more: https://cloud.google.com/vision/docs/reference/rest/v1/AnnotateImageResponse#textannotation
//data class TextAnnotation(
//    val text: String
//)

interface OnTextProcessedCallback {
    // TODO: remove commented code
//    fun onProcessed(textAnnotation: TextAnnotation)
    fun onProcessed(string: String)
    fun onError(e: Exception)
}

// This object is a modification of the code from the firebase docs: https://firebase.google.com/docs/ml/android/recognize-text?authuser=0#1.-prepare-the-input-image
object TextRecognitionService : GoogleVisionService() {
    private const val TAG = "TextRecognitionService"

    fun processImage(image: Bitmap, callback: OnTextProcessedCallback) {
        // Get base64 string representation of the image
        val base64encoded = bitmapToString(image)

        // Create request from image string
        val request = createRequest(base64encoded)

        annotateImage(request.toString())
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e(TAG, "Task unsuccessful")
                    callback.onError(task.exception!!)
                } else {
                    Log.v(TAG, "Successful response $task")
                    val annotation = task.result!!.asJsonArray[0].asJsonObject["fullTextAnnotation"].asJsonObject
                    Log.v(TAG, "Annotation: $annotation")
                    // TODO: remove commented code
//                    callback.onProcessed(TextAnnotation(annotation["text"].asString))
                    callback.onProcessed(annotation["text"].asString)
                }
            }
    }

    private fun annotateImage(requestJson: String): Task<JsonElement> {
        return functions
            .getHttpsCallable("annotateImage")
            .call(requestJson)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data
                JsonParser.parseString(Gson().toJson(result))
            }
    }

    private fun createRequest(base64encoded: String): JsonObject {
        // Create json request to cloud vision
        val request = JsonObject()
        // Add image to request
        val reqImage = JsonObject()
        reqImage.add("content", JsonPrimitive(base64encoded))
        request.add("image", reqImage)
        //Add features to the request
        val feature = JsonObject()
        feature.add("type", JsonPrimitive("TEXT_DETECTION"))
        val features = JsonArray()
        features.add(feature)
        request.add("features", features)
        // Add language hints TODO: remove language hints if user's language isn't english?
        val imageContext = JsonObject()
        val languageHints = JsonArray()
        languageHints.add("en")
        imageContext.add("languageHints", languageHints)
        request.add("imageContext", imageContext)
        return request
    }
}