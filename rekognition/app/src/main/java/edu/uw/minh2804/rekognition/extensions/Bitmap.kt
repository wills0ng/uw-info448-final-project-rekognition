package edu.uw.minh2804.rekognition.extensions

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

fun Bitmap.scaleDown(maxDimension: Int): Bitmap {
    val originalWidth = this.width
    val originalHeight = this.height
    var resizedWidth = maxDimension
    var resizedHeight = maxDimension
    when {
        originalHeight > originalWidth -> {
            resizedHeight = maxDimension
            resizedWidth = (resizedHeight * originalWidth.toFloat() / originalHeight.toFloat()).toInt()
        }
        originalWidth > originalHeight -> {
            resizedWidth = maxDimension
            resizedHeight = (resizedWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
        }
        originalHeight == originalWidth -> {
            resizedHeight = maxDimension
            resizedWidth = maxDimension
        }
    }
    return Bitmap.createScaledBitmap(this, resizedWidth, resizedHeight, false)
}

fun Bitmap.toString64(): String {
    val stream = ByteArrayOutputStream().also {
        this.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }
    return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
}