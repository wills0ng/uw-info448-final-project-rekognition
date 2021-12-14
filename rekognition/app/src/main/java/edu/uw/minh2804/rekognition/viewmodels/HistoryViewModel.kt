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

    private val _historyList = MutableLiveData<List<HistoryItem>>()
    val historyList : LiveData<List<HistoryItem>>
        get() = _historyList

    init {
        Log.d(TAG, "HistoryViewModel initialized")
    }

    /**
     * Populate the history list from stored data.
     */
    suspend fun populateHistoryList(
        photoStore: PhotoStore,
        thumbnailStore: ThumbnailStore,
        annotationStore: AnnotationStore
    ) {
        Log.d(TAG, "Initializing the history list")
        // Note: treat photo store as source of truth since users can delete photos
        // Then look up thumbnails and annotations
        _historyList.value = photoStore.items.sortedByDescending{ it.value.id }.map { photo ->
            val id = photo.value.id
            val date = SimpleDateFormat(PhotoStore.FILENAME_FORMAT, Locale.US).parse(id)
            val photoUri = Uri.fromFile(photo.value.item.file)
            val thumbnailUri = thumbnailStore.getUri(id)
            val annotation = parseAnnotation(annotationStore.findItem(id))

            HistoryItem(id, date, photoUri, thumbnailUri, annotation)
        }
    }

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