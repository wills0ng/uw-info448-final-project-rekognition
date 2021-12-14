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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.suspendCoroutine

class CameraOutputFragment : Fragment(R.layout.fragment_output) {
    private val model: CameraViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireFirebaseOrShutdown()

        val output = view.findViewById<TextView>(R.id.text_output_overlay)

        observeCameraState(output)
        observeCapturedPhoto(output)
        observeEncounteredError(output)
        observeImageAnnotation(output)
    }

    private fun observeCameraState(output: TextView) {
        model.cameraState.observe(this) {
            if (it == CameraState.CAPTURING) {
                output.text = getString(R.string.camera_output_on_processing)
                output.visibility = View.VISIBLE
            }
        }
    }

    private fun observeCapturedPhoto(output: TextView) {
        model.capturedPhoto.observe(this) {
            lifecycleScope.launch {
                try {
                    val result = FirebaseFunctionsService.annotateImage(it.thumbnail.bitmap)
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
    }

    private fun observeEncounteredError(output: TextView) {
        model.encounteredError.observe(this) {
            output.text = it.message!!
            output.visibility = View.VISIBLE
            viewLifecycleOwner.lifecycleScope.launch {
                delay(1000 * TEXT_DISPLAY_DURATION_IN_SECONDS.toLong())
                output.visibility = View.INVISIBLE
            }
        }
    }

    private fun observeImageAnnotation(output: TextView) {
        model.imageAnnotation.observe(this) {
            if (it != null) {
                output.text = it.result.fullTextAnnotation!!.text
                output.visibility = View.VISIBLE
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(1000 * TEXT_DISPLAY_DURATION_IN_SECONDS.toLong())
                    output.visibility = View.INVISIBLE
                }
            }
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

    companion object {
        const val TEXT_DISPLAY_DURATION_IN_SECONDS = 5
        private const val TAG = "OutputFragment"
    }
}