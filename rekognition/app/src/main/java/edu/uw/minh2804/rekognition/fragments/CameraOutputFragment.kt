/** Tom Nguyen: I wrote this class and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import edu.uw.minh2804.rekognition.MainActivity.Companion.CONNECTION_TIMEOUT_IN_SECONDS
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.extensions.isPermissionGranted
import edu.uw.minh2804.rekognition.extensions.requestPermission
import edu.uw.minh2804.rekognition.services.*
import edu.uw.minh2804.rekognition.stores.*
import edu.uw.minh2804.rekognition.stores.Annotation
import edu.uw.minh2804.rekognition.viewmodels.CameraViewModel
import kotlinx.coroutines.*

class CameraOutputFragment : Fragment(R.layout.fragment_output) {
    private lateinit var thumbnailStore: ThumbnailStore
    private val model: CameraViewModel by activityViewModels()
    private val viewVisibilityScope = CoroutineScope(Dispatchers.Default)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireFirebaseOrIgnore()
        requireSpeechEngineOrIgnore()

        thumbnailStore = ThumbnailStore(requireActivity())

        val annotationStore = AnnotationStore(requireActivity())
        val outputView = view.findViewById<TextView>(R.id.text_output_overlay)

        model.displayMessage.observe(this) {
            displayMessageInFixedDuration(outputView, it)
        }

        model.capturedPhoto.observe(this) { cameraOutput ->
            if (!cameraOutput.isProcessed) {
                lifecycleScope.launch {
                    val id = cameraOutput.id
                    if (isInternetEnable()) {
                        val processing = getString(R.string.camera_output_on_processing)
                        speak(id, processing)
                        displayMessageInFixedDuration(outputView, processing)
                        val thumbnail = Thumbnail(cameraOutput.image)
                        launch { thumbnailStore.save(cameraOutput.id, thumbnail) }
                        try {
                            // If it takes more than 10 seconds to retrieve the result, then a TimeoutCancellationException will be thrown.
                            withTimeout(1000 * CONNECTION_TIMEOUT_IN_SECONDS) {
                                val result = cameraOutput.requestAnnotator.annotate(thumbnail.bitmap)
                                val formattedResult = cameraOutput.requestAnnotator.formatResult(result)
                                if (formattedResult != null) {
                                    speak(id, formattedResult)
                                    model.onImageAnnotated(formattedResult)
                                    annotationStore.save(cameraOutput.id, Annotation(result))
                                } else {
                                    val errorToDisplay = getString(
                                        R.string.camera_output_result_not_found,
                                        cameraOutput.requestAnnotator.getResultType(requireContext())
                                    )
                                    speak(id, errorToDisplay)
                                    model.onImageAnnotated(errorToDisplay)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, e.toString())
                            val errorToDisplay = getString(R.string.camera_output_internal_error)
                            speak(id, errorToDisplay)
                            displayMessageInFixedDuration(outputView, errorToDisplay)
                            model.onImageAnnotateFailed(errorToDisplay)
                        }
                        cameraOutput.isProcessed = true
                    } else {
                        val requireInternet = getString(R.string.require_internet)
                        speak(id, requireInternet)
                        displayMessageInFixedDuration(outputView, requireInternet)
                    }
                }
            }
        }
    }

    private fun speak(id: String, message: String) {
        model.speechEngine?.speak(message, TextToSpeech.QUEUE_ADD, null, id)
    }

    private fun displayMessageInFixedDuration(view: TextView, message: String) {
        // displayViewInFixedDuration could be previously called and the delay haven't elapsed yet,
        // so cancelling the previous call is needed to reset the clock.
        viewVisibilityScope.coroutineContext.cancelChildren()
        view.text = message
        view.visibility = View.VISIBLE
        viewVisibilityScope.launch {
            delay(1000 * TEXT_DISPLAY_DURATION_IN_SECONDS)
            view.visibility = View.INVISIBLE
        }
    }

    // https://developer.android.com/training/monitoring-device-state/connectivity-status-type
    private fun isInternetEnable(): Boolean {
        val cm = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetwork != null
    }

    private fun requireFirebaseOrIgnore() {
        lifecycleScope.launch {
            val requiredPermission = Manifest.permission.INTERNET
            if (!requireActivity().isPermissionGranted(requiredPermission)) {
                requireActivity().requestPermission(requiredPermission)
            }
            if (!FirebaseAuthService.isSignedIn()) {
                FirebaseAuthService.signIn()
            }
        }
    }

    // https://android-developers.googleblog.com/2009/09/introduction-to-text-to-speech-in.html
    private fun requireSpeechEngineOrIgnore() {
        val checkIntent = Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                model.speechEngine = TextToSpeech(requireContext()) {}
            } else {
                val installIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                startActivity(installIntent)
            }
        } .run { launch(checkIntent) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewVisibilityScope.cancel()
    }

    companion object {
        const val TEXT_DISPLAY_DURATION_IN_SECONDS = 5L
        private const val TAG = "CameraOutputFragment"
    }
}