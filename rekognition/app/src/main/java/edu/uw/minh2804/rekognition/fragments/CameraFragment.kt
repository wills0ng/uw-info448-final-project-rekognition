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

data class CameraOutput(val id: String, val image: File, val requestAnnotator: Annotator)

// CameraFragment class is taken and modified from https://developer.android.com/codelabs/camerax-getting-started#0
class CameraFragment : Fragment(R.layout.fragment_camera) {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var photoStore: PhotoStore

    private val model: CameraViewModel by activityViewModels()
    private var imageCapture: ImageCapture? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireCameraOrShutdown()

        val tabs = view.findViewById<TabLayout>(R.id.tab_layout_camera_navigation)

        model.tabPosition.observe(this) {
            tabs.selectTab(tabs.getTabAt(it))
        }

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) { model.onTabPositionChanged(tab!!.position) }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        view.findViewById<ImageButton>(R.id.button_camera_capture).setOnClickListener {
            val outputFile = photoStore.createOutputFile()
            val id = outputFile.nameWithoutExtension
            when (tabs.selectedTabPosition) {
                0 -> takePhoto(CameraOutput(id, outputFile, FirebaseFunctionsService.Endpoint.TEXT))
                1 -> takePhoto(CameraOutput(id, outputFile, FirebaseFunctionsService.Endpoint.OBJECT))
                else -> Log.e(TAG, "Selected tab position not found")
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        photoStore = PhotoStore(requireActivity())
    }

    private fun takePhoto(output: CameraOutput) {
        if (imageCapture == null || !FirebaseAuthService.isSignedIn()) return

        model.onCameraCapturing()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(output.image).build()

        imageCapture!!.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                    model.onCameraCaptured(output)
                    lifecycleScope.launch { photoStore.save(output.id, Photo(output.image)) }
                }

                override fun onError(e: ImageCaptureException) {
                    Log.e(TAG, "Photo save failed: ${e.message}", e)
                    model.onCameraCaptureFailed()
                }
            }
        )
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