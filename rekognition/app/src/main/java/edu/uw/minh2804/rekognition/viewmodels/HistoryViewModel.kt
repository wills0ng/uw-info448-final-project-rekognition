/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.viewmodels

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import edu.uw.minh2804.rekognition.models.HistoryItem
import java.io.File

/**
 * A view model for the HistoryFragment. Holds history for images captured using this app
 * and the associated detected text / objects. It is initialized with the images directory
 * passed from the HistoryFragment.
 */
class HistoryViewModel(private val imagesDir: File) : ViewModel() {

    val historyList = initializeHistoryList()

    init {
        Log.i(TAG, "HistoryViewModel initialized")
    }

    /**
     * Initializes the history list from saved data in storage.
     */
    private fun initializeHistoryList(): List<HistoryItem> {
        Log.i(TAG, "Initializing the history list")
        val imageFiles = imagesDir.listFiles()
        return imageFiles?.map {
            // TODO: replace placeholder text
            // TODO: use thumbnail images
            HistoryItem(image=it.toUri(), text="Hello")
        } ?: listOf<HistoryItem>()
    }

    companion object {
        private val TAG = HistoryViewModel::class.simpleName
    }
}