/** Tom Nguyen: I wrote this class and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.fragments

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.extensions.OnPermissionGrantedCallback
import edu.uw.minh2804.rekognition.extensions.isPermissionGranted
import edu.uw.minh2804.rekognition.extensions.requestPermission
import edu.uw.minh2804.rekognition.services.*
import edu.uw.minh2804.rekognition.stores.Annotation
import edu.uw.minh2804.rekognition.viewmodels.CameraState
import edu.uw.minh2804.rekognition.viewmodels.CameraViewModel
import kotlinx.coroutines.*

class CameraOutputFragment : Fragment(R.layout.fragment_output) {
    private val model: CameraViewModel by activityViewModels()
    private var currentEndpoint: FirebaseFunctionsService.Endpoint = FirebaseFunctionsService.Endpoint.TEXT
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireFirebaseOrShutdown()

        val outputView = view.findViewById<TextView>(R.id.text_output_overlay)

        model.firebaseEndpoint.observe(this) {
            Log.v(TAG, "Changing endpoint to: $it")
            currentEndpoint = it
        }

        model.cameraState.observe(this) {
            if (it == CameraState.CAPTURING) {
                outputView.text = getString(R.string.camera_output_on_processing)
                outputView.visibility = View.VISIBLE
            }
        }

        model.capturedPhoto.observe(this) {
            val seconds = 10L
            lifecycleScope.launch {
                try {
                    withTimeout(1000 * seconds) {
                        val result = currentEndpoint.apply(it.thumbnail.bitmap)
                        when {
                            result.fullTextAnnotation != null -> {
                                displayViewInFixedDuration(outputView, result.fullTextAnnotation.text)
                                model.onImageAnnotated(Annotation(result))
                            }
                            result.labelAnnotations != null -> {
                                displayViewInFixedDuration(outputView, result.labelAnnotations.first().description)
                                model.onImageAnnotated(Annotation(result))
                            }
                            else -> {
                                displayViewInFixedDuration(outputView, getString(R.string.camera_output_result_not_found))
                                model.onImageAnnotateFailed()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    displayViewInFixedDuration(outputView, getString(R.string.camera_output_internal_error))
                    model.onImageAnnotateFailed()
                }
            }
        }
    }

    private fun displayViewInFixedDuration(view: TextView, output: String) {
        // displayViewInXDuration could be previously called and the delay haven't elapsed yet,
        // so cancelling the previous call is needed to reset the clock.
        scope.coroutineContext.cancelChildren()
        view.text = output
        view.visibility = View.VISIBLE
        scope.launch {
            delay(1000 * TEXT_DISPLAY_DURATION_IN_SECONDS)
            view.visibility = View.INVISIBLE
        }
    }

    private fun requireFirebaseOrShutdown() {
        val requiredPermission = Manifest.permission.INTERNET
        if (!requireActivity().isPermissionGranted(requiredPermission)) {
            requireActivity().requestPermission(
                requiredPermission,
                object : OnPermissionGrantedCallback {
                    override fun onPermissionDenied() {
                        requireActivity().finish()
                    }
                })
        }
        if (!FirebaseAuthService.isSignedIn()) {
            lifecycleScope.launch {
                FirebaseAuthService.signIn()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }

    companion object {
        const val TEXT_DISPLAY_DURATION_IN_SECONDS = 5L
        private const val TAG = "OutputFragment"
    }
}