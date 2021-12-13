/** Tom Nguyen: I wrote this class and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.extensions.OnPermissionGrantedCallback
import edu.uw.minh2804.rekognition.extensions.isPermissionGranted
import edu.uw.minh2804.rekognition.extensions.requestPermission
import edu.uw.minh2804.rekognition.services.*
import edu.uw.minh2804.rekognition.stores.Annotation
import edu.uw.minh2804.rekognition.viewmodels.CameraState
import edu.uw.minh2804.rekognition.viewmodels.CameraViewModel

class OutputFragment : Fragment(R.layout.fragment_output) {
    private val model: CameraViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireFirebaseOrShutdown()

        val outputView = view.findViewById<TextView>(R.id.text_output_overlay)

        model.cameraState.observe(this) {
            when(it) {
                CameraState.IDLE -> outputView.visibility = View.INVISIBLE
                CameraState.PROCESSING -> {
                    outputView.text = "Processing..."
                    outputView.visibility = View.VISIBLE
                }
                else -> {
                    outputView.visibility = View.VISIBLE
                }
            }
        }

        model.capturedPhoto.observe(this) {
            FirebaseFunctionsService.annotateImage(it.thumbnail.bitmap, object : FirebaseFunctionsCallback {
                override fun onProcessed(annotation: BatchAnnotateImagesResponse) {
                    val result = annotation.responses.first().fullTextAnnotation
                    if (result != null) {
                        outputView.text = result.text
                        model.onImageAnnotated(Annotation(annotation))
                    } else {
                        outputView.text = "No text detected"
                        model.onImageAnnotated(null)
                    }
                }

                override fun onError(exception: Exception) {
                    Log.e(TAG, exception.toString())
                    outputView.text = "Something went wrong, please try again later."
                    model.onImageAnnotated(null)
                }
            })
        }
    }

    private fun requireFirebaseOrShutdown() {
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

    companion object {
        private const val TAG = "OutputFragment"
    }
}