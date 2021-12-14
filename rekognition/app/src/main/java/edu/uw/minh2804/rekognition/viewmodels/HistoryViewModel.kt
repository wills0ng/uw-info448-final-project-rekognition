/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import edu.uw.minh2804.rekognition.models.HistoryItem
import edu.uw.minh2804.rekognition.stores.AnnotationStore
import edu.uw.minh2804.rekognition.stores.PhotoStore
import edu.uw.minh2804.rekognition.stores.ThumbnailStore

/**
 * A view model for the HistoryFragment. Holds history for images captured using this app
 * and the associated detected text / objects. It is initialized with the images directory
 * passed from the HistoryFragment.
 */
class HistoryViewModel : ViewModel() {

    private val _historyList = MutableLiveData<List<HistoryItem>>()
    val historyList : LiveData<List<HistoryItem>>
        get() = _historyList

    init {
        Log.d(TAG, "HistoryViewModel initialized")
    }

    /**
     * Populate the history list from the thumbnails directory.
     */
    suspend fun populateHistoryList(
        photoStore: PhotoStore,
        thumbnailStore: ThumbnailStore,
        annotationStore: AnnotationStore
    ) {
        Log.d(TAG, "Initializing the history list")
        // Note: treat photo store as source of truth since users can delete photos
        // Then look up thumbnails and annotations
        _historyList.value = photoStore.items.map { photo ->
            val id = photo.value.id
            val photoUri = Uri.fromFile(photo.value.item.file)
            val thumbnailUri = thumbnailStore.getUri(id)
            val annotation = annotationStore.findItem(id)

            HistoryItem(photoUri, thumbnailUri, annotation.toString())
        }
    }

    companion object {
        private val TAG = HistoryViewModel::class.simpleName
    }
}