package edu.uw.minh2804.rekognition.stores

import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import edu.uw.minh2804.rekognition.R
import java.io.File

data class Property(
    val name: String,
    val value: String
)

data class EntityAnnotation(
    val properties: List<Property>,
    val score: Double
)

data class TextAnnotation(
    val text: String
)

data class AnnotateImageResponse(
    val fullTextAnnotation: TextAnnotation?,
    val labelAnnotations: EntityAnnotation?
)

data class ImageAnnotationItem(
    val responses: List<AnnotateImageResponse>
)

class ImageAnnotationStore(private val context: FragmentActivity) : ItemStore<ImageAnnotationItem> {
    private val directory: File by lazy {
        context.filesDir.let { appDirectory ->
            File(appDirectory, context.resources.getString(R.string.app_name) + ".responses").also { it.mkdirs() }
        }
    }

    override val items: List<SavedItem<ImageAnnotationItem>>
        get() = directory.listFiles()!!.map {
            readSavedBatchAnnotateImagesResponseFrom(it)
        }

    override fun findItem(id: String): SavedItem<ImageAnnotationItem>? {
        val file = directory.listFiles()!!.firstOrNull {
            it.nameWithoutExtension == id
        }
        return if (file != null) readSavedBatchAnnotateImagesResponseFrom(file) else null
    }

    override fun setItem(id: String, item: ImageAnnotationItem): SavedItem<ImageAnnotationItem> {
        val file = File(directory, "$id.json").also {
            it.writeText(Gson().toJson(item))
        }
        return readSavedBatchAnnotateImagesResponseFrom(file)
    }

    private fun readSavedBatchAnnotateImagesResponseFrom(file: File): SavedItem<ImageAnnotationItem> {
        val item = file.reader().let {
            var result = Gson().fromJson(it, ImageAnnotationItem::class.java)
            it.close()
            result
        }
        return SavedItem(file.nameWithoutExtension, item)
    }
}