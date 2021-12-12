/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.uw.minh2804.rekognition.models.HistoryItem
import edu.uw.minh2804.rekognition.stores.ImageProcessingResult

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
     * Populate the history list from saved data in storage.
     */
    fun populateHistoryList(historyData: Array<ImageProcessingResult>) {
        Log.d(TAG, "Initializing the history list")
        _historyList.value = historyData.map {
            HistoryItem(image=Uri.parse(it.thumbnailUriPath), text=it.result)
        }
    }

    companion object {
        private val TAG = HistoryViewModel::class.simpleName
    }
}