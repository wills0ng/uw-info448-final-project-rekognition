/** Tom Nguyen: I wrote this class and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.services.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// CameraFragment class is taken and modified from https://developer.android.com/codelabs/camerax-getting-started#0
class CameraFragment : Fragment(R.layout.fragment_camera) {
    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!FirebaseAuthService.isSignedIn()) {
            FirebaseAuthService.signIn(object : OnSignedInCallback {
                override fun onSignedIn() {}
                override fun onError(exception: java.lang.Exception) {
                    Log.e(TAG, exception.toString())
                    requireActivity().finish()
                }
            })
        }

        if (isPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

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
        textView.text = "Processing..."
        textView.visibility = View.VISIBLE

        imageCapture!!.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(outputFile)
                    val bitmap = BitmapFactory.decodeFile(savedUri.path)

                    TextRecognitionService.processImage(bitmap, object : OnTextProcessedCallback {
                        override fun onResultFound(annotation: TextAnnotation) {
                            textView.text = annotation.text
                            GlobalScope.launch {
                                Thread.sleep(1000 * 5)
                                view?.findViewById<TextView>(R.id.text_output_overlay)?.let {
                                    if (it.text != "Processing...") {
                                        it.visibility = View.INVISIBLE
                                    }
                                }
                            }
                        }

                        override fun onResultNotFound() {
                            textView.text = "No text detected"
                            GlobalScope.launch {
                                Thread.sleep(1000 * 5)
                                view?.findViewById<TextView>(R.id.text_output_overlay)?.let {
                                    if (it.text != "Processing...") {
                                        it.visibility = View.INVISIBLE
                                    }
                                }
                            }
                        }

                        override fun onError(exception: Exception) {
                            Log.e(TAG, "Photo processed failed: ${exception.message}", exception)
                            textView.text = "Something went wrong, please try again later."
                            GlobalScope.launch {
                                Thread.sleep(1000 * 5)
                                view?.findViewById<TextView>(R.id.text_output_overlay)?.let {
                                    if (it.text != "Processing...") {
                                        it.visibility = View.INVISIBLE
                                    }
                                }
                            }
                        }
                    })
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo save failed: ${exception.message}", exception)
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(requireView().findViewById<PreviewView>(R.id.preview_camera_finder).surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch(exception: Exception) {
                Log.e(TAG, "Use case binding failed", exception)
            }
        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else requireActivity().filesDir
    }

    private fun createUniqueOutputFile(): File {
        val uniqueFileName = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        return File(outputDirectory, uniqueFileName)
    }

    private fun isPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(requireActivity(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.forEach { (_, isGranted) ->
                if (isGranted) {
                    startCamera()
                } else {
                    Toast.makeText(requireActivity(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                }
            }
        } .run { launch(REQUIRED_PERMISSIONS) }
    }

    private companion object {
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val TAG = "CameraFragment"
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.INTERNET)
    }
}