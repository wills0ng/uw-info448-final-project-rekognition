/** Tom Nguyen: I wrote this file and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.fragments

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.viewmodels.CameraViewModel
import kotlinx.coroutines.*

// This fragment is responsible for outputting messages to the user.
// The message will always be outputted by text,
// In addition, if text to speech is supported by the android device,
// then voice will also be outputted.
class ResponseFragment : Fragment(R.layout.fragment_response) {
    private val model: CameraViewModel by activityViewModels()
    private val viewVisibilityScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireSpeechEngineOrIgnore()

        model.speechEngine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // This is to prevent TTS from speaking twice when screen rotate.
                model.speechEngine?.stop()
            }
            override fun onDone(utteranceId: String?) {}
            override fun onError(utteranceId: String?) {}
        })

        model.messageToUser.observe(this) { outputMessageInFixedDuration(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewVisibilityScope.cancel()
    }

    private fun outputMessageInFixedDuration(message: String?) {
        message ?: return

        // This function could be called previously and the duration to toggle text off might haven't elapsed yet,
        // so cancelling the previous call is needed to reset the clock.
        viewVisibilityScope.coroutineContext.cancelChildren()

        val textView = requireView() as TextView
        speak(message)
        textView.text = message
        textView.visibility = View.VISIBLE
        viewVisibilityScope.launch {
            delay(1000 * OUTPUT_DISPLAY_DURATION_IN_SECONDS)
            textView.visibility = View.INVISIBLE
        }
    }

    private fun speak(message: String) {
        model.speechEngine?.speak(message, TextToSpeech.QUEUE_FLUSH, null, hashCode().toString())
    }

    // See more: https://android-developers.googleblog.com/2009/09/introduction-to-text-to-speech-in.html
    private fun requireSpeechEngineOrIgnore() {
        val checkForTtsData = Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                model.speechEngine = TextToSpeech(requireContext()) {}
            } else {
                val redirectToInstall = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                startActivity(redirectToInstall)
            }
        } .run { launch(checkForTtsData) }
    }

    companion object {
        const val OUTPUT_DISPLAY_DURATION_IN_SECONDS = 5L
    }
}