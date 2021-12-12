package edu.uw.minh2804.rekognition.stores

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import androidx.fragment.app.FragmentActivity
import edu.uw.minh2804.rekognition.R
import java.io.File

class ThumbnailItem(bitmap: Bitmap) {
    val bitmap: Bitmap = bitmap.scale(ThumbnailSetting.MAX_WIDTH, ThumbnailSetting.MAX_HEIGHT)
}

class ThumbnailStore(private val context: FragmentActivity) : ItemStore<ThumbnailItem> {
    private val directory: File by lazy {
        context.filesDir.let { appDirectory ->
            File(appDirectory, context.resources.getString(R.string.app_name) + ".thumbnails").also { it.mkdirs() }
        }
    }

    override val items: List<SavedItem<ThumbnailItem>>
        get() = directory.listFiles()!!.map {
            readSavedThumbnailFrom(it)
        }

    override fun findItem(id: String): SavedItem<ThumbnailItem>? {
        val file = directory.listFiles()!!.firstOrNull {
            it.nameWithoutExtension == id
        }
        return if (file != null) readSavedThumbnailFrom(file) else null
    }

    override fun setItem(id: String, item: ThumbnailItem): SavedItem<ThumbnailItem> {
        val file = File(directory, "$id.jpg").also {
            item.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it.outputStream())
        }
        return readSavedThumbnailFrom(file)
    }

    private fun readSavedThumbnailFrom(file: File): SavedItem<ThumbnailItem> {
        return SavedItem(file.nameWithoutExtension, ThumbnailItem(BitmapFactory.decodeFile(file.path)))
    }
}

object ThumbnailSetting {
    const val MAX_HEIGHT = 320
    const val MAX_WIDTH = 320
}