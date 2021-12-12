/** Will Song: I wrote this file. */

package edu.uw.minh2804.rekognition.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.io.File

/**
 * This ViewModelProvider Factory allows us to create a HistoryViewModel with initial data.
 * Uses boilerplate code for a ViewModel Factory from Kotlin code labs
 */
class HistoryViewModelFactory(private val imagesDir: File) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(imagesDir) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}