package edu.uw.minh2804.rekognition.stores

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import edu.uw.minh2804.rekognition.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class Thumbnail(file: File) {
    val bitmap: Bitmap = BitmapFactory.decodeFile(file.toURI().path).scale(ThumbnailSetting.MAX_WIDTH, ThumbnailSetting.MAX_HEIGHT)
}

class ThumbnailStore(private val context: FragmentActivity) : ItemStore<Thumbnail> {
    private val directory: File by lazy {
        context.filesDir.let { appDirectory ->
            File(appDirectory, context.resources.getString(R.string.app_name) + ".thumbnails").also { it.mkdirs() }
        }
    }

    override val items: List<SavedItem<Thumbnail>>
        get() = directory.listFiles()!!.map {
            SavedItem(it.nameWithoutExtension, Thumbnail(it))
        }

    override suspend fun findItem(id: String): SavedItem<Thumbnail>? {
        return withContext(context.lifecycleScope.coroutineContext + Dispatchers.IO) {
            val file = directory.listFiles()!!.firstOrNull {
                it.nameWithoutExtension == id
            }
            if (file != null) SavedItem(file.nameWithoutExtension, Thumbnail(file)) else null
        }
    }

    override suspend fun save(id: String, item: Thumbnail): SavedItem<Thumbnail> {
        return withContext(context.lifecycleScope.coroutineContext + Dispatchers.IO) {
            File(directory, "$id.jpg").let {
                item.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it.outputStream())
            }
            SavedItem(id, item)
        }
    }
}

object ThumbnailSetting {
    const val MAX_HEIGHT = 320
    const val MAX_WIDTH = 320
}