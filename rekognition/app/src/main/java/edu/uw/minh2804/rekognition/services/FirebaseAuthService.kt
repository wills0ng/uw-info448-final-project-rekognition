package edu.uw.minh2804.rekognition.services

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.lang.Exception

val UNAUTHORIZED_EXCEPTION = Exception("User is not signed into Firebase")

interface OnSignedInCallback {
    fun onSignedIn()
    fun onError(exception: Exception)
}

object FirebaseAuthService {
    private const val TAG = "FirebaseAuthService"

    fun signIn() {
        Firebase.auth.signInAnonymously()
    }

    fun signIn(callback: OnSignedInCallback) {
        Firebase.auth.signInAnonymously()
            .addOnSuccessListener { callback.onSignedIn() }
            .addOnFailureListener { callback.onError(it) }
    }

    fun isSignedIn(): Boolean {
        return Firebase.auth.currentUser != null
    }
}