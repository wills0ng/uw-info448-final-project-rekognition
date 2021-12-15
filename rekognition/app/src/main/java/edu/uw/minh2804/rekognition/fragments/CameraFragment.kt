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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.extensions.isPermissionGranted
import edu.uw.minh2804.rekognition.extensions.requestPermission
import edu.uw.minh2804.rekognition.services.*
import edu.uw.minh2804.rekognition.stores.*
import edu.uw.minh2804.rekognition.viewmodels.CameraViewModel
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.launch

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

        view.findViewById<TabLayout>(R.id.tab_layout_camera_navigation).addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    Log.v(TAG, "${tab!!.text.toString()} tab selected")
                    model.onSetCameraTab(
                        when (tab!!.text) {
                            getString(R.string.camera_text_recognition) -> FirebaseFunctionsService.Endpoint.TEXT
                            getString(R.string.camera_image_labeling) -> FirebaseFunctionsService.Endpoint.OBJECT
                            else -> {
                                Log.e(TAG, "Tab label ${tab.text} inconsistent with resources")
                                Log.v(TAG, "Setting firebase endpoint to default: TEXT")
                                FirebaseFunctionsService.Endpoint.TEXT
                            }
                        }
                    )
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            }
        )

        cameraExecutor = Executors.newSingleThreadExecutor()
        photoStore = PhotoStore(requireActivity())
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable
            { displayCameraFeed(cameraProviderFuture.get()) },
            ContextCompat.getMainExecutor(requireContext())
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
        } catch(e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    private fun takePhoto() {
        if (imageCapture == null || !FirebaseAuthService.isSignedIn()) return

        model.onCameraCapturing()

        val outputFile = photoStore.createOutputFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        imageCapture!!.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    model.onCameraCaptured(CameraOutput(outputFile))
                }

                override fun onError(e: ImageCaptureException) {
                    Log.e(TAG, "Photo save failed: ${e.message}", e)
                    model.onCameraCaptureFailed()
                }
            }
        )
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

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraFragment"
    }
}