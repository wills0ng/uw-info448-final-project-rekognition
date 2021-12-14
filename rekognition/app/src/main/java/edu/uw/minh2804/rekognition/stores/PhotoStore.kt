package edu.uw.minh2804.rekognition.stores

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import edu.uw.minh2804.rekognition.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

data class Photo(val file: File)

class PhotoStore(private val context: FragmentActivity) : ItemStore<Photo> {
    val directory: File by lazy {
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let { appDirectory ->
            File(appDirectory, context.resources.getString(R.string.app_name)).also { it.mkdirs() }
        }
        if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
    }

    override val items: List<SavedItem<Photo>>
        get() = directory.listFiles()!!.map {
            SavedItem(it.nameWithoutExtension, Photo(it))
        }

    override fun findItem(id: String): SavedItem<Photo>? {
        val file = directory.listFiles()!!.firstOrNull {
            it.nameWithoutExtension == id
        } ?: return null
        return SavedItem(id, Photo(file))
    }

    override fun save(id: String, item: Photo): SavedItem<Photo> {
        return SavedItem(id, item)
    }

    override fun saveAsync(id: String, item: Photo): Deferred<SavedItem<Photo>> {
        return context.lifecycleScope.async(Dispatchers.IO) {
            save(id, item)
        }
    }

    fun createOutputFile(): File {
        val uniqueFileName = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        return File(directory, uniqueFileName)
    }

    companion object {
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}