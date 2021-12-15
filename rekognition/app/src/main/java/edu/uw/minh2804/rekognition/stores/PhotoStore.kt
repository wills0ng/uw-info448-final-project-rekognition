/** Tom Nguyen: I wrote this file and it's corresponding xml files. **/

package edu.uw.minh2804.rekognition.stores

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import edu.uw.minh2804.rekognition.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// This class is used to provide semantic to a file object.
// In other word, the file that it is holding is a photo.
data class Photo(val file: File)

// This store is responsible for reading and writing photo data to the device's internal storage.
class PhotoStore(private val context: FragmentActivity) : ItemStore<Photo> {
    private val directory: File by lazy {
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let { appDirectory ->
            File(appDirectory, context.resources.getString(R.string.app_name)).also { it.mkdirs() }
        }
        if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
    }

    override val items: List<Lazy<SavedItem<Photo>>>
        get() = directory.listFiles()!!.map {
            lazy { SavedItem(it.nameWithoutExtension, Photo(it)) }
        }

    override suspend fun findItem(id: String): SavedItem<Photo>? {
        return withContext(context.lifecycleScope.coroutineContext + Dispatchers.IO) {
            val file = directory.listFiles()!!.firstOrNull {
                it.nameWithoutExtension == id
            }
            if (file != null) SavedItem(id, Photo(file)) else null
        }
    }

    // Save the item into internal storage, with the id as file name.
    // Returns a SavedItem wrapper as indicator of success, else throw exceptions.
    override suspend fun save(id: String, item: Photo): SavedItem<Photo> {
        return withContext(context.lifecycleScope.coroutineContext) { SavedItem(id, item) }
    }

    fun createOutputFile(): File {
        val uniqueFileName = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        return File(directory, uniqueFileName)
    }

    companion object {
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}