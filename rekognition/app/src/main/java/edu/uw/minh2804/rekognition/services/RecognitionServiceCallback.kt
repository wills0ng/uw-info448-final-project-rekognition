package edu.uw.minh2804.rekognition.services

interface RecognitionServiceCallback<T> {
    fun onResultFound(annotation: T)
    fun onResultNotFound()
    fun onError(exception: Exception)
}