package edu.uw.minh2804.rekognition.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.uw.minh2804.rekognition.fragments.CameraOutput
import edu.uw.minh2804.rekognition.services.FirebaseFunctionsService.Endpoint

enum class CameraState {
    CAPTURING, CAPTURED, IDLE
}

class CameraViewModel : ViewModel() {
    private val _firebaseEndpoint = MutableLiveData<Endpoint>()
    val firebaseEndpoint: LiveData<Endpoint>
        get() = _firebaseEndpoint

    private val _cameraState = MutableLiveData<CameraState>()
    val cameraState: LiveData<CameraState>
        get() = _cameraState

    private val _capturedPhoto = MutableLiveData<CameraOutput>()
    val capturedPhoto: LiveData<CameraOutput>
        get() = _capturedPhoto

    fun onSetCameraTab(endpoint: Endpoint) {
        _firebaseEndpoint.value = endpoint
    }

    fun onCameraCapturing() {
        _cameraState.value = CameraState.CAPTURING
    }

    fun onCameraCaptured(photo: CameraOutput) {
        _cameraState.value = CameraState.CAPTURED
        _capturedPhoto.value = photo
    }

    fun onCameraCaptureFailed() {
        _cameraState.value = CameraState.IDLE
    }

    fun onImageAnnotated() {
        _cameraState.value = CameraState.IDLE
    }

    fun onImageAnnotateFailed() {
        _cameraState.value = CameraState.IDLE
    }
}