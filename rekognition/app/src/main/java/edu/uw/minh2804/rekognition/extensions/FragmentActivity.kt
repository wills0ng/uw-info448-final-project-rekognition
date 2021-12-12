package edu.uw.minh2804.rekognition.extensions

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

interface OnPermissionGrantedCallback {
    fun onPermissionGranted() {}
    fun onPermissionDenied() {}
}

fun FragmentActivity.requestPermission(permission: String, callback: OnPermissionGrantedCallback) {
    this.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            callback.onPermissionGranted()
        } else {
            callback.onPermissionDenied()
        }
    } .run { launch(permission) }
}

fun FragmentActivity.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}