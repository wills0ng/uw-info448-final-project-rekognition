/** Tom Nguyen: I wrote this class and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.fragments

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import edu.uw.minh2804.rekognition.services.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// CameraFragment class is taken and modified from https://developer.android.com/codelabs/camerax-getting-started#0
class CameraFragment : Fragment(R.layout.fragment_camera) {
    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireFirebase()
        requireCamera()

        view.findViewById<ImageButton>(R.id.button_camera_capture).setOnClickListener { takePhoto() }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun takePhoto() {
        if (imageCapture == null || !FirebaseAuthService.isSignedIn()) {
            return
        }

        val outputFile = createUniqueOutputFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        val textView = requireView().findViewById<TextView>(R.id.text_output_overlay)
        textView.text = LOADING_OUTPUT_TEXT
        textView.visibility = View.VISIBLE

        imageCapture!!.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(outputFile)
                    val bitmap = BitmapFactory.decodeFile(savedUri.path)
                    processImage(bitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo save failed: ${exception.message}", exception)
                }
            }
        )
    }

    private fun processImage(bitmap: Bitmap) {
        TextRecognitionService.processImage(bitmap, object : OnTextProcessedCallback {
            override fun onResultFound(annotation: TextAnnotation) {
                displayText(annotation.text, 5)
            }

            override fun onResultNotFound() {
                displayText("No text detected", 5)
            }

            override fun onError(exception: Exception) {
                Log.e(TAG, "Photo processed failed: ${exception.message}", exception)
                displayText("Something went wrong, please try again later.", 5)
            }
        })
    }

    private fun displayText(text: String, durationInSeconds: Long) {
        val textView = requireView().findViewById<TextView>(R.id.text_output_overlay).apply { this.text = text }
        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000 * durationInSeconds)
            textView?.let {
                if (it.text != LOADING_OUTPUT_TEXT) {
                    it.visibility = View.INVISIBLE
                }
            }
        }
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

    private fun createUniqueOutputFile(): File {
        val uniqueFileName = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        return File(outputDirectory, uniqueFileName)
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else requireActivity().filesDir
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

    private companion object {
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val LOADING_OUTPUT_TEXT = "Processing..."
        const val TAG = "CameraFragment"
    }
}