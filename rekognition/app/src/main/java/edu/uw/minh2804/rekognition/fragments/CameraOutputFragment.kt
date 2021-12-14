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
import edu.uw.minh2804.rekognition.extensions.isPermissionGranted
import edu.uw.minh2804.rekognition.extensions.requestPermission
import edu.uw.minh2804.rekognition.services.*
import edu.uw.minh2804.rekognition.stores.Annotation
import edu.uw.minh2804.rekognition.stores.AnnotationStore
import edu.uw.minh2804.rekognition.viewmodels.CameraState
import edu.uw.minh2804.rekognition.viewmodels.CameraViewModel
import kotlinx.coroutines.*

class CameraOutputFragment : Fragment(R.layout.fragment_output) {
    private val model: CameraViewModel by activityViewModels()
    private val viewVisibilityScope = CoroutineScope(Dispatchers.Default)
    private var currentEndpoint: FirebaseFunctionsService.Endpoint = FirebaseFunctionsService.Endpoint.TEXT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireFirebaseOrShutdown()

        val annotationStore = AnnotationStore(requireActivity())
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
            lifecycleScope.launch {
                val id = it.photo.file.nameWithoutExtension
                try {
                    // If it takes more than 10 seconds to retrieve the result, then a TimeoutCancellationException will be thrown.
                    withTimeout(1000 * CONNECTION_TIMEOUT_IN_SECONDS) {
                        val result = currentEndpoint.apply(it.thumbnail.bitmap)
                        when {
                            result.fullTextAnnotation != null -> {
                                displayViewInFixedDuration(outputView, result.fullTextAnnotation.text)
                                annotationStore.save(id, Annotation(result))
                                model.onImageAnnotated()
                            }
                            result.labelAnnotations.any() -> {
                                displayViewInFixedDuration(outputView, result.labelAnnotations.joinToString { it.description })
                                annotationStore.save(id, Annotation(result))
                                model.onImageAnnotated()
                            }
                            else -> {
                                when (currentEndpoint) {
                                    FirebaseFunctionsService.Endpoint.TEXT ->
                                        displayViewInFixedDuration(outputView, getString(R.string.camera_output_result_not_found, "text"))
                                    FirebaseFunctionsService.Endpoint.OBJECT ->
                                        displayViewInFixedDuration(outputView, getString(R.string.camera_output_result_not_found, "object"))
                                }
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
        // displayViewInFixedDuration could be previously called and the delay haven't elapsed yet,
        // so cancelling the previous call is needed to reset the clock.
        viewVisibilityScope.coroutineContext.cancelChildren()
        view.text = output
        view.visibility = View.VISIBLE
        viewVisibilityScope.launch {
            delay(1000 * VIEW_VISIBILITY_DURATION_IN_SECONDS)
            view.visibility = View.INVISIBLE
        }
    }

    private fun requireFirebaseOrShutdown() {
        lifecycleScope.launch {
            val requiredPermission = Manifest.permission.INTERNET
            if (!requireActivity().isPermissionGranted(requiredPermission)) {
                if (!requireActivity().requestPermission(requiredPermission)) {
                    requireActivity().finish()
                }
            }
            if (!FirebaseAuthService.isSignedIn()) {
                FirebaseAuthService.signIn()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewVisibilityScope.cancel()
    }

    companion object {
        const val CONNECTION_TIMEOUT_IN_SECONDS = 10L
        const val VIEW_VISIBILITY_DURATION_IN_SECONDS = 5L
        private const val TAG = "CameraOutputFragment"
    }
}