/** Tom Nguyen: I wrote this class and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.fragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.extensions.isPermissionGranted
import edu.uw.minh2804.rekognition.extensions.requestPermission
import edu.uw.minh2804.rekognition.services.FirebaseFunctionsService.Endpoint
import edu.uw.minh2804.rekognition.services.*
import edu.uw.minh2804.rekognition.stores.Annotation
import edu.uw.minh2804.rekognition.stores.AnnotationStore
import edu.uw.minh2804.rekognition.viewmodels.CameraState
import edu.uw.minh2804.rekognition.viewmodels.CameraViewModel
import kotlinx.coroutines.*

class CameraOutputFragment : Fragment(R.layout.fragment_output) {
    private val model: CameraViewModel by activityViewModels()
    private var currentEndpoint: Endpoint = Endpoint.TEXT
    private val viewVisibilityScope = CoroutineScope(Dispatchers.Default)
    private var speechEngine: TextToSpeech? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireFirebaseOrShutdown()
        requireSpeechEngine()

        val annotationStore = AnnotationStore(requireActivity())
        val outputView = view.findViewById<TextView>(R.id.text_output_overlay)

        model.firebaseEndpoint.observe(this) {
            Log.v(TAG, "Changing endpoint to: $it")
            currentEndpoint = it
        }

        model.cameraState.observe(this) {
            if (it == CameraState.CAPTURING) {
                speechEngine?.speak(getString(R.string.camera_output_on_processing), TextToSpeech.QUEUE_ADD, null, hashCode().toString())
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
                        val result = currentEndpoint.annotate(it.thumbnail.bitmap)
                        val formattedResult = currentEndpoint.formatResult(result)
                        formattedResult?.let { resultToDisplay ->
                            displayViewInFixedDuration(outputView, resultToDisplay)
                            speechEngine?.speak(resultToDisplay, TextToSpeech.QUEUE_ADD, null, id)
                            annotationStore.save(id, Annotation(result))
                            model.onImageAnnotated()
                        } ?: run {
                            val errorToDisplay = getString(
                                R.string.camera_output_result_not_found,
                                currentEndpoint.getResultType(requireContext())
                            )
                            displayViewInFixedDuration(outputView, errorToDisplay)
                            speechEngine?.speak(errorToDisplay, TextToSpeech.QUEUE_ADD, null, id)
                            model.onImageAnnotateFailed()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    val errorToDisplay = getString(R.string.camera_output_internal_error)
                    speechEngine?.speak(errorToDisplay, TextToSpeech.QUEUE_ADD, null, id)
                    displayViewInFixedDuration(outputView, errorToDisplay)
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
            delay(1000 * TEXT_DISPLAY_DURATION_IN_SECONDS)
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

    // https://android-developers.googleblog.com/2009/09/introduction-to-text-to-speech-in.html
    private fun requireSpeechEngine() {
        lifecycleScope.launch {
            val checkIntent = Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    speechEngine = TextToSpeech(requireContext()) {}
                } else {
                    val installIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                    startActivity(installIntent)
                }
            } .run { launch(checkIntent) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewVisibilityScope.cancel()
    }

    companion object {
        const val CONNECTION_TIMEOUT_IN_SECONDS = 10L
        const val TEXT_DISPLAY_DURATION_IN_SECONDS = 5L
        private const val TAG = "CameraOutputFragment"
    }
}