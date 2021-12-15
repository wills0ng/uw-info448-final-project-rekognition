package edu.uw.minh2804.rekognition.fragments

import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import java.io.OutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// This class handles the core input/capture photo functionality.
// See more: https://developer.android.com/codelabs/camerax-getting-started#0
abstract class CameraIOProviderFragment : CameraSurfaceProviderFragment() {
    protected suspend fun takePhoto(output: OutputStream): Boolean {
        imageCapture ?: return false

        val outputOptions = ImageCapture.OutputFileOptions.Builder(output).build()
        val executor = ContextCompat.getMainExecutor(requireContext())

        return suspendCoroutine {
            imageCapture!!.takePicture(
                outputOptions,
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) { it.resume(true) }
                    override fun onError(e: ImageCaptureException) { it.resume(false) }
                }
            )
        }
    }
}