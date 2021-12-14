/** Tom Nguyen: I wrote this class and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.fragments

import android.Manifest
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
                displayViewInXDuration(outputView)
            }
        }

        model.capturedPhoto.observe(this) {
            lifecycleScope.launch {
                try {
                    val result = currentEndpoint.apply(it.thumbnail.bitmap)
                    if (result.fullTextAnnotation != null) {
                        model.onImageAnnotated(Annotation(result))
                    } else {
                        model.onImageAnnotateFailed(Exception(getString(R.string.camera_output_result_not_found)))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    model.onImageAnnotateFailed(Exception(getString(R.string.camera_output_internal_error)))
                }
            }
        }

        model.encounteredError.observe(this) {
            outputView.text = it.message!!
            displayViewInXDuration(outputView)
        }

        model.imageAnnotation.observe(this) {
            if (it != null) {
                outputView.text = it.result.fullTextAnnotation!!.text
                displayViewInXDuration(outputView)
            }
        }
    }

    private fun displayViewInXDuration(view: TextView) {
        // displayViewInXDuration could be previously called and the delay haven't elapsed yet,
        // so cancelling the previous call is needed to reset the clock.
        scope.coroutineContext.cancelChildren()
        view.visibility = View.VISIBLE
        scope.launch {
            delay(1000 * TEXT_DISPLAY_DURATION_IN_SECONDS.toLong())
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
        const val TEXT_DISPLAY_DURATION_IN_SECONDS = 5
        private const val TAG = "OutputFragment"
    }
}