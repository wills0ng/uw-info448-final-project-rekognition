package edu.uw.minh2804.rekognition.stores

import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import edu.uw.minh2804.rekognition.R
import edu.uw.minh2804.rekognition.services.AnnotateImageResponse
import java.io.File

data class Annotation(val result: AnnotateImageResponse)

class AnnotationStore(private val context: FragmentActivity) : ItemStore<Annotation> {
    private val directory: File by lazy {
        context.filesDir.let { appDirectory ->
            File(appDirectory, context.resources.getString(R.string.app_name) + ".annotations").also { it.mkdirs() }
        }
    }

    override val items: List<SavedItem<Annotation>>
        get() = directory.listFiles()!!.map {
            readAnnotationFrom(it)
        }

    override fun findItem(id: String): SavedItem<Annotation>? {
        val file = directory.listFiles()!!.firstOrNull {
            it.nameWithoutExtension == id
        }
        return if (file != null) readAnnotationFrom(file) else null
    }

    override fun save(id: String, item: Annotation): SavedItem<Annotation> {
        File(directory, "$id.json").also {
            it.writeText(Gson().toJson(item))
        }
        return SavedItem(id, item)
    }

    private fun readAnnotationFrom(file: File): SavedItem<Annotation> {
        val item = file.reader().let {
            var result = Gson().fromJson(it, AnnotateImageResponse::class.java)
            it.close()
            result
        }
        return SavedItem(file.nameWithoutExtension, Annotation(item))
    }
}