package edu.uw.minh2804.rekognition.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.extensions.isPermissionGranted
import edu.uw.minh2804.rekognition.extensions.requestPermission
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.launch

// This class is responsible for what the user see on screen.
// It takes the input from the back camera and display it onto a surface view.
// See more: https://developer.android.com/codelabs/camerax-getting-started#0
abstract class CameraSurfaceProviderFragment : Fragment(R.layout.fragment_camera_surface_provider) {
    private lateinit var cameraExecutor: ExecutorService
    protected var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireCameraOrShutdown()
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
        val view = requireView().findViewById<PreviewView>(R.id.preview_camera_finder)
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
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

    private fun requireCameraOrShutdown() {
        lifecycleScope.launch {
            val requiredPermission = Manifest.permission.CAMERA
            if (requireActivity().isPermissionGranted(requiredPermission)) {
                startCamera()
            } else {
                if (requireActivity().requestPermission(requiredPermission)) {
                    startCamera()
                } else {
                    requireActivity().finish()
                }
            }
        }
    }

    companion object {
        private const val TAG = "CameraSurfaceProviderFragment"
    }
}