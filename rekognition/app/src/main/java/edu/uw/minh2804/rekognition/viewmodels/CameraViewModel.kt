package edu.uw.minh2804.rekognition.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.uw.minh2804.rekognition.fragments.CameraOutput
import edu.uw.minh2804.rekognition.stores.Annotation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CameraState {
    IDLE, PROCESSING, PROCESSED, FAILED
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

    fun onCameraProcessing() {
        _cameraState.value = CameraState.PROCESSING
    }

    fun onPhotoCaptured(output: CameraOutput) {
        _capturedPhoto.value = output
        _cameraState.value = CameraState.PROCESSED
    }

    fun onPhotoCaptureFailed() {
        _cameraState.value = CameraState.FAILED
        viewModelScope.launch {
            delay(1000 * CAMERA_STATE_CHANGE_DURATION.toLong())
            _cameraState.value = CameraState.IDLE
        }
    }

    fun onImageAnnotated(output: Annotation?) {
        _imageAnnotation.value = output
        viewModelScope.launch {
            delay(1000 * CAMERA_STATE_CHANGE_DURATION.toLong())
            _cameraState.value = CameraState.IDLE
        }
    }

    companion object {
        const val CAMERA_STATE_CHANGE_DURATION = 4
    }
}