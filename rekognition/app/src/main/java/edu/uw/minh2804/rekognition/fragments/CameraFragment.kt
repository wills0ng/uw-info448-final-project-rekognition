/** Tom Nguyen: I wrote this class and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.extensions.OnPermissionGrantedCallback
import edu.uw.minh2804.rekognition.extensions.isPermissionGranted
import edu.uw.minh2804.rekognition.extensions.requestPermission
import edu.uw.minh2804.rekognition.services.*
import edu.uw.minh2804.rekognition.stores.*
import edu.uw.minh2804.rekognition.viewmodels.CameraViewModel
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraOutput(file: File) {
    val photo = Photo(file)
    val thumbnail = Thumbnail(photo.file)
}

// CameraFragment class is taken and modified from https://developer.android.com/codelabs/camerax-getting-started#0
class CameraFragment : Fragment(R.layout.fragment_camera) {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var photoStore: PhotoStore

    private val model: CameraViewModel by activityViewModels()
    private var imageCapture: ImageCapture? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireCameraOrShutdown()
        view.findViewById<ImageButton>(R.id.button_camera_capture).setOnClickListener { takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()
        photoStore = PhotoStore(requireActivity())
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())
        cameraProviderFuture.addListener(Runnable
        { displayCameraFeed(cameraProviderFuture.get()) },
            ContextCompat.getMainExecutor(requireActivity())
        )
    }

    private fun displayCameraFeed(provider: ProcessCameraProvider) {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(requireView().findViewById<PreviewView>(R.id.preview_camera_finder).surfaceProvider)
        }
        imageCapture = ImageCapture.Builder().build()
        try {
            // Unbind use cases before rebinding
            provider.unbindAll()

            // Bind use cases to camera
            provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
        } catch(exception: Exception) {
            Log.e(TAG, "Use case binding failed", exception)
        }
    }

    private fun takePhoto() {
        if (imageCapture == null || !FirebaseAuthService.isSignedIn()) return

        model.onCameraProcessing()

        val outputFile = photoStore.createOutputFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        imageCapture!!.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    model.onPhotoCaptured(CameraOutput(outputFile))
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo save failed: ${exception.message}", exception)
                    model.onPhotoCaptureFailed()
                }
            }
        )
    }

    private fun requireCameraOrShutdown() {
        val requiredPermission = Manifest.permission.CAMERA
        if (requireActivity().isPermissionGranted(requiredPermission)) {
            startCamera()
        } else {
            requireActivity().requestPermission(requiredPermission, object : OnPermissionGrantedCallback {
                override fun onPermissionGranted() {
                    startCamera()
                }

                override fun onPermissionDenied() {
                    requireActivity().finish()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraFragment"
    }
}