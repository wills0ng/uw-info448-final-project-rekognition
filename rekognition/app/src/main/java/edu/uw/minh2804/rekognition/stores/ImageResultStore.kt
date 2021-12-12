package edu.uw.minh2804.rekognition.stores

import android.graphics.Bitmap
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.text.SimpleDateFormat
import java.util.*

data class ImageResultItem(
    val imageAnnotation: ImageAnnotationItem?,
    val photo: PhotoItem,
    val thumbnail: ThumbnailItem
)

class ImageResultStore(private val context: FragmentActivity) : ItemStore<ImageResultItem> {
    private val imageAnnotationStore = ImageAnnotationStore(context)
    private val photoStore = PhotoStore(context)
    private val thumbnailStore = ThumbnailStore(context)

    override val items: List<SavedItem<ImageResultItem>>
        get() = photoStore.items.map {
            SavedItem(it.id, ImageResultItem(imageAnnotationStore.findItem(it.id)!!.item, it.item, thumbnailStore.findItem(it.id)!!.item))
        }

    override fun findItem(id: String): SavedItem<ImageResultItem>? {
        val imageAnnotation = imageAnnotationStore.findItem(id)
        val photo = photoStore.findItem(id) ?: return null
        val thumbnail = thumbnailStore.findItem(id) ?: return null
        return SavedItem(id, ImageResultItem(imageAnnotation?.item, photo.item, thumbnail.item))
    }

    override fun setItem(id: String, item: ImageResultItem): SavedItem<ImageResultItem> {
        val imageAnnotation = if (item.imageAnnotation != null) imageAnnotationStore.setItem(id, item.imageAnnotation) else null
        val photo = photoStore.setItem(id, item.photo)
        val thumbnail = thumbnailStore.setItem(id, item.thumbnail)
        return SavedItem(id, ImageResultItem(imageAnnotation?.item, photo.item, thumbnail.item))
    }

    fun save(imageAnnotationResponse: JsonObject, rawPhoto: Bitmap): SavedItem<ImageResultItem> {
        return save(
            ImageResultItem(
                Gson().fromJson(imageAnnotationResponse, ImageAnnotationItem::class.java),
                PhotoItem(rawPhoto),
                ThumbnailItem(rawPhoto)
            )
        )
    }

    private fun save(item: ImageResultItem): SavedItem<ImageResultItem> {
        val newId = createNewId()
        return setItem(newId, item)
    }

    private fun createNewId(): String {
        return SimpleDateFormat(ID_FORMAT, Locale.US).format(System.currentTimeMillis())
    }

    companion object {
        const val ID_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}