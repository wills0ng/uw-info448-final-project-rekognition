/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import edu.uw.minh2804.rekognition.models.AnnotationPair
import edu.uw.minh2804.rekognition.models.HistoryItem
import edu.uw.minh2804.rekognition.services.FirebaseFunctionsService.Endpoint.OBJECT
import edu.uw.minh2804.rekognition.stores.Annotation
import edu.uw.minh2804.rekognition.stores.AnnotationStore
import edu.uw.minh2804.rekognition.stores.PhotoStore
import edu.uw.minh2804.rekognition.stores.SavedItem
import edu.uw.minh2804.rekognition.stores.ThumbnailStore
import java.text.SimpleDateFormat
import java.util.*

/**
 * A view model for the HistoryFragment. Holds history for images captured using this app
 * and the associated detected text / objects.
 */
class HistoryViewModel : ViewModel() {
    private lateinit var _photoStore: PhotoStore
    private lateinit var _thumbnailStore: ThumbnailStore
    private lateinit var _annotationStore: AnnotationStore

    private val _historyList = MutableLiveData<List<HistoryItem>>()
    val historyList : LiveData<List<HistoryItem>>
        get() = _historyList

    init {
        Log.d(TAG, "HistoryViewModel initialized")
    }

    // Set the data stores used by this ViewModel
    fun setDataStores(
        photoStore: PhotoStore,
        thumbnailStore: ThumbnailStore,
        annotationStore: AnnotationStore
    ) {
       _photoStore = photoStore
       _thumbnailStore = thumbnailStore
       _annotationStore = annotationStore
    }

    // Populate the ViewModel internal history list from the data stores
    suspend fun updateHistoryList() {
        Log.d(TAG, "Updating the history list")
        // Note: treat photo store as source of truth since users can delete photos
        // Then look up thumbnails and annotations
        _historyList.value = _photoStore.items.sortedByDescending{ it.value.id }.map { photo ->
            val id = photo.value.id
            val date = SimpleDateFormat(PhotoStore.FILENAME_FORMAT, Locale.US).parse(id)
            val photoUri = Uri.fromFile(photo.value.item.file)
            val thumbnailUri = _thumbnailStore.getUri(id)
            val annotation = parseAnnotation(_annotationStore.findItem(id))

            HistoryItem(id, date, photoUri, thumbnailUri, annotation)
        }
        Log.d(TAG, "Finished updating history list")
    }

    // Given an id, checks if that item exists in the internal _historyList
    fun doesItemExist(id: String): Boolean {
        var itemExists = false
        _historyList.value?.let {
            for (item in it) {
                if (item.id == id) {
                    itemExists = true
                }
            }
        }
        return itemExists
    }

    // Parse SavedAnnotation objects into AnnotationPair objects.
    // Specifically, determines wither the annotation had no results, had text detection results,
    // or had object label results, and returns the annotation type and simplified text string.
    private fun parseAnnotation(savedAnnotation: SavedItem<Annotation>?): AnnotationPair {
        return savedAnnotation?.let {
            with(it.item.result) {
                this.fullTextAnnotation?.let { textAnnotation ->
                    AnnotationPair("Text", textAnnotation.text)
                } ?:
                    AnnotationPair("Objects", OBJECT.formatResult(this))
            }
        } ?: AnnotationPair("No Result", null)
    }

    companion object {
        private val TAG = HistoryViewModel::class.simpleName
    }
}