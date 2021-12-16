/** Tom Nguyen: I wrote this file and it's corresponding xml files. **/
/** Shane Fretwell: I added some state to this file to help track which firebase endpoint should be called **/

package edu.uw.minh2804.rekognition.viewmodels

import android.speech.tts.TextToSpeech
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.uw.minh2804.rekognition.fragments.CameraOutput
import java.lang.Exception

// This view model is responsible for maintaining data and state of the current AccessibilityActivity.
class AccessibilityViewModel : ViewModel() {
    var speechEngine: TextToSpeech? = null

    private val _tabPosition = MutableLiveData<Int>()
    val tabPosition: LiveData<Int>
        get() = _tabPosition

    private val _capturedPhoto = MutableLiveData<CameraOutput>()
    val capturedPhoto: LiveData<CameraOutput>
        get() = _capturedPhoto

    private val _messageToUser = MutableLiveData<String?>()
    val messageToUser: LiveData<String?>
        get() = _messageToUser

    // This function is called when the user select a different option in the tab bar.
    fun onTabPositionChanged(index: Int) {
        _tabPosition.value = index
    }

    // This function is called when the camera finish capturing the photo.
    fun onCameraCaptured(photo: CameraOutput) {
        _capturedPhoto.value = photo
    }

    // This function is called when the camera predictively failed to capture the photo.
    fun onCameraCaptureFailed(e: Exception) {
        onCameraCaptureFailed(e.message!!)
    }

    // This function is called when the camera unexpectedly failed to capture the photo.
    // Hence the need to display a different generic message for the user, rather than an exception.
    fun onCameraCaptureFailed(displayMessage: String) {
        _messageToUser.value = displayMessage
    }

    // This function is called when the captured photo is about to be process for annotation.
    fun onImageAnnotating(displayMessage: String) {
        _messageToUser.value = displayMessage
    }

    // This function is called when the captured photo is processed and annotated.
    fun onImageAnnotated(displayMessage: String) {
        _messageToUser.value = displayMessage
    }

    // This function is called when annotation failed predictively.
    fun onImageAnnotateFailed(e: Exception) {
        onImageAnnotateFailed(e.message!!)
    }

    // This function is called when the annotation failed unexpectedly.
    // Hence the need to display a different generic message for the user, rather than an exception.
    fun onImageAnnotateFailed(displayMessage: String) {
        _messageToUser.value = displayMessage
    }

    // Needed to release the speechEngine resource.
    override fun onCleared() {
        super.onCleared()
        speechEngine?.shutdown()
    }
}