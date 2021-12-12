package edu.uw.minh2804.rekognition.services

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.lang.Exception

interface OnSignedInCallback {
    fun onSignedIn() {}
    fun onError(exception: Exception) {}
}

object FirebaseAuthService {
    private val AUTH = Firebase.auth
    val UNAUTHORIZED_EXCEPTION = Exception("User is not signed into Firebase")

    fun signIn() {
        AUTH.signInAnonymously()
    }

    fun signIn(callback: OnSignedInCallback) {
        AUTH.signInAnonymously()
            .addOnSuccessListener { callback.onSignedIn() }
            .addOnFailureListener { callback.onError(it) }
    }

    fun isSignedIn(): Boolean {
        return AUTH.currentUser != null
    }
}