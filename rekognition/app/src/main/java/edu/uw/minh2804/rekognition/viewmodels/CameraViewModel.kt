package edu.uw.minh2804.rekognition.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.uw.minh2804.rekognition.CameraActivity
import edu.uw.minh2804.rekognition.fragments.CameraOutput
import edu.uw.minh2804.rekognition.services.FirebaseFunctionsService.Endpoint
import edu.uw.minh2804.rekognition.stores.Annotation
import java.lang.Exception

enum class CameraState {
    CAPTURING, CAPTURED, IDLE
}

class CameraViewModel : ViewModel() {
    private val _firebaseFunction = MutableLiveData<Endpoint>()
    val firebaseEndpoint: LiveData<Endpoint>
        get() = _firebaseFunction

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

    fun onSetCameraTab(tabPosition: Int) {
        when (tabPosition) {
            0 -> _firebaseFunction.value = Endpoint.TEXT
            1 -> _firebaseFunction.value = Endpoint.OBJECT
        }
    }

    fun onCameraCapturing() {
        _cameraState.value = CameraState.CAPTURING
    }

    fun onCameraCaptured(photo: CameraOutput) {
        _cameraState.value = CameraState.CAPTURED
        _capturedPhoto.value = photo
    }

    fun onCameraCaptureFailed(exception: Exception) {
        _cameraState.value = CameraState.IDLE
        _encounteredError.value = exception
    }

    fun onImageAnnotated(output: Annotation) {
        _cameraState.value = CameraState.IDLE
        _imageAnnotation.value = output
    }

    fun onImageAnnotateFailed(exception: Exception) {
        _cameraState.value = CameraState.IDLE
        _imageAnnotation.value = null
        _encounteredError.value = exception
    }
}