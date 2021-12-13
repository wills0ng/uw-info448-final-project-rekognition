package edu.uw.minh2804.rekognition.services

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import edu.uw.minh2804.rekognition.extensions.toString64

data class Property(
    val name: String,
    val value: String
)

data class EntityAnnotation(
    val properties: List<Property>,
    val score: Double
)

data class TextAnnotation(
    val text: String
)

// See more: https://cloud.google.com/vision/docs/reference/rest/v1/AnnotateImageResponse#textannotation
data class AnnotateImageResponse(
    val fullTextAnnotation: TextAnnotation?,
    val labelAnnotations: List<EntityAnnotation>
)

interface FirebaseFunctionsCallback {
    fun onProcessed(annotation: AnnotateImageResponse)
    fun onError(exception: Exception)
}

object FirebaseFunctionsService {
    private val functions = Firebase.functions

    fun annotateImage(image: Bitmap, callback: FirebaseFunctionsCallback) {
        callFunction("annotateImage", TextRecognitionRequest.createRequest(image.toString64()), callback)
    }

    fun labelImage(image: Bitmap, callback: FirebaseFunctionsCallback) {
        callFunction("labelImage", ObjectRecognitionRequest.createRequest(image.toString64()), callback)
    }

    private fun callFunction(endpoint: String, body: JsonObject, callback: FirebaseFunctionsCallback) {
        if (!FirebaseAuthService.isSignedIn()) {
            callback.onError(FirebaseAuthService.UNAUTHORIZED_EXCEPTION)
            return
        }
        val response = callFunction(endpoint, body)
        response.addOnSuccessListener {
            val result = Gson().fromJson(it.asJsonArray.first(), AnnotateImageResponse::class.java) // Deserialize Json into BatchAnnotateImagesResponse object
            callback.onProcessed(result)
        }
        response.addOnFailureListener {
            callback.onError(it)
        }
    }

    private fun callFunction(endpoint: String, body: JsonObject): Task<JsonElement> {
        return functions
            .getHttpsCallable(endpoint)
            .call(body.toString())
            .continueWith { JsonParser.parseString(Gson().toJson(it.result?.data)) }
    }
}