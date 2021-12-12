package edu.uw.minh2804.rekognition.stores

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.fragment.app.FragmentActivity
import edu.uw.minh2804.rekognition.R
import java.io.File

data class PhotoItem(val bitmap: Bitmap)

class PhotoStore(private val context: FragmentActivity) : ItemStore<PhotoItem> {
    private val directory: File by lazy {
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let { appDirectory ->
            File(appDirectory, context.resources.getString(R.string.app_name)).also { it.mkdirs() }
        }
        if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
    }

    override val items: List<SavedItem<PhotoItem>>
        get() = directory.listFiles()!!.map {
            readSavedPhotoFrom(it)
        }

    override fun findItem(id: String): SavedItem<PhotoItem>? {
        val file = directory.listFiles()!!.firstOrNull {
            it.nameWithoutExtension == id
        }
        return if (file != null) readSavedPhotoFrom(file) else null
    }

    override fun setItem(id: String, item: PhotoItem): SavedItem<PhotoItem> {
        val file = File(directory, "$id.jpg").also {
            item.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it.outputStream())
        }
        return readSavedPhotoFrom(file)
    }

    private fun readSavedPhotoFrom(file: File): SavedItem<PhotoItem> {
        return SavedItem(file.nameWithoutExtension, PhotoItem(BitmapFactory.decodeFile(file.path)))
    }
}