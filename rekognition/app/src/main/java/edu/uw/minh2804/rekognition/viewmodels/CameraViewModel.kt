package edu.uw.minh2804.rekognition.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.uw.minh2804.rekognition.fragments.CameraOutput
import edu.uw.minh2804.rekognition.stores.Annotation
import java.lang.Exception

enum class CameraState {
    IDLE, CAPTURING, CAPTURED
}

class CameraViewModel : ViewModel() {
    private val _cameraState = MutableLiveData<CameraState>()
    val cameraState: LiveData<CameraState>
        get() = _cameraState

    private val _capturedPhoto = MutableLiveData<CameraOutput>()
    val capturedPhoto: LiveData<CameraOutput>
        get() = _capturedPhoto

    private val _imageAnnotation = MutableLiveData<Annotation?>()
    val imageAnnotation: LiveData<Annotation?>
        get() = _imageAnnotation

    private val _encounteredError = MutableLiveData<Exception>()
    val encounteredError: LiveData<Exception>
        get() = _encounteredError

    fun onCameraCapturing() {
        _cameraState.value = CameraState.CAPTURING
    }

    fun onCameraCaptured(photo: CameraOutput) {
        _capturedPhoto.value = photo
        _cameraState.value = CameraState.CAPTURED
    }

    fun onCameraCaptureFailed(exception: Exception) {
        _encounteredError.value = exception
        _cameraState.value = CameraState.IDLE
    }

    fun onImageAnnotated(output: Annotation) {
        _imageAnnotation.value = output
        _cameraState.value = CameraState.IDLE
    }

    fun onImageAnnotateFailed(exception: Exception) {
        _imageAnnotation.value = null
        _encounteredError.value = exception
        _cameraState.value = CameraState.IDLE
    }
}