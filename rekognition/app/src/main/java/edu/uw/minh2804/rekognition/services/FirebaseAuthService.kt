package edu.uw.minh2804.rekognition.services

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object FirebaseAuthService {
    private val AUTH = Firebase.auth

    suspend fun signIn(): AuthResult {
        return suspendCoroutine { continuation ->
            AUTH.signInAnonymously()
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    fun isSignedIn(): Boolean {
        return AUTH.currentUser != null
    }
}