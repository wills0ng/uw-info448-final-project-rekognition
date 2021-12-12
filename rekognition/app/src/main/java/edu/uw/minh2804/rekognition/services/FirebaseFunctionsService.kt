package edu.uw.minh2804.rekognition.services

import com.google.android.gms.tasks.Task
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

object FirebaseFunctionsService {
    private val functions = Firebase.functions

    fun callFunction(endpoint: String, body: JsonObject): Task<JsonElement> {
        return functions
            .getHttpsCallable(endpoint)
            .call(body.toString())
            .continueWith { JsonParser.parseString(Gson().toJson(it.result?.data)) }
    }
}