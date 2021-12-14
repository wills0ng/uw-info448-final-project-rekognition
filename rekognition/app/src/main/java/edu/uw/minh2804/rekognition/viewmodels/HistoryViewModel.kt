/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import edu.uw.minh2804.rekognition.models.HistoryItem
import edu.uw.minh2804.rekognition.stores.AnnotationStore
import edu.uw.minh2804.rekognition.stores.SavedItem
import edu.uw.minh2804.rekognition.stores.Thumbnail
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
    suspend fun populateHistoryList(thumbnailUris: List<Uri>, annotationStore: AnnotationStore) {
        Log.d(TAG, "Initializing the history list")
        _historyList.value = thumbnailUris.map {
            val id = File(it.path!!).nameWithoutExtension
            val annotation = annotationStore.findItem(id)
            HistoryItem(image=it, text=annotation.toString())
        }
    }

    companion object {
        private val TAG = HistoryViewModel::class.simpleName
    }
}