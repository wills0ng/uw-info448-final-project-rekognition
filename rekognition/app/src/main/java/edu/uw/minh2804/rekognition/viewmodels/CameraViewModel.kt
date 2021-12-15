package edu.uw.minh2804.rekognition.viewmodels

import android.speech.tts.TextToSpeech
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.uw.minh2804.rekognition.fragments.CameraOutput
import java.lang.Exception

enum class CameraState {
    CAPTURING, CAPTURED, IDLE
}

class CameraViewModel : ViewModel() {
    var speechEngine: TextToSpeech? = null

    private val _tabPosition = MutableLiveData<Int>()
    val tabPosition: LiveData<Int>
        get() = _tabPosition

    private val _cameraState = MutableLiveData<CameraState>()
    val cameraState: LiveData<CameraState>
        get() = _cameraState

    private val _capturedPhoto = MutableLiveData<CameraOutput>()
    val capturedPhoto: LiveData<CameraOutput>
        get() = _capturedPhoto

    private val _displayMessage = MutableLiveData<String>()
    val displayMessage: LiveData<String>
        get() = _displayMessage

    fun onTabPositionChanged(index: Int) {
        _tabPosition.value = index
    }

    fun onCameraCapturing() {
        _cameraState.value = CameraState.CAPTURING
    }

    fun onCameraCaptured(photo: CameraOutput) {
        _cameraState.value = CameraState.CAPTURED
        _capturedPhoto.value = photo
    }

    fun onCameraCaptured(e: Exception) {
    }

    fun onCameraCaptureFailed() {
        _cameraState.value = CameraState.IDLE
    }

    fun onImageAnnotated(displayMessage: String) {
        _cameraState.value = CameraState.IDLE
        _displayMessage.value = displayMessage
    }

    fun onImageAnnotateFailed(displayMessage: String) {
        _cameraState.value = CameraState.IDLE
        _displayMessage.value = displayMessage
    }

    override fun onCleared() {
        super.onCleared()
        speechEngine?.shutdown()
    }
}