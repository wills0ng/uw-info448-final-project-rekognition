package edu.uw.minh2804.rekognition.extensions

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// Request the permission with the name `permission`
suspend fun ComponentActivity.requestPermission(permission: String): Boolean {
    return suspendCoroutine { continuation ->
        this.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            continuation.resume(it)
        } .run { launch(permission) }
    }
}

// Returns whether the permission with the name `permission` has been granted in this activity
fun ComponentActivity.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}