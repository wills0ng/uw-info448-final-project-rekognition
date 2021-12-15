package edu.uw.minh2804.rekognition.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.extensions.isPermissionGranted
import edu.uw.minh2804.rekognition.extensions.requestPermission
import java.io.OutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.launch

// This class is used to throw when the user didn't grant permission to camera.
class CameraNotDetectedException : Exception("Camera not detected")

// This class handles the core camera functionality.
// It will display the back camera output onto a view and provides a method to capture photos.
// See more: https://developer.android.com/codelabs/camerax-getting-started#0
class CameraFragment : Fragment(R.layout.fragment_camera) {
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCameraOrIgnore()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        val executor = ContextCompat.getMainExecutor(requireContext())
        cameraProviderFuture.addListener({ displayCameraPreview(cameraProviderFuture.get()) }, executor)
    }

    private fun displayCameraPreview(provider: ProcessCameraProvider) {
        val surface = (requireView() as PreviewView).surfaceProvider
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(surface) }
        imageCapture = ImageCapture.Builder().build()
        try {
            // Unbind use cases before rebinding
            provider.unbindAll()

            // Bind use cases to camera
            provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
        } catch(e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    suspend fun takePhoto(output: OutputStream) {
        imageCapture ?: throw CameraNotDetectedException()

        val outputOptions = ImageCapture.OutputFileOptions.Builder(output).build()
        val executor = ContextCompat.getMainExecutor(requireContext())

        return suspendCoroutine {
            imageCapture!!.takePicture(
                outputOptions,
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) { it.resume(Unit) }
                    override fun onError(e: ImageCaptureException) { it.resumeWithException(e) }
                }
            )
        }
    }

    private fun requestCameraOrIgnore() {
        lifecycleScope.launch {
            val requiredPermission = Manifest.permission.CAMERA
            if (requireActivity().isPermissionGranted(requiredPermission) || requireActivity().requestPermission(requiredPermission)) {
                startCamera()
            }
        }
    }

    companion object {
        private const val TAG = "CameraFragment"
    }
}