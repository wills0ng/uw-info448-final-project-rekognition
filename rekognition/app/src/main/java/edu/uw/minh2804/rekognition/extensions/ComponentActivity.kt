package edu.uw.minh2804.rekognition.extensions

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

interface OnPermissionGrantedCallback {
    fun onPermissionGranted() {}
    fun onPermissionDenied() {}
}

fun ComponentActivity.requestPermission(permission: String, callback: OnPermissionGrantedCallback) {
    this.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            callback.onPermissionGranted()
        } else {
            callback.onPermissionDenied()
        }
    } .run { launch(permission) }
}

fun ComponentActivity.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}