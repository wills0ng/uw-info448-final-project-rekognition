/** Tom Nguyen: I wrote this class and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.fragments

import android.Manifest
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.extensions.OnPermissionGrantedCallback
import edu.uw.minh2804.rekognition.extensions.isPermissionGranted
import edu.uw.minh2804.rekognition.extensions.requestPermission
import edu.uw.minh2804.rekognition.extensions.scaleDown
import edu.uw.minh2804.rekognition.services.*
import edu.uw.minh2804.rekognition.stores.ImageProcessingResult
import edu.uw.minh2804.rekognition.stores.ImageProcessingResultStatusCode
import edu.uw.minh2804.rekognition.stores.ImageProcessingStore
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// CameraFragment class is taken and modified from https://developer.android.com/codelabs/camerax-getting-started#0
class CameraFragment : Fragment(R.layout.fragment_camera) {
    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageProcessingStore: ImageProcessingStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireFirebase()
        requireCamera()

        view.findViewById<ImageButton>(R.id.button_camera_capture).setOnClickListener { takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()
        imageProcessingStore = ImageProcessingStore(requireActivity())
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(requireView().findViewById<PreviewView>(R.id.preview_camera_finder).surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()

            try {
                // Must unbind all use cases before rebinding
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch(exception: Exception) {
                Log.e(TAG, "Use case binding failed", exception)
            }
        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    private fun takePhoto() {
        if (imageCapture == null || !FirebaseAuthService.isSignedIn()) {
            return
        }

        val outputFile = imageProcessingStore.createUniqueImageOutputFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        val textView = requireView().findViewById<TextView>(R.id.text_output_overlay)
        textView.text = PROCESSING_IMAGE_OUTPUT
        textView.visibility = View.VISIBLE

        imageCapture!!.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    processImage(outputFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo save failed: ${exception.message}", exception)
                }
            }
        )
    }

    private fun processImage(savedImageFile: File) {
        val savedImageUriPath = savedImageFile.toURI().path
        val scaledDownBitmap = BitmapFactory.decodeFile(savedImageUriPath).scaleDown(ThumbnailSetting.MAX_DIMENSION)

        TextRecognitionService.processImage(scaledDownBitmap, object : OnTextProcessedCallback {
            override fun onResultFound(annotation: TextAnnotation) {
                val savedThumbnailUriPath = imageProcessingStore.saveImageToThumbnailFile(savedImageFile).toURI().path
                val result = ImageProcessingResult(savedImageUriPath, savedThumbnailUriPath, annotation.text, ImageProcessingResultStatusCode.RESULT_FOUND)

                imageProcessingStore.saveResultToFile(savedImageFile, result)
                displayText(annotation.text, OUTPUT_DISPLAY_DURATION)
            }

            override fun onResultNotFound() {
                val savedThumbnailUriPath = imageProcessingStore.saveImageToThumbnailFile(savedImageFile).toURI().path
                val result = ImageProcessingResult(savedImageUriPath, savedThumbnailUriPath, RESULT_NOT_FOUND_OUTPUT, ImageProcessingResultStatusCode.RESULT_NOT_FOUND)

                imageProcessingStore.saveResultToFile(savedImageFile, result)
                displayText(RESULT_NOT_FOUND_OUTPUT, OUTPUT_DISPLAY_DURATION)
            }

            override fun onError(exception: Exception) {
                Log.e(TAG, "Photo processed failed: ${exception.message}", exception)
                displayText("Something went wrong, please try again later.", OUTPUT_DISPLAY_DURATION)
            }
        })
    }

    private fun displayText(text: String, durationInSeconds: Int) {
        val textView = requireView().findViewById<TextView>(R.id.text_output_overlay).apply { this.text = text }
        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000 * durationInSeconds.toLong())
            textView?.let {
                if (it.text != PROCESSING_IMAGE_OUTPUT) {
                    it.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun requireFirebase() {
        val requiredPermission = Manifest.permission.INTERNET
        if (!requireActivity().isPermissionGranted(requiredPermission)) {
            requireActivity().requestPermission(requiredPermission, object : OnPermissionGrantedCallback {
                override fun onPermissionDenied() {
                    requireActivity().finish()
                }
            })
        }
        if (!FirebaseAuthService.isSignedIn()) {
            FirebaseAuthService.signIn(object : OnSignedInCallback {
                override fun onError(exception: java.lang.Exception) {
                    Log.e(TAG, exception.toString())
                    requireActivity().finish()
                }
            })
        }
    }

    private fun requireCamera() {
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

    private companion object {
        const val OUTPUT_DISPLAY_DURATION = 5
        const val PROCESSING_IMAGE_OUTPUT = "Processing..."
        const val RESULT_NOT_FOUND_OUTPUT = "No text detected"
        const val TAG = "CameraFragment"
    }
}