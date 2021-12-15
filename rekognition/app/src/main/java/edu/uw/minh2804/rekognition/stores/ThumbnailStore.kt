package edu.uw.minh2804.rekognition.stores

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.extensions.scaleDown
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class Thumbnail(file: File) {
    val bitmap: Bitmap = BitmapFactory.decodeFile(file.toURI().path).scaleDown(ThumbnailSetting.MAX_DIMENSION).copy(Bitmap.Config.RGB_565, false)
    val orientation: String? = ExifInterface(file.absolutePath).getAttribute(ExifInterface.TAG_ORIENTATION)
}

class ThumbnailStore(private val context: FragmentActivity) : ItemStore<Thumbnail> {
    private val directory: File by lazy {
        context.filesDir.let { appDirectory ->
            File(appDirectory, context.resources.getString(R.string.app_name) + ".thumbnails").also { it.mkdirs() }
        }
    }

    override val items: List<Lazy<SavedItem<Thumbnail>>>
        get() = directory.listFiles()!!.map {
            lazy { SavedItem(it.nameWithoutExtension, Thumbnail(it)) }
        }

    override suspend fun findItem(id: String): SavedItem<Thumbnail>? {
        return withContext(context.lifecycleScope.coroutineContext + Dispatchers.IO) {
            val file = directory.listFiles()!!.firstOrNull {
                it.nameWithoutExtension == id
            }
            if (file != null) SavedItem(file.nameWithoutExtension, Thumbnail(file)) else null
        }
    }

    suspend fun getUri(id: String): Uri? {
        return withContext(context.lifecycleScope.coroutineContext + Dispatchers.IO) {
            val file = directory.listFiles()!!.firstOrNull {
                it.nameWithoutExtension == id
            }
            if (file != null) Uri.fromFile(file) else null
        }
    }

    override suspend fun save(id: String, item: Thumbnail): SavedItem<Thumbnail> {
        return withContext(NonCancellable + Dispatchers.IO) {
            File(directory, "$id.jpg").let {
                item.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it.outputStream())
                // Save thumbnail with exif data containing original image orientation
                val fileDescriptor = context.contentResolver.openFileDescriptor(
                    Uri.fromFile(it), "rw")!!.fileDescriptor
                val exif = ExifInterface(fileDescriptor)
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, item.orientation)
                exif.saveAttributes()
            }
            SavedItem(id, item)
        }
    }
}

object ThumbnailSetting {
    const val MAX_DIMENSION = 640
}