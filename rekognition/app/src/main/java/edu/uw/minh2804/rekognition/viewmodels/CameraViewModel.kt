/** Tom Nguyen: I wrote this file and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.viewmodels

import android.speech.tts.TextToSpeech
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.uw.minh2804.rekognition.fragments.CameraOutput
import java.lang.Exception

// This view model is responsible for maintaining data and state of the current AccessibilityActivity.
class CameraViewModel : ViewModel() {
    var speechEngine: TextToSpeech? = null

    private val _tabPosition = MutableLiveData<Int>()
    val tabPosition: LiveData<Int>
        get() = _tabPosition

    private val _capturedPhoto = MutableLiveData<CameraOutput>()
    val capturedPhoto: LiveData<CameraOutput>
        get() = _capturedPhoto

    private val _messageToUser = MutableLiveData<String>()
    val messageToUser: LiveData<String>
        get() = _messageToUser

    fun onTabPositionChanged(index: Int) {
        _tabPosition.value = index
    }

    fun onCameraCaptured(photo: CameraOutput) {
        _capturedPhoto.value = photo
    }

    fun onCameraCaptureFailed(displayMessage: String) {
        _messageToUser.value = displayMessage
    }

    fun onCameraCaptureFailed(e: Exception) {
        onCameraCaptureFailed(e.message!!)
    }

    fun onImageAnnotating(displayMessage: String) {
        _messageToUser.value = displayMessage
    }

    fun onImageAnnotated(displayMessage: String) {
        _messageToUser.value = displayMessage
    }

    fun onImageAnnotateFailed(displayMessage: String) {
        _messageToUser.value = displayMessage
    }

    fun onImageAnnotateFailed(e: Exception) {
        onImageAnnotateFailed(e.message!!)
    }

    override fun onCleared() {
        super.onCleared()
        speechEngine?.shutdown()
    }
}